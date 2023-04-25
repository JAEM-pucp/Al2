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
        int n = 10;
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
            unrouted = this.Destroy(newSolution,newEnvironment);
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

    public ArrayList<Request> Destroy(Solution solution, Environment environment){
        Random random = new Random();
        ArrayList<Request> requests;
        ArrayList<Request> unrouted = new ArrayList<>();
        int result;
        int eliminated=0;
        int requestAmount = solution.GetRequestAmount();
        while(eliminated< requestAmount/2) {
            for (int i = 0; i < solution.routes.size(); i++) {
                requests = solution.routes.get(i).GetRequests(environment);
                for (int j = 0; j < requests.size(); j++) {
                    if (random.nextBoolean()) {
                        result = this.RemoveRequest(requests.get(j),solution.routes.get(i),environment);
                        if(result==1) {
                            unrouted.add(requests.get(j));
                            eliminated++;
                            if (eliminated >= requestAmount / 2) break;
                        }
                    }
                }
                if(eliminated>= requestAmount/2)break;
            }
        }
        return unrouted;
    }

    //repair can modify input with no problem
    public Solution Repair(Solution solution, Environment environment, LocalDateTime currentTime){
        int availableCapacity=0;
        int chosenRequest = 0;
        float bestScore;
        float newScore;
        int insertLocation;
        boolean insertionWasPossible=true;
        Route newRoute;
        Route bestRoute = new Route();
        //repair until all requests are covered or no more insertions are possible
        while(insertionWasPossible && solution.unrouted.size()!=0) {
            insertionWasPossible=false;
            //for each route
            for (int i = 0; i < solution.routes.size(); i++) {
                availableCapacity = solution.routes.get(i).vehicle.capacity-solution.routes.get(i).vehicle.load;
                bestScore = 9999;
                bestRoute = solution.routes.get(i);
                //find the best unrouted candidate to be inserted
                for (int j = 0; j < solution.unrouted.size(); j++) {
                    if(availableCapacity>0){
                        //insert request shouldn't modify input
                        newRoute = InsertRequest(solution.unrouted.get(j), solution.routes.get(i), environment,currentTime);
                        //compare and update
                        if(newRoute!=null) {
                            newScore = newRoute.EvaluateRoute(environment, currentTime);
                            if (newRoute.GetRequestAmount() > bestRoute.GetRequestAmount()) {
                                bestScore = newScore;
                                bestRoute = newRoute;
                                chosenRequest = j;
                            }
                        }
                    }
                }
                if (solution.routes.get(i).GetRequestAmount() < bestRoute.GetRequestAmount() && bestRoute.IsFeasible(currentTime,environment)) {
                    solution.routes.get(i).CopyFrom(bestRoute,environment);
                    solution.unrouted.get(chosenRequest).tripsLeft++;
                    if(solution.unrouted.get(chosenRequest).load-solution.unrouted.get(chosenRequest).coveredLoad<=availableCapacity){
                        solution.routes.get(i).vehicle.load+=solution.unrouted.get(chosenRequest).load-solution.unrouted.get(chosenRequest).coveredLoad;
                        solution.routes.get(i).vehicle.AddRequestLoad(solution.unrouted.get(chosenRequest).x,solution.unrouted.get(chosenRequest).y,solution.unrouted.get(chosenRequest).load-solution.unrouted.get(chosenRequest).coveredLoad);
                        solution.unrouted.get(chosenRequest).coveredLoad=solution.unrouted.get(chosenRequest).load;
                        solution.unrouted.remove(chosenRequest);
                    }
                    else{
                        solution.routes.get(i).vehicle.AddRequestLoad(solution.unrouted.get(chosenRequest).x,solution.unrouted.get(chosenRequest).y,availableCapacity);
                        solution.routes.get(i).vehicle.load=solution.routes.get(i).vehicle.capacity;
                        solution.unrouted.get(chosenRequest).coveredLoad=availableCapacity;
                    }
                    if(solution.unrouted.size()==0)break;
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

    //shouldn't modify input
    public Route InsertRequest(Request request, Route route, Environment environment, LocalDateTime currentTime){
        Route bestRoute = route;
        float bestScore;
        float newScore;
        Route insertedRoute;
        ArrayList<Node> keyNodes;
        int loadLimit;
        boolean changes = false;
        //make a copy of the environment
        Environment newEnvironment = environment.CopyEnvironment();
        //make a copy of the route
        Route newRoute = route.CopyRoute(newEnvironment);
        //take the request from the new environment
        Request newRequest = newEnvironment.GetRequest(request.x, request.y);
        //can't fill if not at depot
        if(newRoute.nodes.get(0).isDepot){
            loadLimit = newRoute.vehicle.capacity;
        }
        else{
            loadLimit = newRoute.vehicle.load;
        }
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

    public int RemoveRequest(Request request, Route route, Environment environment){
        int positionAtStops=0;
        int positionAtNodes=0;
        int x;
        int y;
        int amountRemoved=0;
        ArrayList<Node> trip;

        if(!route.nodes.get(0).isRequest && !route.nodes.get(0).isDepot){
            route.stops.add(0,route.nodes.get(0));
        }

        for(int i=0;i<route.stops.size();i++){
            if(route.stops.get(i).x==request.x && route.stops.get(i).y==request.y){
                positionAtStops=i;
                break;
            }
        }

        if(positionAtStops==0)return 0;
        //for each node
        for(int i=0;i<route.nodes.size();i++){
            if(route.nodes.get(i).x==route.stops.get(positionAtStops-1).x && route.nodes.get(i).y==route.stops.get(positionAtStops-1).y){
                do {
                    positionAtNodes=i;
                    x=route.nodes.get(i).x;
                    y=route.nodes.get(i).y;
                    route.nodes.remove(i);
                    amountRemoved++;
                }while(x!=route.stops.get(positionAtStops+1).x || y!=route.stops.get(positionAtStops+1).y || amountRemoved<=1);
                break;
            }
        }
        trip = this.CalculateRoute(route.stops.get(positionAtStops-1),route.stops.get(positionAtStops+1),environment);
        route.nodes.addAll(positionAtNodes,trip);
        route.stops.remove(positionAtStops);
        route.vehicle.load-=route.vehicle.GetRequestLoad(request.x,request.y);
        route.vehicle.RemoveRequestLoad(request.x,request.y);
        request.tripsLeft=0;
        request.coveredLoad=0;

        if(!route.stops.get(0).isRequest && !route.stops.get(0).isDepot){
            route.stops.remove(0);
        }

        return 1;
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
