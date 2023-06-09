package app.model;

import java.util.Comparator;

public class RequestComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        Request r1=(Request)o1;
        Request r2=(Request)o2;
        if(r1.timeWindow== r2.timeWindow){
            if(r1.uncoveredLoad==r2.uncoveredLoad){
                return 0;
            } else if (r1.uncoveredLoad<r2.uncoveredLoad) {
                return 1;
            }else {
                return -1;
            }
        }else if(r1.timeWindow> r2.timeWindow){
            return 1;
        }else {
            return -1;
        }
    }
}
