package app.model;

import app.algorithm.LNS;

import java.util.ArrayList;

public class Solution {
    public ArrayList<Route> routes;
    public ArrayList<Request> unrouted;
    public boolean started;

    public Solution(int carTotal, int carCapacity, int carSpeed, int carCost, int bikeTotal, int bikeCapacity, int bikeSpeed, int bikeCost,Environment environment){
        this.routes = new ArrayList<>();
        this.unrouted = new ArrayList<>();
        Route route;
        for(int i=0;i<carTotal;i++){
            route = new Route();
            route.id=i;
            route.tripNodes.add(new TripNode(0, environment.depotX, environment.depotY, true,null));
            route.tripNodes.add(new TripNode(1, environment.depotX, environment.depotY, true,null));
            route.vehicle = new Vehicle(i,carCapacity,'c',carSpeed,carCost,true);
            this.routes.add(route);
        }
        for(int i=0;i<bikeTotal;i++){
            route = new Route();
            route.id=i+carTotal;
            route.tripNodes.add(new TripNode(0, environment.depotX, environment.depotY, true,null));
            route.tripNodes.add(new TripNode(1, environment.depotX, environment.depotY, true,null));
            route.vehicle = new Vehicle(i+carTotal,bikeCapacity,'b',bikeSpeed,bikeCost,true);
            this.routes.add(route);
        }
        this.started = false;
    }
    public Solution(Solution initialSolution) {
        this.routes = new ArrayList<>();
        for(int i=0;i<initialSolution.routes.size();i++){
            this.routes.add(new Route(initialSolution.routes.get(i)));
        }
        this.unrouted=new ArrayList<>();
        for(int i=0;i<initialSolution.unrouted.size();i++){
            this.unrouted.add(new Request(initialSolution.unrouted.get(i)));
        }
        this.started= initialSolution.started;
    }

    public int GetUnroutedAmount() {
        return unrouted.size();
    }

    public ArrayList<Request> GetUnroutedRequests() {
        ArrayList<Request> unroutedRequests = new ArrayList<>();
        for(int i=0;i<this.unrouted.size();i++){
            unroutedRequests.add(this.unrouted.get(i));
        }
        return unroutedRequests;
    }

    public void RemoveFromUnrouted(Request request) {
        for(int i=0;i<this.unrouted.size();i++){
            if(this.unrouted.get(i).id==request.id){
                this.unrouted.remove(i);
                break;
            }
        }
    }

    public void UpdateRoute(Route newRoute) {
        for(int i=0;i<this.routes.size();i++){
            if(this.routes.get(i).id== newRoute.id){
                this.routes.set(i,new Route(newRoute));
                break;
            }
        }
    }

    public ArrayList<Request> GetRequests() {
        ArrayList<Request> requests = new ArrayList<>();
        boolean alreadyIn;
        for(int i=0;i<this.routes.size();i++){
            for(int j=0;j<this.routes.get(i).tripNodes.size();j++){
                for(int k=0;k<this.routes.get(i).tripNodes.get(j).deliveries.size();k++){
                    alreadyIn=false;
                    for(int l=0;l< requests.size();l++){
                        if(requests.get(l).id==this.routes.get(i).tripNodes.get(j).deliveries.get(k).requestId){
                            alreadyIn=true;
                            requests.get(l).uncoveredLoad+=this.routes.get(i).tripNodes.get(j).deliveries.get(k).load;
                        }
                    }
                    if(!alreadyIn){
                        requests.add(new Request(this.routes.get(i).tripNodes.get(j).deliveries.get(k).requestId,
                                this.routes.get(i).tripNodes.get(j).x,this.routes.get(i).tripNodes.get(j).y,
                                this.routes.get(i).tripNodes.get(j).deliveries.get(k).load,
                                this.routes.get(i).tripNodes.get(j).deliveries.get(k).startTime,
                                this.routes.get(i).tripNodes.get(j).deliveries.get(k).timeWindow,
                                this.routes.get(i).tripNodes.get(j).deliveries.get(k).clientId));
                    }
                }
            }
        }
        return requests;
    }

    public Route GetRoute(int id) {
        Route route=null;
        for(int i=0;i<this.routes.size();i++){
            if(this.routes.get(i).id==id){
                route=this.routes.get(i);
                break;
            }
        }
        return route;
    }

    public ArrayList<Request> GetOrderedUnroutedRequests() {
        //make a simple copy
        ArrayList<Request> unrouted = new ArrayList<>();
        for(int i=0;i<this.unrouted.size();i++){
            unrouted.add(this.unrouted.get(i));
        }
        unrouted.sort(new RequestComparator());
        return unrouted;
    }

    public void RemoveRequest(int id, Environment environment) {
        boolean alreadyIn;
        LNS lns = new LNS();
        boolean checkNextRoute=false;
        for(int i=0;i<this.routes.size();i++){
            checkNextRoute=false;
            for(int j=0;j<this.routes.get(i).tripNodes.size();j++){
                for(int k=0;k<this.routes.get(i).tripNodes.get(j).deliveries.size();k++){
                    if(this.routes.get(i).tripNodes.get(j).deliveries.get(k).requestId==id){
                        alreadyIn=false;
                        for(int l=0;l<this.unrouted.size();l++){
                            if(this.unrouted.get(l).id==id){
                                this.unrouted.get(l).uncoveredLoad+=this.routes.get(i).tripNodes.get(j).deliveries.get(k).load;
                                alreadyIn=true;
                            }
                        }
                        if(!alreadyIn){
                            this.unrouted.add(new Request(id,this.routes.get(i).tripNodes.get(j).x,
                                    this.routes.get(i).tripNodes.get(j).y,
                                    this.routes.get(i).tripNodes.get(j).deliveries.get(k).load,
                                    this.routes.get(i).tripNodes.get(j).deliveries.get(k).startTime,
                                    this.routes.get(i).tripNodes.get(j).deliveries.get(k).timeWindow,
                                    this.routes.get(i).tripNodes.get(j).deliveries.get(k).clientId));
                        }
                        this.routes.get(i).tripNodes.get(j).deliveries.remove(k);
                        if(this.routes.get(i).tripNodes.get(j).deliveries.size()==0){
                            this.routes.set(i,lns.CalculateRouteFromStops(this.routes.get(i).GetStops(),this.routes.get(i).id,this.routes.get(i).vehicle,environment));
                        }
                        checkNextRoute=true;
                    }
                    if(checkNextRoute){
                        break;
                    }
                }
                if(checkNextRoute){
                    break;
                }
            }
        }
    }
}
