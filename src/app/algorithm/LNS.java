package app.algorithm;

import app.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

//40 motos 20 autos
//4 y 25 paquetes
//45 30 almacén
public class LNS {

    public LNS(){

    }

    public Output Solve(Input input){
        int unroutedAmount=0;
        int bestUnroutedAmount =9999;
        Output output = new Output();
        Solution bestSolution;
        Environment bestEnvironment;
        float newScore;
        float bestScore;
        Solution newSolution;
        Environment newEnvironment;
        Environment initialEnvironment;
        int iterations = 0;
        int n = 1;
        int destroyN = 2;
        Solution initialSolution;
        //check if algorithm was executed before
        if(input.previousSolution==null){
            //construct initial solution
            initialSolution = this.ConstructInitialSolution(input.environment,input.currentTime);
        }
        else{
            initialSolution = input.previousSolution;
        }
        bestScore = initialSolution.CalculateScore(input.environment,input.currentTime);
        bestSolution = initialSolution;
        bestEnvironment = input.environment;
        bestUnroutedAmount = initialSolution.unrouted.size();
        initialEnvironment = bestEnvironment;
        //run LNS n number of times
        while(iterations<n){
            //copy environment
            newEnvironment = initialEnvironment.CopyEnvironment();
            //copy solution
            newSolution = initialSolution.CopySolution(newEnvironment);
            //destroy new solution and store the unrouted requests
            this.Destroy(newSolution,newEnvironment,destroyN,false);

            //repair the solution
            this.Repair(newSolution,newEnvironment,input.currentTime);
            //calculate the score of the new solution
            newScore = newSolution.CalculateScore(newEnvironment,input.currentTime);
            if(newSolution.unrouted.size()<bestUnroutedAmount){
                bestScore = newScore;
                bestSolution = newSolution;
                bestEnvironment = newEnvironment;
                bestUnroutedAmount = newSolution.unrouted.size();
                initialSolution = newSolution;
                initialEnvironment = newEnvironment;
            }
            //compare and update if necessary
            if(newSolution.unrouted.size()==bestUnroutedAmount && newScore<bestScore){
                bestScore = newScore;
                bestSolution = newSolution;
                bestEnvironment = newEnvironment;
                initialSolution = newSolution;
                initialEnvironment = newEnvironment;
            }
            //increase counter
            iterations++;
        }
        output.environment = bestEnvironment;
        output.solution = bestSolution;
        return  output;
    }

    public Solution ConstructInitialSolution(Environment environment, LocalDateTime currentTime){
        int availableCapacity;
        Solution solution = new Solution(environment);
        Route newRoute;
        boolean noChanges=false;
        int j;
        Node depot = environment.GetDepot();
        ArrayList<Route> availableRoutes;
        //copy the requests
        ArrayList<Request> unrouted = new ArrayList<>();
        for(int i=0;i<environment.requests.size();i++){
            unrouted.add(environment.requests.get(i));
        }
        //get available routes
        availableRoutes = solution.GetAvailableRoutes();
        //iterate until all requests are serviced or no more can be inserted
        while(!noChanges && unrouted.size()!=0){
            noChanges=true;
            //for each route
            for(int i=0;i<availableRoutes.size();i++){
                //insert request in route if feasible
                availableCapacity = solution.routes.get(i).vehicle.capacity-solution.routes.get(i).vehicle.load;
                if (availableCapacity > 0) {
                    j=0;
                    while(j<unrouted.size()){
                        newRoute = this.InsertRequest(unrouted.get(j),availableRoutes.get(i),environment,currentTime);
                        if(newRoute.IsFeasible(currentTime,environment)){
                            availableRoutes.get(i).CopyFrom(newRoute,environment);

                            if(unrouted.get(j).load-unrouted.get(j).coveredLoad<=availableCapacity){
                                availableRoutes.get(i).vehicle.AddRequestLoad(unrouted.get(j).x,unrouted.get(j).y,unrouted.get(j).load-unrouted.get(j).coveredLoad);
                                unrouted.get(j).coveredLoad=unrouted.get(j).load;
                                unrouted.get(j).tripsLeft++;
                                availableCapacity-=availableRoutes.get(i).vehicle.GetRequestLoad(unrouted.get(j).x,unrouted.get(j).y);
                                availableRoutes.get(i).vehicle.load+=availableRoutes.get(i).vehicle.GetRequestLoad(unrouted.get(j).x,unrouted.get(j).y);

                                unrouted.remove(j);
                            }
                            else{
                                availableRoutes.get(i).vehicle.AddRequestLoad(unrouted.get(j).x,unrouted.get(j).y,availableCapacity);
                                unrouted.get(j).coveredLoad+=availableCapacity;
                                unrouted.get(j).tripsLeft++;
                                availableCapacity=0;
                                availableRoutes.get(i).vehicle.load=availableRoutes.get(i).vehicle.capacity;
                            }
                        }
                        else{
                            j++;
                        }
                        if(availableCapacity==0)break;
                    }
                }

            }
        }
        solution.unrouted.addAll(unrouted);

        return solution;
    }

