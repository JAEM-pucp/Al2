package app.model;

import java.time.LocalDateTime;

public class AStarNode {
    public int x;
    public int y;
    public int f;
    public int g;
    public int h;
    public AStarNode parent;
    public LocalDateTime visitTime;
    public AStarNode(TripNode tripNode, int manhattanDistance, LocalDateTime initialTime){
        this.x= tripNode.x;;
        this.y= tripNode.y;
        this.g=0;
        this.h=manhattanDistance;
        this.f = this.g+this.h;
        this.parent = null;
        this.visitTime=initialTime;
    }

    public AStarNode(int x, int y, int g, int h, AStarNode parent, LocalDateTime visitTime) {
        this.x = x;
        this.y = y;
        this.g = g;
        this.h = h;
        this.f=this.g+this.h;
        this.parent = parent;
        this.visitTime = visitTime;
    }
}
