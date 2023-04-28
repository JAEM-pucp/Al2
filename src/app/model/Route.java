package app.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Route {
    public ArrayList<Node> nodes;
    public ArrayList<Node> stops;
    public Vehicle vehicle;
    public LocalDateTime startTime;

    public Route() {
        this.nodes = new ArrayList<>();
        this.stops = new ArrayList<>();
        this.startTime = null;
    }
    public Route(Node depot, Vehicle vehicle) {
        this.nodes = new ArrayList<>();
        this.nodes.add(depot);
        this.nodes.add(depot);
        this.stops = new ArrayList<>();
        this.stops.add(depot);
        this.stops.add(depot);
        this.vehicle = vehicle;
        this.startTime = null;
    }
    public Route CopyRoute(Environment environment){
        Route route = new Route();
        for(int i=0;i<this.nodes.size();i++){
            route.nodes.add(environment.GetNode(this.nodes.get(i).x,this.nodes.get(i).y));
        }
        for(int i=0;i<this.stops.size();i++){
            route.stops.add(environment.GetNode(this.stops.get(i).x,this.stops.get(i).y));
        }
        route.vehicle = environment.GetVehicle(this.vehicle.id);
        route.startTime = this.startTime;
        return route;
    }

    public ArrayList<Node> GetKeyNodes(){
        ArrayList<Node> keyNodes = new ArrayList<>();
        if(!this.stops.get(0).isDepot && !this.stops.get(0).isRequest){
            keyNodes.add(this.nodes.get(0));
        }
        keyNodes.addAll(this.stops);
        keyNodes.remove(keyNodes.size()-1);
        return keyNodes;
    }

    public boolean IsFeasible(LocalDateTime currentTime, Environment environment){
        boolean isFeasible = true;
        long pendingTime;
        LocalDateTime deadline;
        Request request;
        for(int i=0;i<this.stops.size();i++){
            if(this.stops.get(i).isRequest){
                for(int j=0;j<this.nodes.size();j++){
                    if(this.nodes.get(j).x == this.stops.get(i).x && this.nodes.get(j).y == this.stops.get(i).y){
                        pendingTime=(j*60)/this.vehicle.speed;
                        request = environment.GetRequest(this.stops.get(i).x,this.stops.get(i).y);
                        deadline = request.startTime.plusHours(environment.GetRequest(this.stops.get(i).x,this.stops.get(i).y).timeWindow);
                        if(currentTime.plusMinutes(pendingTime+1).isAfter(deadline)){
                            isFeasible = false;
                        }
                        break;
                    }
                }
            }
        }
        return isFeasible;
    }

    public int CopyFrom(Route route, Environment environment){
        this.nodes = new ArrayList<>();
        for(int i=0;i<route.nodes.size();i++){
            this.nodes.add(environment.GetNode(route.nodes.get(i).x,route.nodes.get(i).y));
        }
        this.stops = new ArrayList<>();
        for(int i=0;i<route.stops.size();i++){
            this.stops.add(environment.GetNode(route.stops.get(i).x,route.stops.get(i).y));
        }
        this.vehicle = environment.GetVehicle(route.vehicle.id);
        this.startTime = route.startTime;
        return 1;
    }
    public int GetRequestAmount(){
        int amount=0;
        for(int i=0;i<this.stops.size();i++){
            if(this.stops.get(i).isRequest)amount++;
        }
        return amount;
    }

    public ArrayList<Request> GetRequests(Environment environment){
        ArrayList<Request> requests = new ArrayList<>();
        for(int i=0;i<this.stops.size();i++){
            if(this.stops.get(i).isRequest){
                requests.add(environment.GetRequest(this.stops.get(i).x,this.stops.get(i).y));
            }
        }
        return requests;
    }

    public float EvaluateRoute(Environment environment, LocalDateTime currentTime){
        float score=0;
        int requestAmount=0;
        LocalDateTime startTime;

        for(int i=0;i<this.stops.size();i++){
            if(this.stops.get(i).isRequest){
                for(int j=0;j<this.nodes.size();j++){
                    if(this.nodes.get(j).x == this.stops.get(i).x && this.nodes.get(j).y == this.stops.get(i).y){
                        startTime = environment.GetRequest(this.nodes.get(j).x,this.nodes.get(j).y).startTime;
                        score+=ChronoUnit.MINUTES.between(startTime,currentTime)+(i*60)/this.vehicle.speed;
                        break;
                    }
                }
                requestAmount++;
            }
        }
        return score/requestAmount;
    }

}
