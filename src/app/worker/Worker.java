package app.worker;

import app.algorithm.LNS;
import app.model.Environment;
import app.model.Input;
import app.model.Output;
import app.model.Request;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

public class Worker {
    public void Simulate(){
        Environment environment;
        environment = new Environment(70, 50, 45, 30, 4, 25, 30, 5, 6, 4, 60, 3);
        environment.SetBlockage(44,30);
        LNS lns = new LNS();
        ArrayList<Request> requests = new ArrayList<>();
        Request request = new Request(0,35,41,3,24*60, LocalDateTime.of(2023, Month.APRIL, 12, 10, 30));
        environment.GetNode(35,41).isRequest=true;
        requests.add(request);
        request = new Request(1,10,35,7,8*60,LocalDateTime.of(2023, Month.APRIL, 12, 10, 30));
        environment.GetNode(10,35).isRequest=true;
        requests.add(request);
        environment.requests=requests;
        Input input = new Input(environment,LocalDateTime.of(2023, Month.APRIL, 12, 10, 30),null);
        Output output = lns.Solve(input);
    }
}