    public void Destroy(Solution solution, Environment environment, int n, boolean notRandom){
        Random random = new Random();
        ArrayList<Request> requests;
        ArrayList<Request> unrouted = new ArrayList<>();
        ArrayList<Route> availableRoutes = solution.GetAvailableRoutes();
        boolean result;
        int eliminated=0;
        int requestAmount = solution.GetRequestAmount();
        int counter =0;

        //while stop condition not met
        while(eliminated< requestAmount/n) {

            //for every available route
            for (int i = 0; i < availableRoutes.size(); i++) {
                requests = availableRoutes.get(i).GetRequests(environment);

                //for every request in the route
                for (int j = 0; j < requests.size(); j++) {

                    //dont remove if already at request
                    if(requests.get(j).x != availableRoutes.get(i).nodes.get(0).x || requests.get(j).y != availableRoutes.get(i).nodes.get(0).y) {

                        //randomly choose to remove request
                        if (random.nextBoolean() || notRandom) {

                            //should modify
                            //nodes, stops, vehicle, covered load

                            result = this.RemoveRequest(requests.get(j), availableRoutes.get(i), environment);

                            //if it was removed successfully
                            if (result) {
                                if (!solution.unrouted.contains(requests.get(j))) {
                                    solution.unrouted.add(requests.get(j));
                                }

                                eliminated++;

                                if (eliminated >= requestAmount / n) break;
                            }
                        }
                    }
                }
                if(eliminated>= requestAmount/n)break;
            }
            counter++;
        }
    }

    public boolean RemoveRequest(Request request, Route route, Environment environment){
        int requestLoad;
        ArrayList<Node> trip;

        //if we're already at request, then it can't be removed
        if(route.nodes.get(0).x == request.x && route.nodes.get(0).y == request.y){
            return false;
        }

        //should modify nodes stops vehicles coveredload

        //vehicle and covered load
        requestLoad = route.vehicle.GetRequestLoad(request.x,request.y);

        request.coveredLoad-=requestLoad;
        request.tripsLeft--;
        route.vehicle.RemoveRequestLoad(request.x,request.y);
        //can only empty load if at depot
        if (route.nodes.get(0).isDepot) {
            route.vehicle.load-=requestLoad;
        }

        //remove request from stops
        for(int i=0;i<route.stops.size();i++){
            if(route.stops.get(i).x == request.x && route.stops.get(i).y == request.y){
                route.stops.remove(i);
                break;
            }
        }

        //recalculate route from key nodes
        this.RecalculateRoute(route,environment);

        return true;
    }

    public void RecalculateRoute(Route route, Environment environment){
        ArrayList<Node> importantNodes = new ArrayList<>();
        ArrayList<Node> newNodes = new ArrayList<>();
        int counter;
        if(!route.nodes.get(0).isDepot && !route.nodes.get(0).isRequest){
            importantNodes.add(route.nodes.get(0));
        }
        importantNodes.addAll(route.stops);
        counter=0;
        while(true){
            newNodes.addAll(this.CalculateRoute(importantNodes.get(counter),importantNodes.get(counter+1),environment));
            counter++;
            if(counter+1== importantNodes.size())break;
            newNodes.remove(newNodes.size()-1);
        }
        route.nodes = newNodes;
    }

