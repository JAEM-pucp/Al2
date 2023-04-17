package app.model;

import java.util.ArrayList;

public class Environment {
    public ArrayList<Node> vertex;
    public int width;
    public int height;
    public Environment(){

    }
    public Environment(int width, int height, int depotX, int depotY) {
        Node node;
        this.vertex = new ArrayList<>();
        for (int y = 0; y <= height; y++){
            for (int x = 0; x <= width; x++){
                node = new Node(x,y,false,false);
                if(depotX==x & depotY==y){
                    node.isDepot=true;
                }
                this.vertex.add(node);
                //   0,0 1,0 2,0 0,1 1,1 2,1
                //    0   1   2   3   4   5
            }
        }
    }

    public Node getNode(int x, int y){
        return this.vertex.get(x+y*this.width);
    }
}
