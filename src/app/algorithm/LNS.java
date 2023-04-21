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
            unrouted.addAll(this.Destroy(newSolution));
            this.Repair(newSolution,unrouted,environment);
            iterations++;
        }
        return solution;
    }

    public Solution ConstructInitialSolution(ArrayList<Request> unrouted, Environment environment){
        Solution solution = new Solution(environment);

        boolean noChanges = false;

        while(!noChanges && unrouted.size()!=0){
            for(int i=0;i<solution.routes.size();i++){
                solution.routes.set(i,this.InsertRequest(unrouted.get(0),solution.routes.get(i),environment));
                solution.routes.get(i).FixDurations();
                unrouted.remove(0);
            }
        }

        return solution;
    }

    public ArrayList<Request> Destroy(Solution solution){
        //number of trips to be removed from the solution
        int n = 3;
        int m = 0;
        ArrayList<Request> unrouted = new ArrayList<>();
        Random random = new Random();
        while(m<n){
            if(random.nextBoolean()){
                // está en duro, dará problemas cuando hayan muchos pedidos iniciales
                //no se están removiendo pedidos como se debería, se están removiendo rutas
                unrouted.add(solution.routes.get(m).requests.get(0));
                solution.routes.remove(m);
            }
            m++;
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
}
