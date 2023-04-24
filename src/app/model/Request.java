package app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Request {
    public int id;
    public int x;
    public int y;
    public int load;
    public int coveredLoad;
    public int timeWindow;
    public LocalDateTime startTime;

    public Request(){

    }

    public Request(int id, int x, int y, int load, int timeWindow, LocalDateTime startTime) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.load = load;
        this.timeWindow = timeWindow;
        this.startTime = startTime;
        this.coveredLoad = 0;
    }

    public Request CopyRequest(){
        Request request = new Request();
        request.id = this.id;
        request.x = this.x;
        request.y = this.y;
        request.load = this.load;
        request.timeWindow = this.timeWindow;
        request.startTime = this.startTime;
        request.coveredLoad = this.coveredLoad;
        return request;
    }
}
