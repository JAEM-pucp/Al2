package app.algorithm;

import app.model.*;

import java.util.ArrayList;
import java.util.Random;

//40 motos 20 autos
//4 y 25 paquetes
//45 30 almacén
public class LNS {

    public Solution Solve(ArrayList<Request> requests, Node depot, int carAmount, int motorcycleAmount, Environment environment){
        //make a copy of requests called unrouted
        ArrayList<Request> unrouted = new ArrayList<>();
        for(int i=0;i< requests.size();i++){
            unrouted.add(requests.get(i));
        }
        Solution solution = this.ConstructInitialSolution(unrouted,environment);
        Solution newSolution = new Solution(environment);
        int iterations = 0;
        while(iterations < 10){
            //needs proper copy function
            //newSolution = solution;
            unrouted.addAll(this.Destroy(newSolution,environment));
            this.Repair(newSolution,unrouted,environment);
            iterations++;
        }
        return solution;
    }

    public Solution ConstructInitialSolution(ArrayList<Request> unrouted, Environment environment){
        Solution solution = new Solution(environment);
        Route newRoute;
        boolean noChanges=true;
        int requestsToEvaluate=0;
        solution.requestAmount= unrouted.size();
        while(!noChanges && unrouted.size()!=0){
            noChanges=true;
            for(int i=0;i<solution.routes.size();i++){
                newRoute=this.InsertRequest(unrouted.get(0),solution.routes.get(i),environment);
                if(unrouted.get(0).insertionCost<900){
                    solution.routes.set(i,newRoute);
                    solution.routes.get(i).FixDurations();
                    solution.routes.get(i).vehicle.load+=unrouted.get(0).load;
                    unrouted.remove(0);
                    noChanges=false;
                }
                else{
                    requestsToEvaluate++;
                }
            }
        }

        return solution;
    }

    public ArrayList<Request> Destroy(Solution solution, Environment environment){
        Random random = new Random();
        ArrayList<Request> requests;
        ArrayList<Request> unrouted = new ArrayList<>();
        int eliminated=0;
        while(eliminated< solution.requestAmount/3) {
            for (int i = 0; i < solution.routes.size(); i++) {
                requests = solution.routes.get(i).GetRequests();
                for (int j = 0; j < requests.size(); i++) {
                    if (random.nextBoolean()) {
                        this.RemoveRequest(requests.get(j),solution.routes.get(i),environment);
                        eliminated++;
                    }
                }
            }
        }
        return unrouted;
    }

    public Solution Repair(Solution solution, ArrayList<Request> unrouted, Environment environment){
        int chosenRequest = 0;
        int bestInsertionCost;
        int insertionCost;
        int insertLocation;
        boolean insertionWasPossible=true;
        Route newRoute;
        Route bestRoute = new Route();
        while(insertionWasPossible && unrouted.size()!=0) {
            insertionWasPossible=false;
            for (int i = 0; i < solution.routes.size(); i++) {
                bestInsertionCost = 999;
                for (int j = 0; j < unrouted.size(); j++) {
                    newRoute = InsertRequest(unrouted.get(j), solution.routes.get(i), environment);
                    if (unrouted.get(j).insertionCost < bestInsertionCost) {
                        bestInsertionCost = unrouted.get(j).insertionCost;
                        bestRoute = newRoute;
                        chosenRequest = j;
                    }
                }
                if (bestInsertionCost < 900) {
                    bestRoute.vehicle.load+=unrouted.get(chosenRequest).load;
                    bestRoute.FixDurations();
                    solution.routes.set(i, bestRoute);
                    unrouted.remove(chosenRequest);
                    insertionWasPossible=true;
                    if(unrouted.size()==0)break;
                }
            }
        }
        return solution;
    }

    public ArrayList<Node> CalculateRoute(Node origin, Node destination, Environment environment){
        ArrayList<Node> route = new ArrayList<>();
        int originX = origin.x;
        int originY = origin.y;
        if (destination.x>originX){
            for (; originX <= destination.x; originX++){
                route.add(environment.getNode(originX,originY));
            }
        }
        else if (destination.x<originX){
            for (; originX <= destination.x; originX--){
                route.add(environment.getNode(originX,originY));
            }
        }
        else{
            route.add(environment.getNode(originX,originY));
        }

        if (destination.y>originY){
            originY++;
            for (; originY <= destination.y; originY++){
                route.add(environment.getNode(originX,originY));
            }
        }
        else if (destination.y<originY){
            originY--;
            for (; originY <= destination.y; originY--){
                route.add(environment.getNode(originX,originY));
            }
        }
        return route;
    }