    //repair can modify input with no problem
    public Solution Repair(Solution solution, Environment environment, LocalDateTime currentTime){
        int availableCapacity;
        int bestRouteIndex = 0;
        int requestLoad;
        int bestRequestLoad = 0;
        float bestScore;
        float newScore;
        boolean insertionWasPossible=true;
        boolean alreadyIn = false;
        Route newRoute;
        Route bestRoute = new Route();
        ArrayList<Route> availableRoutes = solution.GetAvailableRoutes();

        //repair until all requests are covered or no more insertions are possible
        while(solution.unrouted.size()!=0 && insertionWasPossible) {

            insertionWasPossible=false;
            //for each unrouted request
            for (int i = 0; i < solution.unrouted.size();) {
                bestScore = 9999;
                insertionWasPossible=false;

                //find the best route it can be inserted in
                for (int j = 0; j < availableRoutes.size(); j++) {
                    availableCapacity = availableRoutes.get(j).vehicle.capacity-availableRoutes.get(j).vehicle.load;
                    //if route can accept requests
                    if(availableCapacity>0) {
                        alreadyIn = false;

                        if (solution.unrouted.get(i).load - solution.unrouted.get(i).coveredLoad <= availableCapacity) {
                            requestLoad = solution.unrouted.get(i).load - solution.unrouted.get(i).coveredLoad;
                        } else {
                            requestLoad = availableCapacity;
                        }

                        //if the request is already in the route
                        for (int k = 0; k < availableRoutes.get(j).stops.size(); k++) {
                            if (solution.unrouted.get(i).x == availableRoutes.get(j).stops.get(k).x && solution.unrouted.get(i).y == availableRoutes.get(j).stops.get(k).y) {
                                alreadyIn = true;
                                break;
                            }
                        }
                        if (alreadyIn) {
                            bestRouteIndex = j;
                            bestRequestLoad = requestLoad;
                            insertionWasPossible = true;
                            break;
                        } else {

                            //otherwise insert request at its best position in the route
                            //insert request shouldn't modify input
                            //outputs modified nodes and stops, checks feasibility
                            newRoute = InsertRequest(solution.unrouted.get(i), availableRoutes.get(j), environment, currentTime);
                            if (newRoute != null) {
                                newScore = newRoute.EvaluateRoute(environment, currentTime);

                                //update the best result
                                if (newScore < bestScore) {
                                    bestScore = newScore;
                                    bestRoute = newRoute;
                                    bestRouteIndex = j;
                                    bestRequestLoad = requestLoad;
                                    insertionWasPossible = true;
                                }
                            }
                        }
                    }
                }

                //copy the best result into the route if changes were made
                if(insertionWasPossible){
                    if(!alreadyIn) {
                        availableRoutes.get(bestRouteIndex).CopyFrom(bestRoute, environment);
                    }
                    availableRoutes.get(bestRouteIndex).vehicle.AddRequestLoad(solution.unrouted.get(i).x,solution.unrouted.get(i).y,bestRequestLoad);
                    availableRoutes.get(bestRouteIndex).vehicle.load+=bestRequestLoad;
                    solution.unrouted.get(i).tripsLeft++;
                    solution.unrouted.get(i).coveredLoad+=bestRequestLoad;
                    if(solution.unrouted.get(i).load==solution.unrouted.get(i).coveredLoad){
                        solution.unrouted.remove(i);
                        if(solution.unrouted.size()==0)break;
                    }
                }
                else{
                    i++;
                }
            }
        }
        return solution;
    }

    public ArrayList<Node> CalculateRoute(Node origin, Node destination, Environment environment){
        ArrayList<Node> nodes = new ArrayList<>();
        Node currentNode = origin;
        Node chosenMove = new Node();
        int lowestCost = 999;
        int cost;
        nodes.add(currentNode);
        ArrayList<Node> possibleMoves;
        while(true){
            possibleMoves = this.GetPossibleMoves(currentNode,environment);
            for(int i=0;i< possibleMoves.size();i++){
                cost = possibleMoves.get(i).CalculateScore(destination);
                if(cost < lowestCost){
                    lowestCost = cost;
                    chosenMove = possibleMoves.get(i);
                }
            }
            nodes.add(chosenMove);
            if (chosenMove==destination)break;
            currentNode = chosenMove;
        }
        return nodes;
    }

