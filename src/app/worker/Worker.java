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
        LNS lns = new LNS();
        ArrayList<ExpNum> expNums = new ArrayList<>();
        ExpNum expNum;
        boolean expNumAlreadyExists;
        boolean simulationIsOver=false;
        boolean activeRoutes;
        boolean solvingNeeded;
        ArrayList<Request> requests = null;
        int reqAmount;
        LocalDateTime currentTime = LocalDateTime.of(2023, Month.APRIL, 1, 0, 0);
        int dayCounter=0;
        ArrayList<Blockage> blockages = null;
        try {
            blockages = this.ImportBlockages();
        } catch(Exception ex) {
            System.out.println("Error with file importing");
            System.exit(0);
        }
        boolean systemCollapse = false;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Environment environment = new Environment(70,50,45,30,blockages,currentTime);
        Solution solution = new Solution(20, 25, 30, 5, 40, 4, 60, 3,environment);
        while(!simulationIsOver){
            System.out.println("Current time: " + environment.currentTime.format(formatter));
            solvingNeeded=false;
            activeRoutes=false;
            if(environment.currentTime.getHour()==0 && environment.currentTime.getMinute()==0 && dayCounter<30){
                try {
                    requests = this.ImportRequests(environment.currentTime);
                } catch(Exception ex) {
                    System.out.println("Error with file importing");
                    System.exit(0);
                }
                dayCounter++;
            }
            //if there are requests to attend
            if(requests.size()!=0) {

                //if the request needs to be attended now
                if (requests.get(0).startTime.equals(environment.currentTime)) {

                    reqAmount = requests.size();
                    //add all the requests that need to be inserted into the solution
                    for (int i = 0; i < reqAmount; i++) {
                        if(requests.get(0).startTime.equals(environment.currentTime)){
                            solution.unrouted.add(requests.get(0));
                            requests.remove(0);
                        }else{
                            break;
                        }
                    }
                    solvingNeeded=true;
                }
            }
            if(solution.started){
                for(int i=0;i<solution.unrouted.size();i++){
                    if(environment.currentTime.isAfter(solution.unrouted.get(i).startTime.plusHours(solution.unrouted.get(i).timeWindow))){
                        systemCollapse=true;
                        break;
                    }
                }
                if(systemCollapse){
                    break;
                }
                for(int i=0;i<solution.routes.size();i++){
                    if(solution.routes.get(i).tripNodes.get(0).visitTime!=null) {
                        if (environment.currentTime.isAfter(solution.routes.get(i).tripNodes.get(0).visitTime)) {
                            for(int j=0;j<solution.routes.get(i).tripNodes.get(0).deliveries.size();j++){
                                solution.routes.get(i).vehicle.load-=solution.routes.get(i).tripNodes.get(0).deliveries.get(j).load;
                                expNumAlreadyExists=false;
                                for(int k=0;k<expNums.size();k++){
                                    if(expNums.get(k).requestId==solution.routes.get(i).tripNodes.get(0).deliveries.get(j).requestId){
                                        expNums.get(k).value= ChronoUnit.MINUTES.between(solution.routes.get(i).tripNodes.get(0).visitTime,
                                                solution.routes.get(i).tripNodes.get(0).deliveries.get(j).startTime.
                                                        plusHours(solution.routes.get(i).tripNodes.get(0).deliveries.get(j).timeWindow));
                                        expNumAlreadyExists=true;
                                    }
                                }
                                if(!expNumAlreadyExists){
                                    expNum = new ExpNum(solution.routes.get(i).tripNodes.get(0).deliveries.get(j).requestId,ChronoUnit.MINUTES.between(solution.routes.get(i).tripNodes.get(0).visitTime,
                                            solution.routes.get(i).tripNodes.get(0).deliveries.get(j).startTime.
                                                    plusHours(solution.routes.get(i).tripNodes.get(0).deliveries.get(j).timeWindow)));
                                    expNums.add(expNum);
                                }
                            }
                            solution.routes.get(i).tripNodes.remove(0);
                            if(solution.routes.get(i).tripNodes.size()==0){
                                solution.routes.get(i).tripNodes.add(new TripNode(0, environment.depotX, environment.depotY, true,null));
                                solution.routes.get(i).tripNodes.add(new TripNode(1, environment.depotX, environment.depotY, true,null));
                                if(solution.unrouted.size()>0){
                                    solvingNeeded=true;
                                }
                            }
                        }
                    }
                }
            }
            if(solvingNeeded){
                solution = lns.Solve(solution,environment);
            }
            for(int i=0;i<solution.routes.size();i++){
                if(solution.routes.get(i).tripNodes.get(0).visitTime!=null){
                    activeRoutes=true;
                }
            }
            if(!activeRoutes && requests.size()==0){
                simulationIsOver=true;
            }
            environment.currentTime=environment.currentTime.plusMinutes(1);
        }
        float finalExpNum=0;
        for(int i=0;i< expNums.size();i++){
            finalExpNum+=expNums.get(i).value;
        }
        System.out.println("ExpNum: " + finalExpNum/expNums.size());
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

    public ArrayList<Request> ImportRequests(LocalDateTime currentTime) throws FileNotFoundException {
        File inputDirectory;
        int id;
        ArrayList<Request> requests = new ArrayList<>();
        inputDirectory = new File(System.getProperty("user.dir") + "/input/packages");
        String[] inputFiles = inputDirectory.list((dir, name) -> new File(dir, name).isFile());
        for (int i = 0; i < inputFiles.length; i++) {
            File file = new File(inputDirectory + "/" + inputFiles[i]);
            Scanner scan = new Scanner(file);
            id =0;
            while (scan.hasNextLine()) {
                String orderStr = scan.nextLine();
                String[] order = orderStr.split(",");
                String readyTime = order[0];
                String[] dateSections = readyTime.split(":");
                LocalDateTime startTime = currentTime.plusHours(Integer.parseInt(dateSections[0])).plusMinutes(Integer.parseInt(dateSections[1]));
                int x = Integer.parseInt(order[1]);
                int y = Integer.parseInt(order[2]);
                int load = Integer.parseInt(order[3]);
                int idCustomer = Integer.parseInt(order[4]);
                int timeWindow = Integer.parseInt(order[5]);

                Request request = new Request(id,x,y,load,startTime,timeWindow,idCustomer);
                requests.add(request);
                id++;
            }
            scan.close();
        }
        return requests;
    }
}
