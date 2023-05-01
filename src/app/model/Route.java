package app.model;

import java.util.ArrayList;

public class Route {
    public int id;
    public ArrayList<TripNode> tripNodes;
    public Vehicle vehicle;

    public Route(){
        this.tripNodes = new ArrayList<>();
    }
    public Route(Route route) {
        this.id = route.id;
        this.vehicle = new Vehicle(route.vehicle);
        this.tripNodes = new ArrayList<>();
        for(int i=0;i<route.tripNodes.size();i++){
            this.tripNodes.add(new TripNode(route.tripNodes.get(i)));
        }
    }

    public Route(int routeId, ArrayList<TripNode> trip, Vehicle vehicle) {
        this.id = routeId;
        this.tripNodes = new ArrayList<>();
        for(int i=0;i<trip.size();i++){
            tripNodes.add(new TripNode(trip.get(i)));
        }
        this.vehicle = new Vehicle(vehicle);
    }

    public int GetAvailableCapacity() {
        if(this.tripNodes.get(0).isDepot) {
            return this.vehicle.capacity - this.GetLoad();
        }else{
            return this.vehicle.load - this.GetLoad();
        }
    }

    public int GetLoad(){
        int load=0;
        for(int i=0;i< this.tripNodes.size();i++){
            for(int j=0;j<this.tripNodes.get(i).deliveries.size();j++){
                load+=this.tripNodes.get(i).deliveries.get(j).load;
            }
        }
        return load;
    }

    public ArrayList<TripNode> GetStops() {
        ArrayList<TripNode> stops = new ArrayList<>();
        stops.add(this.tripNodes.get(0));
        for(int i=1;i<this.tripNodes.size();i++){
            if(this.tripNodes.get(i).deliveries.size()>0 || this.tripNodes.get(i).isDepot){
                stops.add(this.tripNodes.get(i));
            }
        }
        return stops;
    }

    public int GetCoverableLoad(Request request) {
        int availableCapacity = this.GetAvailableCapacity();
        if(availableCapacity>= request.uncoveredLoad){
            return request.uncoveredLoad;
        }else{
            return availableCapacity;
        }
    }

    public int GetDeliveryAmount() {
        int deliveryAmount=0;
        for(int i=0;i<this.tripNodes.size();i++){
            deliveryAmount+=tripNodes.get(i).deliveries.size();
        }
        return deliveryAmount;
    }
}