    //inserts request at its best position in the route
    //insert request shouldn't modify input
    //outputs modified nodes and stops, checks feasibility
    public Route InsertRequest(Request request, Route route, Environment environment, LocalDateTime currentTime){
        Route bestRoute = route;
        float bestScore = 9999;
        float newScore;
        Route insertedRoute;
        ArrayList<Node> keyNodes;
        boolean changes = false;
        Environment newEnvironment;
        Route newRoute;
        Request newRequest;

        for(int i=0;i<route.stops.size()-1;i++){
            //make a copy of the route
            newRoute = new Route();
            for(int j=0;j<route.nodes.size();j++){
                newRoute.nodes.add(route.nodes.get(j));
            }
            for(int j=0;j<route.stops.size();j++){
                newRoute.stops.add(route.stops.get(j));
            }
            newRoute.vehicle = route.vehicle;
            newRoute.startTime = route.startTime;

            //insert after stop
            newRoute.stops.add(i+1,environment.GetNode(request.x, request.y));

            //recalculate route
            this.RecalculateRoute(newRoute,environment);
            newScore = newRoute.EvaluateRoute(environment,currentTime);
            //if this position IS FEASIBLE AND is the best to insert, update best
            if(newRoute.IsFeasible(currentTime,environment) && newScore<bestScore){
                bestScore = newScore;
                bestRoute = newRoute;
                changes = true;
            }
        }
        if(!changes) return null;
        return bestRoute;
    }

    //shouldn't modify inputs
    public Route InsertRequestAt(Request requestToInsert, Node previousStop, Route route, Environment environment){
        int x;
        int y;
        int stopsIndex=0;
        int positionToInsert = 0;
        int removed = 0;
        ArrayList<Node> nodes;
        //make a copy of the environment
        Environment newEnvironment = environment.CopyEnvironment();
        //make a copy of the route
        Route newRoute = route.CopyRoute(newEnvironment);
        //if the request is already partially serviced in the route
        if(previousStop.x==requestToInsert.x && previousStop.y==requestToInsert.y){
            return newRoute;
        }
        //take the request from the new environment
        Request newRequestToInsert = newEnvironment.GetRequest(requestToInsert.x, requestToInsert.y);
        Node newPreviousStop = newEnvironment.GetNode(previousStop.x, previousStop.y);
        if(newPreviousStop.isDepot){
            //find stops position
            for(int i=0;i<newRoute.stops.size();i++){
                if(newRoute.stops.get(i).x== newPreviousStop.x && newRoute.stops.get(i).y== newPreviousStop.y){
                    stopsIndex = i;
                    break;
                }
            }
            for(int i=0;i<newRoute.nodes.size();i++){
                if(newRoute.nodes.get(i).x== newRoute.stops.get(stopsIndex).x && newRoute.nodes.get(i).y== newRoute.stops.get(stopsIndex).y){
                    positionToInsert=i;
                    do{
                        x=newRoute.nodes.get(i).x;
                        y=newRoute.nodes.get(i).y;
                        newRoute.nodes.remove(i);
                        removed++;
                    }while(x!=newRoute.stops.get(stopsIndex+1).x && y!=newRoute.stops.get(stopsIndex+1).y || removed<=1);
                    break;
                }
            }
            nodes = this.CalculateRoute(newRoute.stops.get(stopsIndex),newEnvironment.GetNode(newRequestToInsert.x, newRequestToInsert.y),newEnvironment);
            nodes.remove(nodes.size()-1);
            nodes.addAll(this.CalculateRoute(newEnvironment.GetNode(newRequestToInsert.x, newRequestToInsert.y),newRoute.stops.get(stopsIndex+1),newEnvironment));
            newRoute.nodes.addAll(positionToInsert,nodes);
            /*if(newRequestToInsert.load<=newRoute.vehicle.capacity-newRoute.vehicle.load) {
                newRoute.vehicle.load += newRequestToInsert.load;
            }
            else{
                newRoute.vehicle.load = newRoute.vehicle.capacity;
            }*/
            newRoute.stops.add(stopsIndex+1,newEnvironment.GetNode(newRequestToInsert.x, newRequestToInsert.y));
        }
        return newRoute;
    }

    public ArrayList<Node> GetPossibleMoves(Node node, Environment environment){
        ArrayList<Node> nodes = new ArrayList<>();
        Node toAdd=null;
        toAdd = environment.GetNode(node.x-1,node.y);
        if(toAdd!=null){
            nodes.add(toAdd);
        }
        toAdd = environment.GetNode(node.x,node.y+1);
        if(toAdd!=null){
            nodes.add(toAdd);
        }
        toAdd = environment.GetNode(node.x+1,node.y);
        if(toAdd!=null){
            nodes.add(toAdd);
        }
        toAdd = environment.GetNode(node.x,node.y-1);
        if(toAdd!=null){
            nodes.add(toAdd);
        }
        nodes.add(node);
        return nodes;
    }
}
