package app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Input {
    public Environment environment;
    public LocalDateTime currentTime;
    public Solution previousSolution;

    public Input(Environment environment, LocalDateTime currentTime, Solution previousSolution) {
        this.environment = environment;
        this.currentTime = currentTime;
        this.previousSolution = previousSolution;
    }
}
