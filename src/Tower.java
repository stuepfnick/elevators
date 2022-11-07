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

    public void addRequest(int originFloor, int destinationFloor) {
        List<Double> times = new ArrayList<>();
        for (var e : elevators) {
            times.add(e.calculateTimeToFloor(originFloor));
        }
        int fastestIndex = 0;
        for (int i = 0; i < times.size(); i++) {
            if (times.get(i) < times.get(fastestIndex)) {
                fastestIndex = i;
            }
        }
        var e = elevators.get(fastestIndex);
        e.addDestinationFloors(List.of(originFloor, destinationFloor));
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
