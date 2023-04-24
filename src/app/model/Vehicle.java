package app.model;

public class Vehicle {
    public int id;
    public char type;
    public int capacity;
    public int load;
    public int speed;
    public int cost;
    public boolean isAvailable;

    public Vehicle(){

    }
    public Vehicle CopyVehicle(){
        Vehicle vehicle = new Vehicle();
        vehicle.id = this.id;
        vehicle.type = this.type;
        vehicle.capacity = this.capacity;
        vehicle.load = this.load;
        vehicle.speed = this.speed;
        vehicle.cost = this.cost;
        vehicle.isAvailable = this.isAvailable;
        return vehicle;
    }
    public Vehicle(char type, int capacity, int speed, int cost, int id) {
        this.type = type;
        this.capacity = capacity;
        this.speed = speed;
        this.cost = cost;
        this.load = 0;
        this.id = id;
        this.isAvailable = true;
    }

    public Vehicle(char type, int capacity, int speed, int cost, int load, int id) {
        this.type = type;
        this.capacity = capacity;
        this.speed = speed;
        this.cost = cost;
        this.load = load;
        this.id = id;
        this.isAvailable = true;
    }
}
