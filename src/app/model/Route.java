package app.model;

import java.util.ArrayList;

public class Route {
    public ArrayList<Node> nodes;
    public ArrayList<Node> stops;
    public Vehicle vehicle;

    public Route() {
        this.nodes = new ArrayList<>();
        this.stops = new ArrayList<>();
    }

    public int GetRequestAmount(){
        int amount=0;
        for(int i=0;i<this.stops.size();i++){
            if(this.stops.get(i).isRequest)amount++;
        }
        return amount;
    }

    public int FixDurations(){
        int stopIndex = 0;
        for(int i=0;i<this.nodes.size();i++){
            if(this.nodes.get(i).x==this.stops.get(stopIndex).x && this.nodes.get(i).y==this.stops.get(stopIndex).y){
                if(this.nodes.get(i).isRequest){
                    this.nodes.get(i).request.distance=i;
                    this.nodes.get(i).request.duration=(int)(((float)i/this.vehicle.speed)*60);
                }
                stopIndex++;
            }
            if(stopIndex==this.stops.size())break;
        }
        return 1;
    }

    public ArrayList<Request> GetRequests(){
        ArrayList<Request> requests = new ArrayList<>();
        for(int i=0;i<this.stops.size();i++){
            if(this.stops.get(i).isRequest){
                requests.add(this.stops.get(i).request);
            }
        }
        return requests;
    }

    public float EvaluateRoute(){
        float costAvg=0;
        int requestAmount = this.GetRequestAmount();
        ArrayList<Request> requests = this.GetRequests();
        for(int i=0;i<requestAmount;i++){
            //duration = x% timewindow
            costAvg+=100*requests.get(i).duration/requests.get(i).timeWindow;
        }
        return costAvg/requestAmount;
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
}
