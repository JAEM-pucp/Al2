package app.algorithm;

import app.model.*;

import java.util.ArrayList;
import java.util.Random;

//40 motos 20 autos
//4 y 25 paquetes
//45 30 almacén
public class LNS {

    public Solution Solve(ArrayList<Request> requests, Node depot, int carAmount, int motorcycleAmount, Environment environment){
        Solution solution = this.ConstructInitialSolution(requests,depot,carAmount,motorcycleAmount,environment);
        Solution newSolution = new Solution();
        int iterations = 0;
        ArrayList<Request> unrouted;
        while(iterations < 10){
            //needs proper copy function
            //newSolution = solution;
            unrouted = this.Destroy(newSolution);
            this.Repair(newSolution,unrouted);
            iterations++;
        }
        return solution;
    }

    public Solution ConstructInitialSolution(ArrayList<Request> requests, Node depot, int carAmount, int motorcycleAmount, Environment environment){
        Solution solution = new Solution();
        Route route;
        int count = 0;
        //debería pasarse como parámetro
        //Environment environment = new Environment(70,50,45,30);
        while (requests.size() > count){
            route = new Route();
            route.nodes = this.CalculateRoute(depot,requests.get(count).destination,environment);
            route.requests.add(requests.get(count));
            requests.get(count).isActive=true;
            count++;
        }

        return solution;
    }

    public ArrayList<Request> Destroy(Solution solution){
        //number of trips to be removed from the solution
        int n = 3;
        int m = 0;
        ArrayList<Request> unrouted = new ArrayList<>();
        Random random = new Random();
        while(m<n){
            if(random.nextBoolean()){
                // está en duro, dará problemas cuando hayan muchos pedidos iniciales
                unrouted.add(solution.routes.get(m).requests.get(0));
                solution.routes.remove(m);
            }
            m++;
        }
        return unrouted;
    }

    public Solution Repair(Solution solution, ArrayList<Request> unrouted){
        int chosenRoute;
        int insertionCost;
        int insertLocation;
        for(int i=0;i<unrouted.size();i++){
            for(int j=0;j<solution.routes.size();j++){
                //cuando se arregle que la ruta vuelva al almacén probablemente se debe poner k+1<solution
                for(int k=0;k<solution.routes.get(j).nodes.size();k++){
                    //falta considerar que se pueda pasar del tiempo
                    insertionCost = solution.routes.get(j).nodes.get(k).CalculateCost(unrouted.get(i).destination);
                    if(insertionCost<unrouted.get(i).insertionCost){
                        chosenRoute=j;
                        unrouted.get(i).insertionCost=insertionCost;
                        insertLocation=k;
                    }
                }
            }
            //insertar el request en la mejor ruta

        }
        return solution;
    }

    public ArrayList<Node> CalculateRoute(Node origin, Node destination, Environment environment){
        ArrayList<Node> route = new ArrayList<>();
        int originX = origin.x;
        int originY = origin.y;
        if (destination.x>originX){
            for (; originX <= destination.x; originX++){
                route.add(environment.getNode(originX,originY));
            }
        }
        else if (destination.x<originX){
            for (; originX <= destination.x; originX--){
                route.add(environment.getNode(originX,originY));
            }
        }
        else{
            route.add(environment.getNode(originX,originY));
        }

        if (destination.y>originY){
            originY++;
            for (; originY <= destination.y; originY++){
                route.add(environment.getNode(originX,originY));
            }
        }
        else if (destination.y<originY){
            originY--;
            for (; originY <= destination.y; originY--){
                route.add(environment.getNode(originX,originY));
            }
        }
        return route;
    }
}
