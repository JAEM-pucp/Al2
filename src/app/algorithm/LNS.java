package app.algorithm;

import app.model.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class LNS {
    public Solution Solve(Solution solution, Environment environment) {
        int n = 1;
        float score;
        float bestScore = 0;
        int bestUnroutedAmount;
        int unroutedAmount;
        Solution initialSolution = null;
        Solution newSolution;
        Solution bestSolution;
        if (!solution.started) {
            //create initial solution
            this.ConstructInitialSolution(solution, environment);
        }
        initialSolution = solution;
        //save initial solution score
        bestSolution = initialSolution;
        bestScore = this.EvaluateSolution(initialSolution);
        bestUnroutedAmount = initialSolution.GetUnroutedAmount();
        //iterate n number of times
        for (int i = 0; i < n; i++) {
            //create a copy of the initial solution
            newSolution = new Solution(initialSolution);
            //destroy the solution
            this.Destroy(newSolution, environment);
            //repair solution
            this.Repair(newSolution, environment);
            //evaluateSolution
            score = this.EvaluateSolution(newSolution);
            unroutedAmount = newSolution.GetUnroutedAmount();
            if (unroutedAmount < bestUnroutedAmount) {
                bestUnroutedAmount = unroutedAmount;
                bestScore = score;
                bestSolution = newSolution;
            } else if (unroutedAmount == bestUnroutedAmount && score > bestScore) {
                bestScore = score;
                bestSolution = newSolution;
            }
            if (unroutedAmount <= bestUnroutedAmount || score > 0.8 * bestScore) {
                initialSolution = newSolution;
            }
        }
        return bestSolution;
    }

    private float EvaluateSolution(Solution solution) {
        float score = 0;
        int scoreNum = 0;
        for (int i = 0; i < solution.routes.size(); i++) {
            if (solution.routes.get(i).GetDeliveryAmount() > 0) {
                score += this.EvaluateRoute(solution.routes.get(i));
                scoreNum++;
            }
        }
        if (scoreNum == 0) {
            return 0;
        } else {
            return score / scoreNum;
        }
    }


    public void ConstructInitialSolution(Solution solution, Environment environment) {
        boolean insertionWasPossible;
        ArrayList<Request> unrouted;
        Route newRoute;
        insertionWasPossible = true;
        int coverableLoad;
        int availableCapacity;
        while (insertionWasPossible && solution.unrouted.size() != 0) {
            insertionWasPossible = false;
            for (int i = 0; i < solution.routes.size(); i++) {
                if (solution.routes.get(i).vehicle.isAvailable) {
                    availableCapacity = solution.routes.get(i).GetAvailableCapacity();
                    if (availableCapacity > 0) {
                        unrouted = solution.GetUnroutedRequests();
                        for (int j = 0; j < unrouted.size(); ) {
                            coverableLoad = solution.routes.get(i).GetCoverableLoad(unrouted.get(j));
                            newRoute = this.InsertRequest(unrouted.get(j), coverableLoad, solution.routes.get(i), environment);
                            if (newRoute != null) {
                                availableCapacity -= coverableLoad;
                                solution.UpdateRoute(newRoute);
                                unrouted.get(j).uncoveredLoad -= coverableLoad;
                                if (unrouted.get(j).uncoveredLoad == 0) {
                                    solution.RemoveFromUnrouted(unrouted.get(j));
                                    if (solution.unrouted.size() == 0) {
                                        break;
                                    }
                                    j++;
                                }
                                insertionWasPossible = true;
                                if (availableCapacity == 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
                if (solution.unrouted.size() == 0) {
                    break;
                }
            }
        }
        solution.started = true;
    }

    public Route InsertRequest(Request request, int load, Route route, Environment environment) {
        ArrayList<TripNode> stops;
        float score;
        float bestScore = 0;
        Route newRoute;
        Route bestRoute = null;
        boolean insetionWasPossible = false;
        stops = route.GetStops();
        for (int i = 0; i < stops.size() - 1; i++) {
            //
            newRoute = new Route(route);
            newRoute = this.InsertRequestBetweenStops(request, load, stops.get(i), newRoute, environment);
            if (newRoute != null) {
                score = this.EvaluateRoute(newRoute);
                if (score > bestScore) {
                    bestScore = score;
                    bestRoute = newRoute;
                    insetionWasPossible = true;
                }
            }
        }
        if (insetionWasPossible) {
            return bestRoute;
        } else {
            return null;
        }
    }

    private float EvaluateRoute(Route route) {
        float score = 0;
        int scoreNum = 0;
        for (int i = 0; i < route.tripNodes.size(); i++) {
            for (int j = 0; j < route.tripNodes.get(i).deliveries.size(); j++) {
                score += ChronoUnit.MINUTES.between(route.tripNodes.get(i).visitTime,
                        route.tripNodes.get(i).deliveries.get(j).startTime.
                                plusHours(route.tripNodes.get(i).deliveries.get(j).timeWindow));
                scoreNum++;
            }
        }
        if (scoreNum == 0) return 0;
        return score / scoreNum;
    }

    public Route InsertRequestBetweenStops(Request request, int load, TripNode startingStop, Route route, Environment environment) {
        ArrayList<TripNode> stops;
        ArrayList<TripNode> trip;
        Vehicle vehicle = new Vehicle(route.vehicle);
        vehicle.load+=load;
        stops = route.GetStops();
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).id == startingStop.id) {
                stops.add(i + 1, new TripNode(request, load));
                break;
            }
        }
        for (int i = 0; i < stops.size(); i++) {
            stops.get(i).id = i;
        }
        return this.CalculateRouteFromStops(stops, route.id, vehicle, environment);
    }

    public Route CalculateRouteFromStops(ArrayList<TripNode> stops, int routeId, Vehicle vehicle, Environment environment) {
        if (stops.size() == 2 && stops.get(0).isDepot) {
            return new Route(routeId, stops, vehicle);
        }
        ArrayList<AStarNode> openList;
        ArrayList<AStarNode> closedList;
        ArrayList<AStarNode> neighbors;
        ArrayList<TripNode> trip;
        Route route = new Route();
        AStarNode aStarNode = null;
        int lowestF = 9999;
        int indexInOpenList = -1;
        int indexInClosedList = -1;
        boolean isNeighborInOpenList;
        boolean isNeighborInClosedList;
        boolean destinationReached = false;
        LocalDateTime initialTime;
        if (stops.get(0).visitTime == null) {
            initialTime = environment.currentTime;
        } else {
            initialTime = stops.get(0).visitTime;
        }
        route.id = routeId;
        route.vehicle = vehicle;
        route.tripNodes.add(stops.get(0));
        for (int i = 0; i < stops.size() - 1; i++) {
            openList = new ArrayList<>();
            openList.add(new AStarNode(stops.get(i), Math.abs(stops.get(i).x - stops.get(i + 1).x) + Math.abs(stops.get(i).y - stops.get(i + 1).y), initialTime));
            closedList = new ArrayList<>();
            destinationReached = false;
            while (!destinationReached) {
                //get node with lowest f from open list
                lowestF = 9999;
                indexInOpenList = -1;
                for (int j = 0; j < openList.size(); j++) {
                    if (openList.get(j).f < lowestF) {
                        aStarNode = openList.get(j);
                        indexInOpenList = j;
                        lowestF = openList.get(j).f;
                    }
                }
                if (aStarNode.x == stops.get(i + 1).x && aStarNode.y == stops.get(i + 1).y) {
                    destinationReached = true;
                    break;
                } else {
                    openList.remove(indexInOpenList);
                    closedList.add(aStarNode);
                    neighbors = this.GetNeighbors(aStarNode, stops.get(i + 1).x, stops.get(i + 1).y, initialTime, 60 / vehicle.speed, environment);
                    for (int j = 0; j < neighbors.size(); j++) {
                        isNeighborInOpenList = false;
                        isNeighborInClosedList = false;
                        for (int k = 0; k < openList.size(); k++) {
                            if (openList.get(k).x == neighbors.get(j).x && openList.get(k).y == neighbors.get(j).y) {
                                isNeighborInOpenList = true;
                                indexInOpenList = k;
                                break;
                            }
                        }
                        for (int k = 0; k < closedList.size(); k++) {
                            if (closedList.get(k).x == neighbors.get(j).x && closedList.get(k).y == neighbors.get(j).y) {
                                isNeighborInClosedList = true;
                                indexInClosedList = k;
                                break;
                            }
                        }
                        if (isNeighborInClosedList && neighbors.get(j).g < closedList.get(indexInClosedList).g) {
                            closedList.get(indexInClosedList).g = neighbors.get(j).g;
                            closedList.get(indexInClosedList).f = closedList.get(indexInClosedList).g + closedList.get(indexInClosedList).h;
                            closedList.get(indexInClosedList).parent = aStarNode;
                        } else if (isNeighborInOpenList && neighbors.get(j).g < openList.get(indexInOpenList).g) {
                            openList.get(indexInOpenList).g = neighbors.get(j).g;
                            openList.get(indexInOpenList).f = openList.get(indexInOpenList).g + openList.get(indexInOpenList).h;
                            openList.get(indexInOpenList).parent = aStarNode;
                        } else if (!isNeighborInOpenList && !isNeighborInClosedList) {
                            openList.add(neighbors.get(j));
                        }
                    }
                }
            }

            stops.get(i).visitTime = initialTime;
            stops.get(i + 1).visitTime = aStarNode.visitTime;
            for (int j = 0; j < stops.get(i + 1).deliveries.size(); j++) {
                if (stops.get(i + 1).visitTime.isAfter(stops.get(i + 1).deliveries.get(j).startTime.
                        plusHours(stops.get(i + 1).deliveries.get(j).timeWindow))) {
                    return null;
                }
            }
            initialTime = aStarNode.visitTime;
            trip = new ArrayList<>();
            while (aStarNode.parent != null) {
                trip.add(new TripNode(aStarNode));
                aStarNode = aStarNode.parent;
            }
            trip.remove(0);
            Collections.reverse(trip);
            trip.add(stops.get(i + 1));

            route.tripNodes.addAll(trip);
        }
        for (int i = 0; i < route.tripNodes.size(); i++) {
            route.tripNodes.get(i).id = i;
        }
        return route;
    }

    private ArrayList<AStarNode> GetNeighbors(AStarNode aStarNode, int destinationX, int destinationY, LocalDateTime initialTime,
                                              int minutesBetweenMoves, Environment environment) {
        ArrayList<AStarNode> neighbors = new ArrayList<>();
        LocalDateTime currentTime = initialTime.plusMinutes((aStarNode.g + 1) * minutesBetweenMoves);
        int x;
        int y;
        x = aStarNode.x;
        y = aStarNode.y;
        if (environment.IsMoveAllowed(x, y + 1, currentTime) || (x == destinationX && y + 1 == destinationY)) {
            neighbors.add(new AStarNode(x, y + 1, aStarNode.g + 1, Math.abs(x - destinationX) + Math.abs((y + 1) - destinationY), aStarNode, currentTime));
        }
        if (environment.IsMoveAllowed(x + 1, y, currentTime) || (x + 1 == destinationX && y == destinationY)) {
            neighbors.add(new AStarNode(x + 1, y, aStarNode.g + 1, Math.abs((x + 1) - destinationX) + Math.abs(y - destinationY), aStarNode, currentTime));
        }
        if (environment.IsMoveAllowed(x, y - 1, currentTime) || (x == destinationX && y - 1 == destinationY)) {
            neighbors.add(new AStarNode(x, y - 1, aStarNode.g + 1, Math.abs(x - destinationX) + Math.abs((y - 1) - destinationY), aStarNode, currentTime));
        }
        if (environment.IsMoveAllowed(x - 1, y, currentTime) || (x - 1 == destinationX && y == destinationY)) {
            neighbors.add(new AStarNode(x - 1, y, aStarNode.g + 1, Math.abs((x - 1) - destinationX) + Math.abs(y - destinationY), aStarNode, currentTime));
        }
        return neighbors;
    }

    public void Destroy(Solution solution, Environment environment){
        ArrayList<Request> requests;
        Random random = new Random();
        requests = solution.GetRequests();
        int requestsRemoved;
        int requestsToBeRemoved;
        requestsToBeRemoved = (random.nextInt(20)+10)*requests.size()/100;
        requestsRemoved = 0;
        while(requestsRemoved<requestsToBeRemoved) {
            for (int i = 0; i < requests.size(); i++) {
                if (random.nextBoolean()) {
                    solution.RemoveRequest(requests.get(i).id,environment);
                    requestsRemoved++;
                }
            }
        }
    }
    public void Repair(Solution solution, Environment environment) {
        boolean insertionWasPossible;
        boolean anyInsertionWasPossible=true;
        ArrayList<Request> unrouted;
        Route newRoute;
        Route bestRoute=null;
        int coverableLoad;
        int bestCoverableLoad=-1;
        int availableCapacity;
        float score;
        float bestScore;
        while (anyInsertionWasPossible && solution.unrouted.size() != 0) {
            unrouted = solution.GetOrderedUnroutedRequests();
            anyInsertionWasPossible=false;
            for (int i=0;i<unrouted.size();) {
                bestScore=0;
                insertionWasPossible = false;
                bestCoverableLoad=0;
                for (int j = 0; j < solution.routes.size(); j++) {
                    if (solution.routes.get(j).vehicle.isAvailable) {
                        availableCapacity = solution.routes.get(j).GetAvailableCapacity();
                        if (availableCapacity > 0) {
                            coverableLoad = solution.routes.get(j).GetCoverableLoad(unrouted.get(i));
                            newRoute = this.InsertRequest(unrouted.get(i), coverableLoad, solution.routes.get(j), environment);
                            if (newRoute != null) {
                                score = this.EvaluateRoute(newRoute);
                                if(coverableLoad>bestCoverableLoad){
                                    bestScore=score;
                                    bestRoute=newRoute;
                                    bestCoverableLoad=coverableLoad;
                                    insertionWasPossible = true;
                                    anyInsertionWasPossible=true;
                                }else if(coverableLoad==bestCoverableLoad && score>bestScore){
                                    bestScore=score;
                                    bestRoute=newRoute;
                                    bestCoverableLoad=coverableLoad;
                                    insertionWasPossible = true;
                                    anyInsertionWasPossible=true;
                                }
                            }
                        }
                    }
                }
                if(insertionWasPossible) {
                    solution.UpdateRoute(bestRoute);
                    unrouted.get(i).uncoveredLoad -= bestCoverableLoad;
                    if (unrouted.get(i).uncoveredLoad == 0) {
                        solution.RemoveFromUnrouted(unrouted.get(i));
                        if (solution.unrouted.size() == 0) {
                            break;
                        }
                        i++;
                    }
                    if (solution.unrouted.size() == 0) {
                        break;
                    }
                }else {
                    i++;
                }
            }
        }
        solution.started = true;
    }
}