package app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Solution {
    public ArrayList<Route> routes;
    public ArrayList<Request> unrouted;

    public Solution(){
        this.routes = new ArrayList<>();
        this.unrouted = new ArrayList<>();
    }

    public Solution CopySolution(Environment environment){
        Solution solution = new Solution();
        Route route;
        for(int i=0;i<this.routes.size();i++){
            route = this.routes.get(i).CopyRoute(environment);
            solution.routes.add(route);
        }
        for(int i=0;i<this.unrouted.size();i++){
            solution.unrouted.add(environment.GetRequest(this.unrouted.get(i).x,this.unrouted.get(i).y));
        }
        return solution;
    }

    public ArrayList<Route> GetAvailableRoutes(){
        ArrayList<Route> availableRoutes = new ArrayList<>();
        for(int i=0;i<this.routes.size();i++){
            if(this.routes.get(i).vehicle.isAvailable){
                availableRoutes.add(this.routes.get(i));
            }
        }
        return availableRoutes;
    }

    public int GetRequestAmount(){
        int requestAmount=0;
        for(int i =0;i<this.routes.size();i++){
            for(int j=0;j<this.routes.get(i).stops.size();j++){
                if(this.routes.get(i).stops.get(j).isRequest){
                    requestAmount++;
                }
            }
        }
        return requestAmount;
    }
    public Solution(Environment environment) {
        this.routes = new ArrayList<>();
        this.unrouted = new ArrayList<>();
        Route route;
        for(int i=0;i<environment.vehicles.size();i++){
            route = new Route();
            route.nodes.add(environment.GetDepot());
            route.nodes.add(environment.GetDepot());
            route.stops.add(environment.GetDepot());
            route.stops.add(environment.GetDepot());
            route.vehicle=environment.vehicles.get(i);
            this.routes.add(route);
        }
    }

    public float CalculateScore(Environment environment, LocalDateTime currentTime){
        float costAvg=0;
        for(int i=0;i<this.routes.size();i++){
            costAvg+=this.routes.get(i).EvaluateRoute(environment,currentTime);
        }

        return costAvg/this.routes.size();
    }

    public boolean IsActive(){
        boolean isActive = false;
        for(int i=0;i<this.routes.size();i++){
            if(this.routes.get(i).startTime!=null){
                isActive = true;
            }
        }
        return isActive;
    }
}
