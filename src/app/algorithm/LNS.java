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
        int bestUnroutedAmount =0;
        Output output = new Output();
        Solution bestSolution;
        Environment bestEnvironment;
        float newScore;
        float bestScore;
        ArrayList<Request> unrouted;
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
            unrouted = this.Destroy(newSolution,newEnvironment,destroyN);
            //add the new unrouted to the previous unrouted requests
            newSolution.unrouted.addAll(unrouted);
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
                                availableCapacity-=availableRoutes.get(i).vehicle.GetRequestLoad(unrouted.get(j).x,unrouted.get(j).y);
                                availableRoutes.get(i).vehicle.load+=availableRoutes.get(i).vehicle.GetRequestLoad(unrouted.get(j).x,unrouted.get(j).y);

                                unrouted.remove(j);
                            }
                            else{
                                availableRoutes.get(i).vehicle.AddRequestLoad(unrouted.get(j).x,unrouted.get(j).y,availableCapacity);
                                unrouted.get(j).coveredLoad+=availableCapacity;
                                availableCapacity=0;
                                availableRoutes.get(i).vehicle.load=availableRoutes.get(i).vehicle.capacity;
                            }
                        }
                        else{
                            j++;
                        }
                        if(availableCapacity==0)break;
                    }
                    /*
                    newRoute = this.InsertRequest(unrouted.get(0),solution.routes.get(i),environment,currentTime);
                    if(newRoute!=null && newRoute.IsFeasible(currentTime,environment)){
                        availableRoutes.get(i).CopyFrom(newRoute,environment);
                        unrouted.get(0).tripsLeft++;
                        if(unrouted.get(0).load-unrouted.get(0).coveredLoad<=availableCapacity){
                            availableRoutes.get(i).vehicle.load+=unrouted.get(0).load-unrouted.get(0).coveredLoad;
                            availableRoutes.get(i).vehicle.AddRequestLoad(unrouted.get(0).x,unrouted.get(0).y,unrouted.get(0).load-unrouted.get(0).coveredLoad);
                            unrouted.get(0).coveredLoad=unrouted.get(0).load;
                            unrouted.remove(0);
                        }
                        else{
                            availableRoutes.get(i).vehicle.AddRequestLoad(unrouted.get(0).x,unrouted.get(0).y,availableCapacity);
                            unrouted.get(0).coveredLoad=availableCapacity;
                            availableRoutes.get(i).vehicle.load+=availableRoutes.get(i).vehicle.capacity;
                        }
                        if(unrouted.size()==0)break;
                    }*/
                }

            }
        }
        solution.unrouted.addAll(unrouted);

        return solution;
    }

    public ArrayList<Request> Destroy(Solution solution, Environment environment, int n){
        Random random = new Random();
        ArrayList<Request> requests;
        ArrayList<Request> unrouted = new ArrayList<>();
        ArrayList<Route> availableRoutes = solution.GetAvailableRoutes();
        boolean result;
        int eliminated=0;
        int requestAmount = solution.GetRequestAmount();

        //while stop condition not met
        while(eliminated< requestAmount/n) {

            //for every available route
            for (int i = 0; i < availableRoutes.size(); i++) {
                requests = availableRoutes.get(i).GetRequests(environment);

                //for every request in the route
                for (int j = 0; j < requests.size(); j++) {

                    //randomly choose to remove request
                    if (random.nextBoolean()) {

                        //should modify
                        //nodes, stops, vehicle, covered load
                        result = this.RemoveRequest(requests.get(j),solution.routes.get(i),environment);

                        //if it was removed successfully
                        if(result) {
                            unrouted.remove(requests.get(j));
                            unrouted.add(requests.get(j));
                            eliminated++;

                            //if all requests were removed from the route
                            /*if(availableRoutes.get(i).GetRequests(environment).size()==0){
                                availableRoutes.get(i).startTime = null;
                            }*/

                            if (eliminated >= requestAmount / n) break;
                        }
                    }
                }
                if(eliminated>= requestAmount/n)break;
            }
        }
        return unrouted;
    }

    public boolean RemoveRequest(Request request, Route route, Environment environment){
        int x;
        int y;
        int nextStopX=0;
        int nextStopY=0;
        int previousStopX=0;
        int previousStopY=0;
        int patchIndex = 0;
        int requestLoad;
        Node previousStop = new Node();
        Node nextStop = new Node();
        ArrayList<Node> trip;

        //if we're already at request, then it can't be removed
        if(route.nodes.get(0).x == request.x && route.nodes.get(0).y == request.y){
            return false;
        }

        //should modify nodes stops vehicles coveredload

        requestLoad = route.vehicle.GetRequestLoad(request.x,request.y);

        request.coveredLoad-=requestLoad;
        route.vehicle.RemoveRequestLoad(request.x,request.y);

        //temporarily add initial node as first stop if not at depot
        if(!route.nodes.get(0).isDepot && !route.nodes.get(0).isRequest){
            route.stops.add(0,route.nodes.get(0));
        }

        //getting the next and previous stop coordinates and nodes and removing the request from the stops
        for(int i=1;i<route.stops.size();i++){
            if(route.stops.get(i).x == request.x && route.stops.get(i).y == request.y){
                nextStop = route.stops.get(i+1);
                nextStopX = route.stops.get(i+1).x;
                nextStopY = route.stops.get(i+1).y;
                previousStop = route.stops.get(i-1);
                previousStopX = route.stops.get(i-1).x;
                previousStopY = route.stops.get(i-1).y;
                route.stops.remove(i);
                break;
            }
        }

        //removing nodes
        for(int i=0;i<route.nodes.size();i++){
            if(route.nodes.get(i).x == previousStopX && route.nodes.get(i).y == previousStopY){
                patchIndex = i;
                while(true){
                    x = route.nodes.get(i).x;
                    y = route.nodes.get(i).y;
                    route.nodes.remove(i);
                    if(x==nextStopX && y==nextStopY){
                        break;
                    }
                }
                break;
            }
        }

        //calculating patch trip
        trip = this.CalculateRoute(previousStop,nextStop,environment);

        //inseting patch trip
        route.nodes.addAll(patchIndex,trip);

        //remove temporary stop
        if(!route.nodes.get(0).isDepot && !route.nodes.get(0).isRequest){
            route.stops.remove(0);
        }

        return true;
    }

    //repair can modify input with no problem
    public Solution Repair(Solution solution, Environment environment, LocalDateTime currentTime){
        int availableCapacity;
        int chosenRequestIndex = 0;
        int requestLoad;
        int bestRequestLoad = 0;
        float bestScore;
        float newScore=9999;
        boolean changes;
        boolean insertionWasPossible=true;
        Route newRoute;
        Route bestRoute;
        Request chosenRequest = new Request();
        ArrayList<Route> availableRoutes = solution.GetAvailableRoutes();

        //repair until all requests are covered or no more insertions are possible
        while(insertionWasPossible && solution.unrouted.size()!=0) {
            insertionWasPossible=false;

            //for each route
            for (int i = 0; i < availableRoutes.size(); i++) {
                availableCapacity = availableRoutes.get(i).vehicle.capacity-availableRoutes.get(i).vehicle.load;
                bestScore = 9999;
                bestRoute = availableRoutes.get(i);
                changes = false;

                //if route can accept requests
                if(availableCapacity!=0){

                    //find the best unrouted candidate to be inserted
                    for (int j = 0; j < solution.unrouted.size(); j++) {
                        if(solution.unrouted.get(j).load - solution.unrouted.get(j).coveredLoad<=availableCapacity) {
                            requestLoad = solution.unrouted.get(j).load - solution.unrouted.get(j).coveredLoad;
                        }
                        else {
                            requestLoad = availableCapacity;
                        }

                        //inserts request at its best position in the route
                        //insert request shouldn't modify input
                        //outputs modified nodes and stops, checks feasibility
                        newRoute = InsertRequest(solution.unrouted.get(j), availableRoutes.get(i), environment, currentTime);
                        if(newRoute != null){
                            newScore = newRoute.EvaluateRoute(environment,currentTime);
                        }

                        //update the best result
                        if (newRoute != null && newScore<bestScore) {
                            bestScore = newScore;
                            bestRoute = newRoute;
                            chosenRequestIndex = j;
                            chosenRequest = solution.unrouted.get(j);
                            changes = true;
                            bestRequestLoad = requestLoad;
                            insertionWasPossible=true;
                        }
                    }

                    //copy the best result into the route if changes were made
                    if(changes){
                        solution.routes.get(i).CopyFrom(bestRoute,environment);
                        solution.routes.get(i).vehicle.AddRequestLoad(chosenRequest.x,chosenRequest.y,bestRequestLoad);
                        solution.routes.get(i).vehicle.load+=bestRequestLoad;
                        chosenRequest.tripsLeft++;
                        chosenRequest.coveredLoad+=bestRequestLoad;
                        if(chosenRequest.load==chosenRequest.coveredLoad){
                            solution.unrouted.remove(chosenRequestIndex);
                            if(solution.unrouted.size()==0)break;
                        }
                    }
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
        float bestScore;
        float newScore;
        Route insertedRoute;
        ArrayList<Node> keyNodes;
        boolean changes = false;
        Environment newEnvironment;
        Route newRoute;
        Request newRequest;

        //make a copy of the environment
        newEnvironment = environment.CopyEnvironment();

        //make a copy of the route
        newRoute = route.CopyRoute(newEnvironment);

        //take the request from the new environment
        newRequest = newEnvironment.GetRequest(request.x, request.y);

        //calculate score to compare to
        bestScore = route.EvaluateRoute(environment,currentTime);
        //get the key nodes, these will stay in the new routes
        keyNodes = newRoute.GetKeyNodes();
        //for each key node
        for(int i=0;i< keyNodes.size();i++){
            //insert node after key node
            //shouldn't modify inputs
            if(newRequest.x==keyNodes.get(i).x && newRequest.y==keyNodes.get(i).y){
                return newRoute;
            }
            else {
                insertedRoute = this.InsertRequestAt(newRequest, keyNodes.get(i), newRoute, newEnvironment);
                newScore = insertedRoute.EvaluateRoute(environment, currentTime);
                //if(newScore<bestScore && insertedRoute.IsFeasible(newEnvironment)){
                if (bestRoute.stops.size() <= insertedRoute.stops.size() && insertedRoute.IsFeasible(currentTime, newEnvironment)) {
                    bestScore = newScore;
                    bestRoute = insertedRoute;
                    changes = true;
                }
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
