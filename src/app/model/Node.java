package app.model;

public class Node {
    public int x;
    public int y;
    public boolean isDepot;
    public boolean isBlocked;

    public Node() {
    }
    public Node(int x, int y, boolean isDepot, boolean isBlocked) {
        this.x = x;
        this.y = y;
        this.isDepot = isDepot;
        this.isBlocked = isBlocked;
    }

    public int CalculateCost(Node node){
        return Math.abs(this.x-node.x)+Math.abs(this.y- node.y);
    }
}
