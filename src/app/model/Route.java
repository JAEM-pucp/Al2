package app.model;

import java.util.ArrayList;

public class Route {
    public ArrayList<Node> nodes;
    public ArrayList<Request> requests;

    public Route() {
        this.nodes = new ArrayList<>();
        this.requests = new ArrayList<>();
    }
}
