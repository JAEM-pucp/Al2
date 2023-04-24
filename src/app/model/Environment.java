package app.model;

import java.util.ArrayList;

public class Environment {
    public int width;
    public int height;
    public ArrayList<Node> vertex;
    public ArrayList<Vehicle> vehicles;
    public ArrayList<Request> requests;
    public Environment(){
        this.vertex = new ArrayList<>();
        this.vehicles = new ArrayList<>();
        this.requests = new ArrayList<>();
    }

    public Environment CopyEnvironment(){
        Environment environment = new Environment();
        environment.width = this.width;
        environment.height = this.height;
        for(int i=0;i<this.vertex.size();i++){
            environment.vertex.add(this.vertex.get(i).CopyNode());
        }
        for(int i=0;i<this.vehicles.size();i++){
            environment.vehicles.add(this.vehicles.get(i).CopyVehicle());
        }
        for(int i=0;i<this.requests.size();i++){
            environment.requests.add(this.requests.get(i).CopyRequest());
        }
        return environment;
    }

    public int GetAvailableVehicleAmount(){
        int availableAmount = 0;
        for(int i=0;i<this.vehicles.size();i++){
            if(this.vehicles.get(i).isAvailable){
                availableAmount++;
            }
        }
        return availableAmount;
    }

    public ArrayList<Vehicle> GetAvailableVehicles(){
        ArrayList<Vehicle> vehicles = new ArrayList<>();
        for(int i=0;i<this.vehicles.size();i++){
            if(this.vehicles.get(i).isAvailable){
                vehicles.add(this.vehicles.get(i));
            }
        }
        return vehicles;
    }

    public Request GetRequest(int x, int y){
        for(int i=0;i<this.requests.size();i++){
            if(x==this.requests.get(i).x && y==this.requests.get(i).y){
                return this.requests.get(i);
            }
        }
        return null;
    }

    public Node GetDepot(){
        for(int i=0; i<this.vertex.size();i++){
            if(this.vertex.get(i).isDepot){
                return this.vertex.get(i);
            }
        }
        return null;
    }

    public Environment(int width, int height, int carTotal, int bikeTotal) {
        this.width = width;
        this.height = height;
        this.vertex = new ArrayList<>();
        this.vehicles = new ArrayList<>();
    }

    public Environment(int width, int height, int depotX, int depotY, int carTotal, int carCapacity, int carSpeed, int carCost, int bikeTotal, int bikeCapacity, int bikeSpeed, int bikeCost) {
        this.width=width;
        this.height=height;
        this.vehicles = new ArrayList<>();
        this.requests = new ArrayList<>();
        Vehicle vehicle;
        for(int i=0;i<bikeTotal;i++){
            vehicle = new Vehicle('b',bikeCapacity,bikeSpeed,bikeCost,i);
            this.vehicles.add(vehicle);
        }

        for(int i=0;i<carTotal;i++){
            vehicle = new Vehicle('c',carCapacity,carSpeed,carCost,i+bikeTotal);
            this.vehicles.add(vehicle);
        }

        Node node;
        this.vertex = new ArrayList<>();
        for (int y = 0; y <= height; y++){
            for (int x = 0; x <= width; x++){
                node = new Node(x,y,false,false,false);
                if(depotX==x && depotY==y){
                    node.isDepot=true;
                }
                this.vertex.add(node);
                //   0,0 1,0 2,0 0,1 1,1 2,1
                //    0   1   2   3   4   5
            }
        }
    }

    public Node GetNode(int x, int y){
        if(x>this.width)return null;
        if(y>this.height)return null;
        if(x<0)return null;
        if(y<0)return null;
        return this.vertex.get(x+y*(this.width+1));
    }

    public Vehicle GetVehicle(int id){
        Vehicle vehicle=null;
        for(int i=0;i<this.vehicles.size();i++){
            if(vehicles.get(i).id==id)vehicle = vehicles.get(i);
        }
        return vehicle;
    }

    public int GetCarTotal(){
        int counter=0;
        for(int i=0;i<this.vehicles.size();i++){
            if(this.vehicles.get(i).type=='c'){
                counter++;
            }
        }
        return counter;
    }
    public int SetBlockage(int x, int y){
        this.GetNode(x,y).isBlocked=true;
        return 1;
    }

    public void RemoveRequest(int x, int y){
        for(int i=0;i<this.requests.size();i++){
            if(this.requests.get(i).x == x && this.requests.get(i).y == y){
                this.requests.remove(i);
            }
        }
    }
}
