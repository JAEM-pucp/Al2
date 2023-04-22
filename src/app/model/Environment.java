package app.model;

import java.util.ArrayList;

public class Environment {
    public ArrayList<Node> vertex;
    public int width;
    public int height;
    public ArrayList<Vehicle> vehicles;
    public int carTotal;
    public int carAvailable;
    public int carInUse;
    public int bikeTotal;
    public int bikeAvailable;
    public int bikeInUse;
    public Node depot;
    public Environment(){
        this.vertex = new ArrayList<>();
        this.vehicles = new ArrayList<>();
    }
    public Environment(int width, int height, int depotX, int depotY, int carTotal, int carCapacity, int carSpeed, int carCost, int bikeTotal, int bikeCapacity, int bikeSpeed, int bikeCost) {
        this.carTotal=carTotal;
        this.bikeTotal=bikeTotal;
        this.width=width;
        this.height=height;
        this.vehicles = new ArrayList<>();
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
                    this.depot = node;
                }
                this.vertex.add(node);
                //   0,0 1,0 2,0 0,1 1,1 2,1
                //    0   1   2   3   4   5
            }
        }
    }

    public Node GetNode(int x, int y){
        return this.vertex.get(x+y*(this.width+1));
    }

    public Vehicle GetVehicle(int id){
        Vehicle vehicle=null;
        for(int i=0;i<this.vehicles.size();i++){
            if(vehicles.get(i).id==id)vehicle = vehicles.get(i);
        }
        return vehicle;
    }

    public Environment CopyEnvironment(ArrayList<Request> requests){
        Environment environment = new Environment();
        Node node;
        Request request;
        for(int i=0;i<this.vertex.size();i++){
            node = new Node(this.vertex.get(i).x,this.vertex.get(i).y,this.vertex.get(i).isDepot
                    ,this.vertex.get(i).isBlocked,this.vertex.get(i).isRequest);
            environment.vertex.add(node);
        }
        for(int i=0;i< requests.size();i++){
            request= new Request(environment.GetNode(requests.get(i).destination.x,requests.get(i).destination.y)
                    ,requests.get(i).load,requests.get(i).insertionCost,requests.get(i).timeWindow
                    ,requests.get(i).duration,requests.get(i).distance,requests.get(i).id);
            environment.GetNode(requests.get(i).destination.x,requests.get(i).destination.y).request=request;
        }
        return environment;
    }
}
