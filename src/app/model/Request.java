package app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Request {
    public int x;
    public int y;
    public int load;
    public int coveredLoad;
    public int tripsLeft;
    public int timeWindow;
    public LocalDateTime startTime;
    public int clientId;

    public Request(){

    }

    public Request(int x, int y, int load, int timeWindow, LocalDateTime startTime, int clientId) {
        this.x = x;
        this.y = y;
        this.load = load;
        this.timeWindow = timeWindow;
        this.startTime = startTime;
        this.coveredLoad = 0;
        this.tripsLeft = 0;
        this.clientId = clientId;
    }

    public Request CopyRequest(){
        Request request = new Request();
        request.x = this.x;
        request.y = this.y;
        request.load = this.load;
        request.timeWindow = this.timeWindow;
        request.startTime = this.startTime;
        request.coveredLoad = this.coveredLoad;
        request.tripsLeft = this.tripsLeft;
        request.clientId = this.clientId;
        return request;
    }
}
