package app.worker;

import app.algorithm.LNS;
import app.model.*;

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
        LocalDateTime currentTime = LocalDateTime.of(2023, Month.APRIL, 1, 0, 0);
        LocalDateTime comp = LocalDateTime.of(2023, Month.APRIL, 1, 5, 8);
        Environment environment;//25 4
        environment = new Environment(70, 50, 45, 30, 20, 25, 30, 5, 40, 4, 60, 3);
        //environment.SetBlockage(44,30);
        LNS lns = new LNS();
        ArrayList<Request> requests = new ArrayList<>();
        try {
            requests = this.ImportRequests();
        } catch(Exception ex) {
            System.out.println("Error with file importing");
            System.exit(0);
        }
        ArrayList<Blockage> blockages = new ArrayList<>();
        try {
            blockages = this.ImportBlockages();
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
        boolean blockageWasAdded = false;
        long expNum = 0;
        long timeElapsedInRoute;
        int expNumAmount =0;
        int reqAmount;
        int x;
        int y;
        boolean isRequest;
        boolean changes = false;
        boolean collapse = false;
        LocalDateTime endTime;
        //bike moves one node every minute
        //car moves one node every two minutes
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //run the simulation
        while(true){
            changes=false;
            System.out.println("Current time: " + currentTime.format(formatter));
            input.currentTime = currentTime;

            //checking collapse
            for(int i=0;i<input.environment.requests.size();i++){
                endTime = input.environment.requests.get(i).startTime.plusHours(input.environment.requests.get(i).timeWindow);
                if(currentTime.isAfter(endTime)){
                    collapse=true;
                    System.out.println("System collapse");
                    break;
                }
            }
            if(collapse)break;

            //if solution was constructed at least once
            if(started){
                blockageWasAdded = false;

                //for each blockage
                for(int i=0;i< blockages.size();i++){

                    //if blockage needs to be active
                    if(blockages.get(i).startTime.equals(currentTime)){

                        //insert blockage into environment
                        input.environment.SetBlockage(blockages.get(i).x,blockages.get(i).y);
                        blockageWasAdded = true;
                    }

                    //if blockage needs to be removed
                    if(blockages.get(i).endTime.equals(currentTime)){

                        //remove from environment
                        input.environment.GetNode(blockages.get(i).x,blockages.get(i).y).isBlocked = false;
                    }
                }

                //if any blockage was added, recalculate
                if(blockageWasAdded) {
                    lns.Destroy(input.previousSolution, input.environment, 1, true);
                    output = lns.Solve(input);
                    changes = true;
                    input.previousSolution = output.solution;
                    input.environment = output.environment;
                }
            }

            //if there are requests to attend
            if(requests.size()!=0) {

                //if the request needs to be attended now
                if (requests.get(0).startTime.equals(currentTime)) {

                    reqAmount = requests.size();
                    //add all the requests that need to be inserted into the solution
                    for(int i=0;i< reqAmount;i++){
                        if(requests.get(0).startTime.equals(currentTime)){

                            //add to the total of requests
                            input.environment.requests.add(requests.get(0));

                            //add to the existing unrouted list
                            if (input.previousSolution != null) {
                                input.previousSolution.unrouted.add(requests.get(0));
                            }

                            //set node as request
                            input.environment.GetNode(requests.get(0).x,requests.get(0).y).isRequest=true;

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
                    changes=true;

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



            if(started){

                //if changes were made, routes may need to be deactivated
                for(int i=0;i<input.previousSolution.routes.size();i++){
                    if(input.previousSolution.routes.get(i).stops.size()==2 && input.previousSolution.routes.get(i).stops.get(0).isDepot){
                        input.previousSolution.routes.get(i).startTime=null;
                    }
                }

                //for each route
                for(int i=0;i<input.previousSolution.routes.size();i++){

                    //if the route is active
                    if(input.previousSolution.routes.get(i).startTime!=null && input.previousSolution.routes.get(i).nodes.size()!=0){
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
                                        expNum+=(ChronoUnit.MINUTES.between(input.environment.GetRequest(x,y).startTime,currentTime));
                                        expNumAmount++;

                                        //update environment node to no longer be a request
                                        input.environment.GetNode(x,y).isRequest=false;

                                        //remove request from environment
                                        input.environment.RemoveRequest(x,y);
                                    }
                                }
                            }

                            //if the route is over, initialize it
                            if(input.previousSolution.routes.get(i).nodes.size()==1){
                                input.previousSolution.routes.get(i).startTime=null;
                                input.previousSolution.routes.get(i).nodes.add(input.environment.GetDepot());
                                input.previousSolution.routes.get(i).stops.add(input.environment.GetDepot());
                                input.previousSolution.routes.get(i).vehicle.load=0;
                            } /*else if (input.previousSolution.routes.get(i).GetRequests(input.environment).size()==0 && input.previousSolution.routes.get(i).stops.get(0).isDepot) {
                                    input.previousSolution.routes.get(i).startTime=null;
                                }*/
                        }
                    }
                }
            }
            //if all requests were attended and the routes aren't active, finish simulation
            if(requests.size()==0 && !input.previousSolution.IsActive()){
                break;
            }

            if(started){
                for(int i=0;i<input.previousSolution.routes.size();i++){
                    if(input.previousSolution.routes.get(i).startTime!=null){
                        System.out.print("Stops: ");
                        for(int j=0;j<input.previousSolution.routes.get(i).stops.size();j++){
                            System.out.print("("+input.previousSolution.routes.get(i).stops.get(j).x+","+input.previousSolution.routes.get(i).stops.get(j).y+") ");
                        }
                        System.out.println("");
                        if(input.previousSolution.routes.get(i).nodes.size()==0){
                            System.out.println(input.previousSolution.routes.get(i).vehicle.id+" error");
                        }
                        else {
                            System.out.println(input.previousSolution.routes.get(i).vehicle.id+" current position: (" + input.previousSolution.routes.get(i).nodes.get(0).x + "," + input.previousSolution.routes.get(i).nodes.get(0).y + ") ");
                        }
                        System.out.println("______________________________________________________");
                        /*for(int j=0;j<input.previousSolution.routes.get(i).nodes.size();j++){
                            System.out.print("("+input.previousSolution.routes.get(i).nodes.get(j).x+","+input.previousSolution.routes.get(i).nodes.get(j).y+") ");
                        }
                        System.out.println("");*/
                    }
                }
                System.out.println("========================================================");
            }

            //add a minute to current time
            currentTime = currentTime.plusMinutes(1);
            comp = LocalDateTime.of(2023, Month.APRIL, 1, 3, 48);
            if(currentTime.equals(comp)){
                System.out.print("");
            }
        }
        if(expNumAmount==0){
            expNum=0;
        }
        else {
            expNum = expNum / expNumAmount;
        }
        System.out.println("ExpNum: " + expNum);
    }

    public ArrayList<Request> ImportRequests() throws FileNotFoundException {
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
                LocalDateTime startTime = LocalDateTime.of(2023, Month.APRIL, 1, 0, 0).plusHours(Integer.parseInt(dateSections[0])).plusMinutes(Integer.parseInt(dateSections[1]));
                int x = Integer.parseInt(order[1]);
                int y = Integer.parseInt(order[2]);
                int load = Integer.parseInt(order[3]);
                int idCustomer = Integer.parseInt(order[4]);
                int timeWindow = Integer.parseInt(order[5]);
                //environment.GetNode(x,y).isRequest=true;

                Request request = new Request(x,y,load,timeWindow,startTime,idCustomer);
                requests.add(request);
            }
            scan.close();
        }
        return requests;
    }

    public ArrayList<Blockage> ImportBlockages() throws FileNotFoundException {
        File inputDirectory;
        ArrayList<Blockage> blockages = new ArrayList<>();
        inputDirectory = new File(System.getProperty("user.dir") + "/input/blockages");
        String[] inputFiles = inputDirectory.list((dir, name) -> new File(dir, name).isFile());
        int k = 0;
        for (int i = 0; i < inputFiles.length; i++) {
            File file = new File(inputDirectory + "/" + inputFiles[i]);
            Scanner scan = new Scanner(file);
            while (scan.hasNextLine()) {
                String blockStr = scan.nextLine();
                String[] b = blockStr.split(",");
                String[] start = b[0].split("-")[0].split(":");
                String[] end = b[0].split("-")[1].split(":");
                LocalDateTime tStart = LocalDateTime.of(2023, Month.APRIL, 1, 0, 0).plusDays(Integer.parseInt(start[0]) - 1).plusHours(Integer.parseInt(start[1])).plusMinutes(Integer.parseInt(start[2]));
                LocalDateTime tEnd = LocalDateTime.of(2023, Month.APRIL, 1, 0, 0).plusDays(Integer.parseInt(end[0]) - 1).plusHours(Integer.parseInt(end[1])).plusMinutes(Integer.parseInt(end[2]));
                for (int j = 1; j < b.length-1; j+=2) {
                    Blockage blockage = new Blockage();
                    blockage.id = k;
                    blockage.startTime = tStart;
                    blockage.endTime = tEnd;
                    blockage.x = Integer.parseInt(b[j]);
                    blockage.y = Integer.parseInt(b[j+1]);
                    blockages.add(blockage);
                    k++;
                }
            }
            scan.close();
        }
        return blockages;
    }
}