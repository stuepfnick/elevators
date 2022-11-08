package project.Tower;

import project.elevator.Elevator;

import java.util.ArrayList;
import java.util.List;

import static project.Tower.TowerConstants.*;

public class Tower {
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
