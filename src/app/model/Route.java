package app.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Route {
    public ArrayList<Node> nodes;
    public ArrayList<Node> stops;
    public Vehicle vehicle;

    public Route() {
        this.nodes = new ArrayList<>();
        this.stops = new ArrayList<>();
    }
    public Route(Node depot, Vehicle vehicle) {
        this.nodes = new ArrayList<>();
        this.nodes.add(depot);
        this.nodes.add(depot);
        this.stops = new ArrayList<>();
        this.stops.add(depot);
        this.stops.add(depot);
        this.vehicle = vehicle;
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
        return route;
    }

    public ArrayList<Node> GetKeyNodes(){
        ArrayList<Node> keyNodes = new ArrayList<>();
        if(!this.stops.get(0).isDepot){
            keyNodes.add(this.nodes.get(0));
        }
        keyNodes.addAll(this.stops);
        keyNodes.remove(keyNodes.size()-1);
        return keyNodes;
    }

    public boolean IsFeasible(LocalDateTime currentTime, Environment environment){
        int stopIndex = 0;
        int totalLoad = 0;
        boolean isFeasible = true;
        for(int i=0;i<this.nodes.size();i++){
            if(this.nodes.get(i).x==this.stops.get(i).x && this.nodes.get(i).y==this.stops.get(i).y){
                stopIndex++;
                if(this.nodes.get(i).isRequest){
                    totalLoad+=environment.GetRequest(this.nodes.get(i).x,this.nodes.get(i).y).load;
                    //
                    if((int)ChronoUnit.MINUTES.between(environment.GetRequest(this.nodes.get(i).x,this.nodes.get(i).y).startTime,currentTime)+(int)((float)i*60/(float)this.vehicle.speed)>environment.GetRequest(this.nodes.get(i).x,this.nodes.get(i).y).timeWindow){
                        isFeasible = false;
                    }
                }
            }
        }
        if(totalLoad>this.vehicle.load){
            isFeasible = false;
        }
        return isFeasible;
    }

    public boolean IsFeasible(Environment environment){
        int stopIndex = 0;
        int totalLoad = 0;
        boolean isFeasible = true;
        for(int i=0;i<this.nodes.size();i++){
            if(this.nodes.get(i).x==this.stops.get(stopIndex).x && this.nodes.get(i).y==this.stops.get(stopIndex).y){
                stopIndex++;
                if(this.nodes.get(i).isRequest){
                    totalLoad+=environment.GetRequest(this.nodes.get(i).x,this.nodes.get(i).y).load;
                    //
                    if((int)((float)i*60/(float)this.vehicle.speed)>environment.GetRequest(this.nodes.get(i).x,this.nodes.get(i).y).timeWindow){
                        isFeasible = false;
                    }
                }
                if(stopIndex==this.stops.size()-1)break;
            }
        }
        if(totalLoad>this.vehicle.capacity){
            isFeasible = false;
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
        int stopsIndex=0;
        float score=0;
        int requestAmount=0;
        for(int i=0;i<this.nodes.size();i++){
            if(this.nodes.get(i).x==this.stops.get(stopsIndex).x && this.nodes.get(i).y==this.stops.get(stopsIndex).y){
                stopsIndex++;
                if(this.nodes.get(i).isRequest){
                    score+=(float)environment.GetRequest(this.nodes.get(i).x,this.nodes.get(i).y).timeWindow-ChronoUnit.MINUTES.between(environment.GetRequest(this.nodes.get(i).x,this.nodes.get(i).y).startTime,currentTime)+(int)((float)i*60/(float)this.vehicle.speed);
                    requestAmount++;
                }
                if(stopsIndex==this.stops.size())break;
            }
        }
        return score/requestAmount;
    }

}
