package app.model;

public class Vehicle {
    public Node startDepot;
    public Node endDepot;
    public int capacity;

    public Vehicle() {
        this.startDepot = new Node();
        this.endDepot = new Node();
    }
}
