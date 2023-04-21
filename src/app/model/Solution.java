package app.model;

import java.util.ArrayList;

public class Solution {
    public ArrayList<Route> routes;
    public int requestAmount;

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
}
