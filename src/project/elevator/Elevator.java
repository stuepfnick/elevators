package project.elevator;

import project.View;
import project.enums.Direction;
import project.enums.Status;
import project.simulation.SimObject;
import project.simulation.Simulation;
import project.simulation.SimulationConstants;
import project.tower.TowerConstants;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static project.elevator.ElevatorConstants.*;

public class Elevator implements SimObject {
    private final Point.Double position;
    private double speed, velocity;
    private int currentFloor, nextDestinationFloor, numberOfPassengers;
    private final Queue<Request> requestQueue;
    private final Queue<Action> actionQueue;

    // current Action has different time then queued Actions
    private double actionEndTime;
    private Status currentStatus;
    private Direction currentDirection;

    /**
     * Constructor takes 2 params
     * @param index the index of elevator in Tower List
     * @param currentFloor the floor, where the elevator starts
     */
    public Elevator(int index, int currentFloor) {
        this.currentFloor = currentFloor;
        nextDestinationFloor = currentFloor;
        position = new Point.Double((index * SimulationConstants.ELEVATOR_SPACING_PIXEL) + (SimulationConstants.ELEVATOR_SPACING_PIXEL - PIXEL_HEIGHT) / 2.0, currentFloor * TowerConstants.FLOOR_HEIGHT);
        requestQueue = new LinkedList<>();
        actionQueue = new LinkedList<>();
        currentStatus = Status.IDLE;
        currentDirection = Direction.NONE;
    }

    /**
     * Calculates the pure travel time between 2 floors
     * @param floor1 origin floor
     * @param floor2 destination floor
     * @return time as double in seconds
     */
    public static double calculateTravelTime(int floor1, int floor2) {
        double distance = Math.abs((floor2 - floor1) * TowerConstants.FLOOR_HEIGHT);
        if (distance > DISTANCE_TO_ACCELERATE * 2) {
            return (distance - DISTANCE_TO_ACCELERATE * 2) / MAX_SPEED + TIME_TO_ACCELERATE * 2;
        }
        return Math.sqrt(distance / ACCELERATION) * 2;
    }

    public static double calculateTravelAndWaitingTime(int floor1, int floor2) {
        return floor1 == floor2 ? 0d : calculateTravelTime(floor1, floor2) + WAITING_TIME;
    }

    /**
     * Calculates the time needed, until the elevator reaches the origin floor of the new request
     * @param request the new Request
     * @return time as double
     */
    public double calculateTimeToRequest(Request request) {
        double remainingActionTime = actionEndTime - Simulation.getTick() / 1000d;
        double totalTime = Math.max(remainingActionTime, 0d);
        totalTime += actionQueue.stream() // time for remaining Actions
                .mapToDouble(Action::getDuration)
                .sum();
        int previousFloor = nextDestinationFloor;
        for (var req : requestQueue) { // time for remaining Requests
            if (request.getOriginFloor() == previousFloor && request.getDestinationFloor() == req.getOriginFloor()) {
                return totalTime; // if the request corresponds to an empty return trip: return time until then
            }
            totalTime += calculateTravelAndWaitingTime(previousFloor, req.getOriginFloor());
            totalTime += calculateTravelAndWaitingTime(req.getOriginFloor(), req.getDestinationFloor());
            previousFloor = req.getDestinationFloor();
        }
        return totalTime + calculateTravelAndWaitingTime(previousFloor, request.getOriginFloor()); // time to requested floor
    }

    /**
     * Tries to add a passenger to an existing Request
     * @param request incoming Request
     * @return boolean, if it could add the passenger
     */
    public boolean tryAddPassenger(Request request) {
        return requestQueue.stream()
                .filter(r -> r.equals(request) && r.getNumberOfPassengers() < CAPACITY) // if request is in queue and under capacity
                .findFirst()
                .map(r -> {
                    r.addPassenger();   // add passenger
                    return true;
                }).orElse(false);
    }

    /**
     * Actually adds the new Request into the queue <br>
     * Normally at the end of queue, or if it finds an empty return run
     * @param request the new Request
     */
    public void addRequest(Request request) {
        List<Request> requestList = (LinkedList<Request>) requestQueue;
        int previousFloor = nextDestinationFloor;
        for (int i = 0; i < requestList.size(); i++) {
            Request req = requestList.get(i);
            if (request.getOriginFloor() == previousFloor && request.getDestinationFloor() == req.getOriginFloor()) {
                requestList.add(i, request); // if request matches empty return run, insert new Request at this position
                return;
            }
            previousFloor = req.getDestinationFloor();
        }
        requestQueue.add(request);
        if (request.getOriginFloor() == currentFloor && currentStatus == Status.IDLE && requestQueue.size() == 1) {
            actionQueue.add(new Action(WAITING_TIME, Status.WAITING, Direction.NONE));
        }
    }

