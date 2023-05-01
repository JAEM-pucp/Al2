package app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Environment {
    public int width;
    public int height;
    public int depotX;
    public int depotY;
    public ArrayList<Blockage> blockages;
    public LocalDateTime currentTime;

    public boolean IsMoveAllowed(int x, int y, LocalDateTime currentTime) {
        boolean moveIsAllowed=true;
        if(x>this.width)moveIsAllowed= false;
        if(y>this.height)moveIsAllowed= false;
        if(x<0)moveIsAllowed= false;
        if(y<0)moveIsAllowed= false;
        for(int i=0;i<this.blockages.size();i++){
            if(x==this.blockages.get(i).x && y==this.blockages.get(i).y &&
                    (currentTime.isEqual(this.blockages.get(i).startTime)
                            || currentTime.isAfter(this.blockages.get(i).startTime))
                    && (currentTime.isEqual(this.blockages.get(i).endTime)
                    || currentTime.isBefore(this.blockages.get(i).endTime))){
                moveIsAllowed=false;
            }
        }
        return moveIsAllowed;
    }

    public Environment(int width, int height, int depotX, int depotY, ArrayList<Blockage> blockages, LocalDateTime currentTime) {
        this.width = width;
        this.height = height;
        this.depotX = depotX;
        this.depotY = depotY;
        this.blockages = blockages;
        this.currentTime = currentTime;
    }
}
