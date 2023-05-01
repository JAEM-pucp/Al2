import app.algorithm.LNS;
import app.model.Blockage;
import app.model.Environment;
import app.model.Request;
import app.model.Solution;
import app.worker.Worker;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        /*
        // Press Alt+Intro with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        // Press Mayús+F10 or click the green arrow button in the gutter to run the code.
        for (int i = 1; i <= 5; i++) {

            // Press Mayús+F9 to start debugging your code. We have set one breakpoint
            // for you, but you can always add more by pressing Ctrl+F8.
            System.out.println("i = " + i);
        }

        Worker worker = new Worker();
        ArrayList<Blockage> blockages = null;
        try {
            blockages = worker.ImportBlockages();
        } catch(Exception ex) {
            System.out.println("Error with file importing");
            System.exit(0);
        }
        LocalDateTime currentTime = LocalDateTime.of(2023, Month.APRIL, 1, 0, 0);
        Environment environment = new Environment(70,50,45,30,blockages,currentTime);
        Solution solution = new Solution(20, 25, 30, 5, 40, 4, 60, 3,environment);
        LNS lns = new LNS();
        Request request = new Request(0,45,26,27,currentTime,1,476);
        solution.unrouted.add(request);
        request = new Request(1,45,24,3,currentTime,1,239);
        solution.unrouted.add(request);
        solution.started=true;
        solution = lns.Solve(solution,environment);
        for(int i=0;i<solution.routes.size();i++){
            if(solution.routes.get(i).tripNodes.get(0).visitTime!=null) {
                if (currentTime.isEqual(solution.routes.get(i).tripNodes.get(0).visitTime)) {
                    solution.routes.get(i).tripNodes.remove(0);
                }
            }
        }
        request = new Request(2,45,24,3,currentTime.plusMinutes(1),2,239);
        solution.unrouted.add(request);
        environment.currentTime=environment.currentTime.plusMinutes(1);
        solution=lns.Solve(solution,environment);

         */
        Worker worker = new Worker();
        worker.Simulate();
    }
}