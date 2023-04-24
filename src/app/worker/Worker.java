package app.worker;

import app.algorithm.LNS;
import app.model.Environment;
import app.model.Input;
import app.model.Output;
import app.model.Request;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Worker {
    public void Simulate(){
        LocalDateTime currentTime = LocalDateTime.of(2023, Month.APRIL, 12, 10, 30);
        Environment environment;
        environment = new Environment(70, 50, 45, 30, 4, 25, 30, 5, 6, 4, 60, 3);
        environment.SetBlockage(44,30);
        LNS lns = new LNS();
        ArrayList<Request> requests = new ArrayList<>();
        Request request = new Request(0,35,41,3,24*60, LocalDateTime.of(2023, Month.APRIL, 12, 10, 31));
        environment.GetNode(35,41).isRequest=true;
        requests.add(request);
        request = new Request(1,10,35,7,8*60,LocalDateTime.of(2023, Month.APRIL, 12, 10, 33));
        environment.GetNode(10,35).isRequest=true;
        requests.add(request);

        Input input = new Input(environment,currentTime,null);
        Output output;

        boolean started =false;
        long expNum = 0;
        int expNumAmount =requests.size();
        //bike moves one node every minute
        //car moves one node every two minutes
        while(true){
            input.currentTime = currentTime;
            if(started){
                for(int i=0;i<input.previousSolution.routes.size();i++){
                    if(input.previousSolution.routes.get(i).startTime!=null){
                        if(ChronoUnit.MINUTES.between(input.previousSolution.routes.get(i).startTime,currentTime)%(60/input.previousSolution.routes.get(i).vehicle.speed)==0){
                            //move one node
                            if(input.previousSolution.routes.get(i).nodes.get(0).x == input.previousSolution.routes.get(i).stops.get(0).x && input.previousSolution.routes.get(i).nodes.get(0).y == input.previousSolution.routes.get(i).stops.get(0).y){
                                input.previousSolution.routes.get(i).stops.remove(0);
                            }
                            if(input.previousSolution.routes.get(i).nodes.get(0).isRequest){
                                input.environment.GetRequest(input.previousSolution.routes.get(i).nodes.get(0).x,input.previousSolution.routes.get(i).nodes.get(0).y).tripsLeft--;
                                if(input.environment.GetRequest(input.previousSolution.routes.get(i).nodes.get(0).x,input.previousSolution.routes.get(i).nodes.get(0).y).tripsLeft==0){
                                    expNum+=(ChronoUnit.MINUTES.between(input.environment.GetRequest(input.previousSolution.routes.get(i).nodes.get(0).x,input.previousSolution.routes.get(i).nodes.get(0).y).startTime,currentTime));
                                    input.environment.RemoveRequest(input.previousSolution.routes.get(i).nodes.get(0).x,input.previousSolution.routes.get(i).nodes.get(0).y);
                                }
                            }
                            input.previousSolution.routes.get(i).nodes.remove(0);
                            if(input.previousSolution.routes.get(i).nodes.size()==0){
                                input.previousSolution.routes.get(i).startTime=null;
                                input.previousSolution.routes.get(i).nodes.add(input.environment.GetDepot());
                                input.previousSolution.routes.get(i).nodes.add(input.environment.GetDepot());
                                input.previousSolution.routes.get(i).stops.add(input.environment.GetDepot());
                                input.previousSolution.routes.get(i).stops.add(input.environment.GetDepot());
                            }
                        }
                    }
                }
            }
            //if there are requests to attend
            if(requests.size()!=0) {
                if (requests.get(0).startTime.equals(currentTime)) {
                    environment.requests.add(requests.get(0));
                    if (input.previousSolution != null) {
                        input.previousSolution.unrouted.add(requests.get(0));
                    }
                    output = lns.Solve(input);
                    input.previousSolution = output.solution;
                    input.environment = output.environment;
                    //update routes starting time
                    for (int i = 0; i < input.previousSolution.routes.size(); i++) {
                        if (input.previousSolution.routes.get(i).startTime == null && input.previousSolution.routes.get(i).GetRequestAmount() > 0) {
                            input.previousSolution.routes.get(i).startTime = currentTime;
                        }
                    }
                    requests.remove(0);
                    started = true;
                }
            }
            //add a minute
            currentTime = currentTime.plusMinutes(1);
            if(requests.size()==0 && !input.previousSolution.IsActive()){
                break;
            }
        }
        expNum=expNum/expNumAmount;
    }
}