package project.elevator;

import project.Tower.TowerConstants;
import project.View;
import project.enums.Direction;
import project.enums.Status;
import project.simulation.SimObject;
import project.simulation.Simulation;
import project.simulation.SimulationConstants;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static project.elevator.ElevatorConstants.*;

public class Elevator implements SimObject {
    private final Point.Double position;
    private double speed, velocity;
    private int currentFloor;
    private final Queue<Integer> destinationFloors;
    private final Queue<Action> actionQueue;

    // current Action has different time then queued Actions
    private double actionEndTime;

    private Status currentStatus;
    private Direction currentDirection;

    public Elevator(int index, int currentFloor) {
        this.currentFloor = currentFloor;
        position = new Point2D.Double(index * SimulationConstants.ELEVATOR_SPACING_PIXEL + 1, currentFloor * TowerConstants.FLOOR_HEIGHT);
        destinationFloors = new LinkedList<>();
        actionQueue = new LinkedList<>();
        currentStatus = Status.IDLE;
        currentDirection = Direction.NONE;
    }

    public static double calculateTravelTime(int floor1, int floor2) {
        double distance = Math.abs(floor2 * TowerConstants.FLOOR_HEIGHT - floor1 * TowerConstants.FLOOR_HEIGHT);
        if (distance > DISTANCE_TO_ACCELERATE * 2) {
            return  (distance - DISTANCE_TO_ACCELERATE * 2) / MAX_SPEED + TIME_TO_ACCELERATE * 2;
        } else {
            return Math.sqrt(distance / ACCELERATION) * 2;
        }
    }

    public double calculateTimeToFloor(int floor) {
        double movingTime = calculateTravelTime(currentFloor, floor);

        if (currentStatus == Status.IDLE) {
            return movingTime;
        } else {
            double actionRestTime = actionEndTime - Simulation.getTick() / 1000d;
            double totalTime = actionRestTime > 0 ? actionRestTime : 0d;
            totalTime += actionQueue.stream()
                    .mapToDouble(Action::getDuration)
                    .sum();
            int oldFloor = currentFloor;
            var floors = destinationFloors.stream().toList();
            for (int i = 0; i < destinationFloors.size(); i++) {
                int nextFloor = floors.get(i);
                totalTime += calculateTravelTime(oldFloor, nextFloor) + WAITING_TIME;
                oldFloor = nextFloor;
            }
            return totalTime;
        }
    }

    public void addDestinationFloor(int floor) {
        destinationFloors.add(floor);
    }

    public void addDestinationFloors(List<Integer> floors) {
        for (int floor : floors) {
            addDestinationFloor(floor);
        }
    }

    private void updateStatus(double deltaTime) {
        currentFloor = (int) Math.round(position.y / TowerConstants.FLOOR_HEIGHT);

        double currentTime = Simulation.getTick() / 1000d;
        if (actionEndTime <= currentTime) {
            if (!actionQueue.isEmpty()) {
                Action action = actionQueue.remove();
                actionEndTime = currentTime + action.getDuration();
                currentStatus = action.getStatus();
                currentDirection = action.getDirection();
            } else {
                speed = 0d;
                velocity = speed;
                evaluateActions();
            }
        } else {
            switch (currentStatus) {
                case ACCELERATING -> {
                    speed += ACCELERATION * deltaTime;
                    speed = Math.min(speed, MAX_SPEED);
                }
                case BRAKING, WAITING -> {
                    speed -= ACCELERATION * deltaTime;
                    speed = Math.max(speed, 0d);
                }
            }
            switch (currentDirection) {
                case UP, NONE -> velocity = speed;
                case DOWN -> velocity = -speed;
            }
        }
    }

    private void evaluateActions() {
        if (destinationFloors.peek() != null) {
            int nextDestination = destinationFloors.remove();
            double displacement = nextDestination * TowerConstants.FLOOR_HEIGHT - position.y;
            double distance = Math.abs(displacement);

            Direction direction = Direction.NONE;
            if (distance < MAX_DELTA) {
                return;
            } else if (displacement < 0) {
                direction = Direction.DOWN;
            } else if (displacement > 0) {
                direction = Direction.UP;
            }

            if (distance > DISTANCE_TO_ACCELERATE * 2) {
                actionQueue.add(new Action(TIME_TO_ACCELERATE, Status.ACCELERATING, direction));
                actionQueue.add(new Action((distance - DISTANCE_TO_ACCELERATE * 2) / MAX_SPEED, Status.MOVING, direction));
                actionQueue.add(new Action(TIME_TO_ACCELERATE, Status.BRAKING, direction)); // evt. static Methods für die Actions?! elevators.project.elevator.Action.brake(timeToAccelerate, direction)
            } else {
                double halfTime = Math.sqrt(distance / ACCELERATION);
                actionQueue.add(new Action(halfTime, Status.ACCELERATING, direction));
                actionQueue.add(new Action(halfTime, Status.BRAKING, direction));
            }
            actionQueue.add(new Action(WAITING_TIME, Status.WAITING, Direction.NONE));
        } else {
            currentStatus = Status.IDLE;
        }
    }

    @Override
    public void fixedUpdate() {
        position.y += velocity * SimulationConstants.FIXED_DELTA_TIME;
    }

    @Override
    public void update(double deltaTime) {
        updateStatus(deltaTime);
    }

    @Override
    public void render(Graphics2D g, float interpolation) {
        double displayElevation = position.y + velocity * interpolation * SimulationConstants.FIXED_DELTA_TIME;
        Point pixelPos = new Point((int) position.x, View.HEIGHT - (int) Math.round(displayElevation / TowerConstants.FLOOR_HEIGHT * SimulationConstants.FLOOR_HEIGHT_PIXEL) - SimulationConstants.ELEVATOR_SPACING_PIXEL + 2);
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
        return String.format("%d - %.2fm " + status.toLowerCase() + " " + direction.toLowerCase(), currentFloor, Math.abs(position.y));
    }
}
