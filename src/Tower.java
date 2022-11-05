import java.util.ArrayList;
import java.util.List;

public class Tower {
    public static final double FLOOR_HEIGHT = 4.0; // m
    public static final int NUMBER_OF_ELEVATORS = 7;
    public static final int NUMBER_OF_FLOORS = 55;
    private final List<Elevator> elevators;

    public Tower() {
        elevators = new ArrayList<>(NUMBER_OF_ELEVATORS);
        initElevators();
    }

    private void initElevators() {
        for (int i = 0; i < NUMBER_OF_ELEVATORS; i++) {
            elevators.add(new Elevator(i, 0));
        }
    }

    public List<Elevator> getElevators() {
        return elevators;
    }
}
