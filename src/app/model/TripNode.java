package app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TripNode {
    public int id;
    public int x;
    public int y;
    public boolean isDepot;
    public ArrayList<Delivery> deliveries;
    public LocalDateTime visitTime;

    public TripNode(int id, int x, int y, boolean isDepot, LocalDateTime visitTime) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.isDepot = isDepot;
        this.deliveries = new ArrayList<>();
        this.visitTime = visitTime;
    }

    public TripNode(TripNode tripNode) {
        this.id= tripNode.id;
        this.x= tripNode.x;
        this.y= tripNode.y;
        this.isDepot= tripNode.isDepot;
        this.visitTime=tripNode.visitTime;
        this.deliveries = new ArrayList<>();
        for(int i=0;i<tripNode.deliveries.size();i++){
            this.deliveries.add(new Delivery(tripNode.deliveries.get(0)));
        }
    }
    public TripNode(Request request,int load){
        this.id=-1;
        this.x= request.x;
        this.y= request.y;
        this.deliveries = new ArrayList<>();
        this.deliveries.add(new Delivery(request.id,load,request.startTime, request.timeWindow, request.clientId));
    }

    public TripNode(AStarNode aStarNode) {
        this.id = -1;
        this.x=aStarNode.x;
        this.y=aStarNode.y;
        this.isDepot=false;
        this.deliveries=new ArrayList<>();
        this.visitTime=aStarNode.visitTime;
    }
}
