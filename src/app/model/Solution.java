package app.model;

import java.util.ArrayList;

public class Solution {
    public ArrayList<Route> routes;
    public int requestAmount;

    public Solution(){
        this.routes = new ArrayList<>();
        this.requestAmount=0;
    }

    public Solution(Environment environment) {
        this.routes = new ArrayList<>();
        Route route;
        for(int i=0;i<environment.vehicles.size();i++){
            route = new Route();
            route.nodes.add(environment.depot);
            route.nodes.add(environment.depot);
            route.stops.add(environment.depot);
            route.stops.add(environment.depot);
            route.vehicle=environment.vehicles.get(i);
            this.routes.add(route);
        }
    }

    public float EvaluateSolution(){
        float costAvg=0;
        for(int i=0;i<this.routes.size();i++){
            costAvg+=this.routes.get(i).EvaluateRoute();
        }

        return costAvg/this.routes.size();
    }

    public Solution CopySolution(Environment environment){
        Solution solution = new Solution();
        Route route;
        for(int i=0;i<this.routes.size();i++){
            route = this.routes.get(i).CopyRoute(environment);
            solution.routes.add(route);
        }
        solution.requestAmount=this.requestAmount;
        return solution;
    }
}
