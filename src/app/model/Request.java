package app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Request {
    public Node origin;
    public Node destination;
    public int load;
    public ArrayList<LocalDateTime> originTimeWindow;
    public ArrayList<LocalDateTime> destinationTimeWindow;
    public boolean isActive;
    public int insertionCost;
    public int timeWindow;
    public int currentDuration;

    public Request(){
        this.origin = new Node();
        this.destination = new Node();
        this.originTimeWindow = new ArrayList<>();
        this.destinationTimeWindow = new ArrayList<>();
        this.isActive = false;
        this.insertionCost=99999;
    }
}
