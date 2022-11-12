package project.tower;

import project.elevator.Elevator;
import project.elevator.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static project.tower.TowerConstants.NUMBER_OF_ELEVATORS;

public class Tower {
    private final List<Elevator> elevators;
    private final List<Request> requests;

    public Tower() {
        elevators = new ArrayList<>(NUMBER_OF_ELEVATORS);
        requests = new CopyOnWriteArrayList<>();
        initElevators();
    }

    public void addRequest(int originFloor, int destinationFloor) {
        if (originFloor == destinationFloor) {
            System.out.println("Origin and destination floors have to be different!");
            return;
        } else if (originFloor != 0 && destinationFloor != 0) {
            System.out.println("Only requests from or to floor 0 are valid!");
            return;
        }
        requests.add(new Request(originFloor, destinationFloor));
    }

    private void executeRequest(Request request) {
        Elevator fastestElevator = elevators.get(0);
        double fastestTime = fastestElevator.calculateTimeToRequest(request);
        for (var e : elevators) {
            if (e.tryAddPassenger(request)) return; // if we can add passenger to existing queue it has to be faster, so nothing more needed
            double time = e.calculateTimeToRequest(request);
            if (time < fastestTime) {
                fastestElevator = e;
                fastestTime = time;
            }
        }
        fastestElevator.addRequest(request);
    }

    private void initElevators() {
        for (int i = 0; i < NUMBER_OF_ELEVATORS; i++) {
            elevators.add(new Elevator(i, 0));
        }
    }

    public List<Elevator> getElevators() {
        return elevators;
    }

    public void update(double deltaTime) {
        while (!requests.isEmpty()) {
            var request = requests.remove(0);
            executeRequest(request);
        }
    }
}
