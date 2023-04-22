import app.algorithm.LNS;
import app.model.Environment;
import app.model.Request;
import app.model.Solution;

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

         */
        Environment environment;
        environment = new Environment(70, 50, 45, 30, 4, 25, 30, 5, 6, 4, 60, 3);
        LNS lns = new LNS();
        ArrayList<Request> requests = new ArrayList<>();
        Request request = new Request(environment.GetNode(35,41),3,24*60,0);
        environment.GetNode(35,41).isRequest=true;
        environment.GetNode(35,41).request=request;
        requests.add(request);
        request = new Request(environment.GetNode(10,35),7,8*60,1);
        environment.GetNode(10,35).isRequest=true;
        environment.GetNode(10,35).request=request;
        requests.add(request);
        Solution solution = lns.Solve(requests,environment);

    }
}