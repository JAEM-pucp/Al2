package app.model;

public class Vehicle {
    public char type;
    public int capacity;
    public int speed;
    public int cost;
    public Vehicle(char type, int capacity, int speed, int cost) {
        this.type = type;
        this.capacity = capacity;
        this.speed = speed;
        this.cost = cost;
    }
}
