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
            if(this.nodes.get(i).x==this.stops.get(stopIndex).x && this.nodes.get(i).y==this.stops.get(stopIndex).y && this.nodes.get(i).isRequest){
                this.nodes.get(i).request.distance=i;
                this.nodes.get(i).request.duration=i/this.vehicle.speed;
                stopIndex++;
            }
            if(stopIndex==this.stops.size())break;
        }
        return 1;
    }
}
