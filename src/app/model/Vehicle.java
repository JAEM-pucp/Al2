package app.model;

public class Vehicle {
    public int id;
    public int capacity;
    public int load;
    public char type;
    public int speed;
    public int cost;
    public boolean isAvailable;

    public Vehicle(int id, int capacity, char type, int speed, int cost, boolean isAvailable) {
        this.id = id;
        this.capacity = capacity;
        this.load = 0;
        this.type = type;
        this.speed = speed;
        this.cost = cost;
        this.isAvailable = isAvailable;
    }

    public Vehicle(Vehicle vehicle) {
        this.id = vehicle.id;
        this.capacity=vehicle.capacity;
        this.load = vehicle.load;
        this.type= vehicle.type;
        this.cost= vehicle.cost;
        this.speed= vehicle.speed;
        this.isAvailable= vehicle.isAvailable;;
    }
}