    /**
     * Updates status, depending on different things like currentAction, ActionQueue <br>
     * and requestQueue
     * @param deltaTime the time passed, since last call in seconds.
     */
    private void updateStatus(double deltaTime) {
        currentFloor = (int) Math.round(position.y / TowerConstants.FLOOR_HEIGHT);

        double currentTime = Simulation.getTick() / 1000d;
        if (currentTime >= actionEndTime) {  // action end or no action running
            double difference = currentTime - actionEndTime;
            difference = difference <= deltaTime ? difference : 0d;
            updateVelocity(deltaTime - difference);

            if (!actionQueue.isEmpty()) { // get next action from queue
                Action action = actionQueue.remove();
                currentStatus = action.getStatus();
                currentDirection = action.getDirection();
                actionEndTime = currentTime - difference + action.getDuration();
                updateVelocity(difference);
            } else {
                speed = 0d;
                velocity = speed;
                currentStatus = Status.IDLE;
                evaluateActions();
            }
        } else { // action running
            updateVelocity(deltaTime);
        }
    }

    /**
     * Updates only the velocity (depending on currentStatus and currentDirection).
     * @param deltaTime time passed, since last call in seconds.
     */
    private void updateVelocity(double deltaTime) {
        switch (currentStatus) {
            case ACCELERATING -> {
                speed += ACCELERATION * deltaTime;
                speed = Math.min(speed, MAX_SPEED);
            }
            case DECELERATING, WAITING -> {
                speed -= ACCELERATION * deltaTime;
                speed = Math.max(speed, 0d);
            }
        }
        switch (currentDirection) {
            case UP, NONE -> velocity = speed;
            case DOWN -> velocity = -speed;
        }
    }

    /**
     * Evaluates the following actions, depending on the queues
     */
    private void evaluateActions() {
        if (requestQueue.peek() != null) {
            if (requestQueue.peek().getOriginFloor() == currentFloor && currentStatus == Status.IDLE) {
                Request request = requestQueue.remove();
                nextDestinationFloor = request.getDestinationFloor();
                numberOfPassengers = request.getNumberOfPassengers();
            } else {
                nextDestinationFloor = requestQueue.peek().getOriginFloor();
                numberOfPassengers = 0;
            }
            double displacement = nextDestinationFloor * TowerConstants.FLOOR_HEIGHT - position.y;
            double distance = Math.abs(displacement);

            Direction direction = Direction.NONE;
            if (distance < MAX_DELTA) { // ???
                return;
            } else if (displacement < 0) {
                direction = Direction.DOWN;
            } else if (displacement > 0) {
                direction = Direction.UP;
            }

            if (distance > DISTANCE_TO_ACCELERATE * 2) {
                actionQueue.add(new Action(TIME_TO_ACCELERATE, Status.ACCELERATING, direction));
                actionQueue.add(new Action((distance - DISTANCE_TO_ACCELERATE * 2) / MAX_SPEED, Status.MOVING, direction));
                actionQueue.add(new Action(TIME_TO_ACCELERATE, Status.DECELERATING, direction)); // evt. static Methods für die Actions?! Action.brake(timeToAccelerate, direction)
            } else {
                double halfTime = Math.sqrt(distance / ACCELERATION);
                actionQueue.add(new Action(halfTime, Status.ACCELERATING, direction));
                actionQueue.add(new Action(halfTime, Status.DECELERATING, direction));
            }
            actionQueue.add(new Action(WAITING_TIME, Status.WAITING, Direction.NONE));
        } else {
            currentStatus = Status.IDLE;
            numberOfPassengers = 0;
        }
    }

    @Override
    public void fixedUpdate(double deltaTime) {
        updateStatus(deltaTime);
        position.y += velocity * deltaTime;
    }

    @Override
    public void render(Graphics2D g, float interpolation) {
        double displayElevation = position.y + velocity * interpolation * SimulationConstants.FIXED_DELTA_TIME;
        Point pixelPos = new Point((int) position.x, View.HEIGHT - (int) Math.round(displayElevation / TowerConstants.FLOOR_HEIGHT * SimulationConstants.FLOOR_HEIGHT_PIXEL) - SimulationConstants.FLOOR_HEIGHT_PIXEL + (SimulationConstants.FLOOR_HEIGHT_PIXEL - PIXEL_HEIGHT));
        g.setColor(Color.BLACK);
        g.fillRect(pixelPos.x, pixelPos.y, PIXEL_WIDTH, PIXEL_HEIGHT);
        g.setColor(Color.ORANGE);
        String elevationString = String.format("%.2fm", Math.abs(position.y));
        g.drawString(elevationString, pixelPos.x, pixelPos.y + PIXEL_HEIGHT - 2);
    }

    @Override
    public String getStatusText() {
        String status = String.valueOf(currentStatus);
        String direction = String.valueOf(currentDirection);
        try {
            return String.format("%d - %.2fm " + status.toLowerCase() + " " + direction.toLowerCase() + " -> " + nextDestinationFloor + " (" + numberOfPassengers + ") " + requestQueue, currentFloor, Math.abs(position.y));
        } catch (NullPointerException ignored) {
            return String.format("%d - %.2fm " + status.toLowerCase() + " " + direction.toLowerCase() + " -> " + nextDestinationFloor + " (" + numberOfPassengers + ") ", currentFloor, Math.abs(position.y));
        }
    }
}
