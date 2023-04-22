package app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Request {
    public Node destination;
    public int load;
    public ArrayList<LocalDateTime> originTimeWindow;
    public ArrayList<LocalDateTime> destinationTimeWindow;
    public boolean isActive;
    public int insertionCost;
    public int timeWindow;
    public int duration;
    public int distance;
    public int id;

    public Request(){
        this.destination = new Node();
        this.originTimeWindow = new ArrayList<>();
        this.destinationTimeWindow = new ArrayList<>();
        this.isActive = false;
        this.insertionCost=99999;
    }

    public Request(Node destination, int load, int timeWindow, int id) {
        this.destination = destination;
        this.load = load;
        this.timeWindow = timeWindow;
        this.isActive = false;
        this.insertionCost=99999;
        this.id = id;
    }

    public Request(Node destination, int load, int insertionCost, int timeWindow, int duration, int distance, int id) {
        this.destination = destination;
        this.load = load;
        this.insertionCost = insertionCost;
        this.timeWindow = timeWindow;
        this.duration = duration;
        this.distance = distance;
        this.id = id;
    }

    public Request CopyRequest(Environment environment){
        Request request = new Request(this.destination,this.load,this.insertionCost,this.timeWindow,this.duration
                ,this.distance,this.id);

        request.destination=environment.GetNode(this.destination.x,this.destination.y);

        return request;
    }
}
