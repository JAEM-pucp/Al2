package app.model;

import java.time.LocalDateTime;

public class Delivery {
    public int requestId;
    public int load;
    public LocalDateTime startTime;
    public int timeWindow;
    public int clientId;

    public Delivery(int requestId, int load, LocalDateTime startTime, int timeWindow, int clientId) {
        this.requestId = requestId;
        this.load = load;
        this.startTime = startTime;
        this.timeWindow = timeWindow;
        this.clientId = clientId;
    }

    public Delivery(Delivery delivery) {
        this.requestId=delivery.requestId;
        this.load=delivery.load;
        this.startTime=delivery.startTime;
        this.timeWindow=delivery.timeWindow;
        this.clientId=delivery.clientId;
    }
}
