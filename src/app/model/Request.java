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

    public Request(){
        this.destination = new Node();
        this.originTimeWindow = new ArrayList<>();
        this.destinationTimeWindow = new ArrayList<>();
        this.isActive = false;
        this.insertionCost=99999;
    }

    public Request(Node destination, int load, int timeWindow) {
        this.destination = destination;
        this.load = load;
        this.timeWindow = timeWindow;
        this.isActive = false;
        this.insertionCost=99999;
    }
}
