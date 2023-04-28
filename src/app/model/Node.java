package app.model;

public class Node {
    public int x;
    public int y;
    public boolean isDepot;
    public boolean isBlocked;
    public boolean isRequest;

    public Node() {
    }

    public Node CopyNode(){
        Node node = new Node();
        node.x = this.x;
        node.y = this.y;
        node.isDepot = this.isDepot;
        node.isBlocked = this.isBlocked;
        node.isRequest = this.isRequest;
        return node;
    }
    public Node(int x, int y, boolean isDepot, boolean isBlocked, boolean isRequest) {
        this.x = x;
        this.y = y;
        this.isDepot = isDepot;
        this.isBlocked = isBlocked;
        this.isRequest = isRequest;
    }

    public int CalculateCost(Node node){
        return Math.abs(this.x-node.x)+Math.abs(this.y- node.y);
    }

    public int CalculateScore(Node node){
        int add = 0;
        if(this.isBlocked && !this.isRequest)add=9999;
        return Math.abs(this.x-node.x)+Math.abs(this.y- node.y)+add;
    }
}
