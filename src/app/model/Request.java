package app.model;

import java.time.LocalDateTime;

public class Request {
    public int id;
    public int x;
    public int y;
    public int uncoveredLoad;
    public LocalDateTime startTime;
    //in hours
    public int timeWindow;
    public int clientId;

    public Request(int id, int x, int y, int uncoveredLoad, LocalDateTime startTime, int timeWindow, int clientId) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.uncoveredLoad = uncoveredLoad;
        this.startTime = startTime;
        this.timeWindow = timeWindow;
        this.clientId = clientId;
    }

    public Request(Request request) {
        this.id=request.id;
        this.x=request.x;
        this.y=request.y;
        this.uncoveredLoad= request.uncoveredLoad;
        this.timeWindow=request.timeWindow;
        this.startTime=request.startTime;
        this.clientId=request.clientId;
    }
}
