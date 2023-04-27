package app.model;

import java.time.LocalDateTime;

public class Blockage {
    public int id;
    public int x;
    public int y;
    public LocalDateTime startTime;
    public LocalDateTime endTime;

    public Blockage(){

    }

    public Blockage(int x, int y, LocalDateTime startTime, LocalDateTime endTime) {
        this.x = x;
        this.y = y;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
