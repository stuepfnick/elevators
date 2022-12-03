package project.tower;

import project.elevator.Elevator;
import project.elevator.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static project.tower.TowerConstants.NUMBER_OF_ELEVATORS;

/**
 * The Tower object holds the List of elevators
 * and handles the Requests.
 */
public class Tower {
    private final List<Elevator> elevators;
    private final List<Request> requests;

    public Tower() {
        elevators = new ArrayList<>(NUMBER_OF_ELEVATORS);
        requests = new CopyOnWriteArrayList<>();
        initElevators();
    }

    public List<Elevator> getElevators() {
        return elevators;
    }

    /**
     * Adds the request to it's own queue.
     * (as it is on another thread than the simulation)
     * @param originFloor from floor
     * @param destinationFloor to floor
     */
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

    /**
     * Finally adds the request to the elevator which can execute it as fastest.<br>
     * This gets called from same thread as Simulation, so cannot cause a concurrent modification problem.
     * @param request the Request to be added
     */
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

    /**
     * Creates the elevators and add them to the List.
     */
    private void initElevators() {
        for (int i = 0; i < NUMBER_OF_ELEVATORS; i++) {
            elevators.add(new Elevator(i, 0));
        }
    }

    /**
     * Called from the Simulation Thread:<br>
     * Calls the executeRequest for each Request, which was stored in between 2 updates.
     */
    public void update() {
        while (!requests.isEmpty()) {
            var request = requests.remove(0);
            executeRequest(request);
        }
    }
}
