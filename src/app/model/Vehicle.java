package app.model;

public class Vehicle {
    public char type;
    public int capacity;
    public int speed;
    public int cost;
    public int load;
    public int id;
    public Vehicle(char type, int capacity, int speed, int cost, int id) {
        this.type = type;
        this.capacity = capacity;
        this.speed = speed;
        this.cost = cost;
        this.load = 0;
        this.id = id;
    }
}