    public Route InsertRequest(Request newRequest, Route route, Environment environment){
        //make a copy of the route to modify
        Route newRoute = new Route();
        ArrayList<Node> newNodes;
        ArrayList<Node> bestNodes= new ArrayList<>();
        int bestInsertionCost=999;
        int requestIndex=0;
        int bestRequestIndex=0;

        for(int i=0;i<route.nodes.size();i++){
            newRoute.nodes.add(route.nodes.get(i));
        }
        for(int i=0;i<route.stops.size();i++){
            newRoute.stops.add(route.stops.get(i));
        }
        newRoute.vehicle = route.vehicle;

        //find best position
        //two cases:
        //I'm at depot (the load can increase freely until it hits capacity)
        //check every mayor node except the return to depot, that'll be a new route
        for(int i=0;i<newRoute.stops.size()-1;i++){
            newNodes = InsertNode(newRequest,newRoute,i,environment);
            if(newRequest.insertionCost<bestInsertionCost){
                bestInsertionCost=newRequest.insertionCost;
                bestNodes=newNodes;
                bestRequestIndex=i;
            }
        }
        newRoute.nodes=bestNodes;
        newRoute.stops.add(bestRequestIndex+1,newRequest.destination);

        return newRoute;

        //I'm on the road (load can't increase unless I go back to depot or pass a broken down vehicle)

    }

    public ArrayList<Node> InsertNode(Request newRequest, Route route, int index, Environment environment){
        ArrayList<Node> newNodes = new ArrayList<>();
        ArrayList<Node> newStops = new ArrayList<>();
        ArrayList<Node> tripTo = new ArrayList<>();
        ArrayList<Node> tripFrom = new ArrayList<>();
        int x;
        int y;
        int cost=0;
        int stopIndex = 0;

        for(int i=0;i<route.nodes.size();i++){
            newNodes.add(route.nodes.get(i));
        }

        for(int i=0;i<route.stops.size();i++){
            newStops.add(route.stops.get(i));
        }
        newStops.add(index+1,newRequest.destination);

        for(int i=0;i< newNodes.size();i++){
            if(newNodes.get(i).x == newRequest.destination.x && newNodes.get(i).y == newRequest.destination.y){
                do {
                    x=newNodes.get(i).x;
                    y=newNodes.get(i).y;
                    newNodes.remove(i);
                }while(x!=route.stops.get(index+1).x || y!=route.stops.get(index+1).y);
                tripTo = this.CalculateRoute(route.stops.get(index),newRequest.destination,environment);
                tripTo.remove(tripTo.size()-1);
                tripFrom = this.CalculateRoute(newRequest.destination,route.stops.get(index+1),environment);
                tripTo.addAll(tripFrom);
                newNodes.addAll(i,tripTo);
            }
        }

        for(int i=0;i<newNodes.size();i++){
            if(newNodes.get(i).x==newStops.get(stopIndex).x && newNodes.get(i).y==newStops.get(stopIndex).y && newNodes.get(i).isRequest){
                cost+=i-newNodes.get(i).request.timeWindow;
                if(i/route.vehicle.speed>newNodes.get(i).request.timeWindow){
                    cost+=3000;
                }
                stopIndex++;
            }
            if(stopIndex==newStops.size())break;
        }
        if(route.vehicle.load+ newRequest.load>route.vehicle.capacity){
            cost+=3000;
        }
        newRequest.insertionCost=cost;

        return newNodes;
    }

    public int RemoveRequest(Request request, Route route, Environment environment){
        int positionAtStops=0;
        int positionAtNodes=0;
        int x;
        int y;
        ArrayList<Node> trip;

        for(int i=0;i<route.stops.size();i++){
            if(route.stops.get(i).x==request.destination.x && route.stops.get(i).y==request.destination.y){
                positionAtStops=i;
                break;
            }
        }

        for(int i=0;i<route.nodes.size();i++){
            if(route.nodes.get(i).x==route.stops.get(positionAtStops-1).x && route.nodes.get(i).y==route.stops.get(positionAtStops-1).y){
                do {
                    positionAtNodes=i;
                    x=route.nodes.get(i).x;
                    y=route.nodes.get(i).y;
                    route.nodes.remove(i);
                }while(x!=route.stops.get(positionAtStops+1).x || y!=route.stops.get(positionAtStops+1).y);
                break;
            }
        }
        trip = this.CalculateRoute(route.stops.get(positionAtStops-1),route.stops.get(positionAtStops+1),environment);
        route.nodes.addAll(positionAtNodes,trip);
        route.stops.remove(positionAtStops);
        route.FixDurations();
        return 1;
    }
}
