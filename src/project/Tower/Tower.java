package project.Tower;

import project.elevator.Elevator;
import project.simulation.SimObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static project.Tower.TowerConstants.NUMBER_OF_ELEVATORS;

public class Tower implements SimObject {
    private final List<Elevator> elevators;

    private final List<List<Integer>> requests;


    public Tower() {
        elevators = new ArrayList<>(NUMBER_OF_ELEVATORS);
        requests = new CopyOnWriteArrayList<>();
        initElevators();
    }

    public void addRequest(int originFloor, int destinationFloor) {
        requests.add(List.of(originFloor, destinationFloor));
    }

    private void executeRequest(int originFloor, int destinationFloor) {
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

    @Override
    public void fixedUpdate() {

    }

    @Override
    public void update(double deltaTime) {
        while (!requests.isEmpty()) {
            var request = requests.remove(0);
            executeRequest(request.get(0), request.get(1));
        }
    }

    @Override
    public void render(Graphics2D g, float interpolation) {

    }

    @Override
    public String getStatusText() {
        return "Elevators operational";
    }
}
