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
}
