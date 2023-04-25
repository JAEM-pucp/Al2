package app.model;

import java.util.ArrayList;

public class Vehicle {
    public int id;
    public char type;
    public int capacity;
    public int load;
    public int speed;
    public int cost;
    public ArrayList<RequestLoad> requestLoads;
    public boolean isAvailable;

    public Vehicle(){
        this.requestLoads = new ArrayList<>();
    }

    public int AddRequestLoad(int x, int y, int load){
        RequestLoad requestLoad = new RequestLoad();
        requestLoad.nodeX = x;
        requestLoad.nodeY = y;
        requestLoad.load = load;
        this.requestLoads.add(requestLoad);
        return 1;
    }

    public int GetRequestLoad(int x, int y){
        int load =9999;
        for(int i=0;i<this.requestLoads.size();i++){
            if (this.requestLoads.get(i).nodeX == x && this.requestLoads.get(i).nodeY == y){
                return this.requestLoads.get(i).load;
            }
        }
        return load;
    }

    public int RemoveRequestLoad(int x, int y){
        for(int i=0;i<this.requestLoads.size();i++){
            if (this.requestLoads.get(i).nodeX == x && this.requestLoads.get(i).nodeY == y){
                this.requestLoads.remove(i);
            }
        }
        return 1;
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
        RequestLoad requestLoad;
        for(int i=0;i<this.requestLoads.size();i++){
            requestLoad = new RequestLoad();
            requestLoad.nodeX=this.requestLoads.get(i).nodeX;
            requestLoad.nodeY=this.requestLoads.get(i).nodeY;
            requestLoad.load=this.requestLoads.get(i).load;
            vehicle.requestLoads.add(requestLoad);
        }
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
        this.requestLoads = new ArrayList<>();
    }

    public Vehicle(char type, int capacity, int speed, int cost, int load, int id) {
        this.type = type;
        this.capacity = capacity;
        this.speed = speed;
        this.cost = cost;
        this.load = load;
        this.id = id;
        this.isAvailable = true;
        this.requestLoads = new ArrayList<>();
    }
}
