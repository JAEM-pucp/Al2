package app.worker;

import app.algorithm.LNS;
import app.model.Environment;
import app.model.Input;
import app.model.Output;
import app.model.Request;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Scanner;

public class Worker {
    public void Simulate(){
        LocalDateTime currentTime = LocalDateTime.of(2023, Month.APRIL, 12, 0, 0);
        Environment environment;//25 4
        environment = new Environment(70, 50, 45, 30, 20, 40, 30, 5, 40, 100, 60, 3);
        //environment.SetBlockage(44,30);
        LNS lns = new LNS();
        ArrayList<Request> requests = new ArrayList<>();
        try {
            requests = this.ImportRequests(environment);
        } catch(Exception ex) {
            System.out.println("Error with file importing");
            System.exit(0);
        }
        /*
        Request request = new Request(0,35,41,3,24*60, LocalDateTime.of(2023, Month.APRIL, 12, 10, 31));
        environment.GetNode(35,41).isRequest=true;
        requests.add(request);
        request = new Request(1,10,35,7,8*60,LocalDateTime.of(2023, Month.APRIL, 12, 10, 33));
        environment.GetNode(10,35).isRequest=true;
        requests.add(request);*/

        Input input = new Input(environment,currentTime,null);
        Output output;

        boolean started =false;
        long expNum = 0;
        long timeElapsedInRoute;
        int expNumAmount =requests.size();
        int x;
        int y;
        boolean isRequest;
        //bike moves one node every minute
        //car moves one node every two minutes
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //run the simulation
        while(true){
            System.out.println("Current time: " + currentTime.format(formatter));
            input.currentTime = currentTime;

            //if solution was constructed at least once
            if(started){

                //for each route
                for(int i=0;i<input.previousSolution.routes.size();i++){

                    //if the route is active
                    if(input.previousSolution.routes.get(i).startTime!=null){
                        timeElapsedInRoute =ChronoUnit.MINUTES.between(input.previousSolution.routes.get(i).startTime,currentTime);

                        //if it's their turn to move
                        if(timeElapsedInRoute>0 && timeElapsedInRoute%(60/input.previousSolution.routes.get(i).vehicle.speed)==0){

                            //move one node
                            x = input.previousSolution.routes.get(i).nodes.get(0).x;
                            y = input.previousSolution.routes.get(i).nodes.get(0).y;
                            input.previousSolution.routes.get(i).nodes.remove(0);

                            //if the node was a stop, remove from stops also
                            if(x==input.previousSolution.routes.get(i).stops.get(0).x && y==input.previousSolution.routes.get(i).stops.get(0).y){
                                isRequest = input.previousSolution.routes.get(i).stops.get(0).isRequest;
                                input.previousSolution.routes.get(i).stops.remove(0);

                                //if the stop was a request
                                if(isRequest){

                                    //reduce trip count from request
                                    input.environment.GetRequest(x,y).tripsLeft--;

                                    //reduce load from vehicle
                                    input.previousSolution.routes.get(i).vehicle.load-=input.previousSolution.routes.get(i).vehicle.GetRequestLoad(x,y);

                                    //remove request load from vehicle
                                    input.previousSolution.routes.get(i).vehicle.RemoveRequestLoad(x,y);

                                    //if all trips were covered
                                    if(input.environment.GetRequest(x,y).tripsLeft==0){

                                        //update expNum
                                        expNum+=(ChronoUnit.MINUTES.between(currentTime,input.environment.GetRequest(x,y).startTime.plusHours(input.environment.GetRequest(x,y).timeWindow)));

                                        //update environment node to no longer be a request
                                        input.environment.GetNode(x,y).isRequest=false;

                                        //remove request from enviornment
                                        input.environment.RemoveRequest(x,y);
                                    }
                                }

                                //if the route is over, initialize it
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
            }

            //if there are requests to attend
            if(requests.size()!=0) {

                //if the request needs to be attended now
                if (requests.get(0).startTime.equals(currentTime)) {

                    //add all the requests that need to be inserted into the solution
                    for(int i=0;i< requests.size();i++){
                        if(requests.get(0).startTime.equals(currentTime)){

                            //add to the total of requests
                            input.environment.requests.add(requests.get(0));

                            //add to the existing unrouted list
                            if (input.previousSolution != null) {
                                input.previousSolution.unrouted.add(requests.get(0));
                            }

                            //remove from list of requests to attend
                            requests.remove(0);
                        }
                        //if the requests doesn't need to be attended rn then break
                        else{
                            break;
                        }
                    }

                    //calculate new solution for the new set of requests
                    output = lns.Solve(input);

                    //update input with the values obtained
                    input.previousSolution = output.solution;
                    input.environment = output.environment;


                    //assign current time as starting time for the newly created routes
                    for (int i = 0; i < input.previousSolution.routes.size(); i++) {
                        if (input.previousSolution.routes.get(i).startTime == null && input.previousSolution.routes.get(i).GetRequestAmount() > 0) {
                            input.previousSolution.routes.get(i).startTime = currentTime;
                        }
                    }
                    started = true;
                }
            }
            //if all requests were attended and the routes aren't active, finish simulation
            if(requests.size()==0 && !input.previousSolution.IsActive()){
                break;
            }

            //add a minute to current time
            currentTime = currentTime.plusMinutes(1);
        }
        expNum=expNum/expNumAmount;
        System.out.println("ExpNum: " + expNum);
    }

    public ArrayList<Request> ImportRequests(Environment environment) throws FileNotFoundException {
        File inputDirectory;
        ArrayList<Request> requests = new ArrayList<>();
        inputDirectory = new File(System.getProperty("user.dir") + "/input/packages");
        String[] inputFiles = inputDirectory.list((dir, name) -> new File(dir, name).isFile());
        for (int i = 0; i < inputFiles.length; i++) {
            File file = new File(inputDirectory + "/" + inputFiles[i]);
            Scanner scan = new Scanner(file);
            while (scan.hasNextLine()) {
                String orderStr = scan.nextLine();
                String[] order = orderStr.split(",");
                String readyTime = order[0];
                String[] dateSections = readyTime.split(":");
                LocalDateTime startTime = LocalDateTime.of(2023, Month.APRIL, 12, 0, 0).plusHours(Integer.parseInt(dateSections[0])).plusMinutes(Integer.parseInt(dateSections[1]));
                int x = Integer.parseInt(order[1]);
                int y = Integer.parseInt(order[2]);
                int load = Integer.parseInt(order[3]);
                int idCustomer = Integer.parseInt(order[4]);
                int timeWindow = Integer.parseInt(order[5]);
                environment.GetNode(x,y).isRequest=true;

                Request request = new Request(x,y,load,timeWindow,startTime,idCustomer);
                requests.add(request);
            }
            scan.close();
        }
        return requests;
    }
}