import java.awt.*;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Elevator implements SimObject {

    // Constants for ALL elevators
    public static final double MAX_SPEED = 8.0;    // m/s
    public static final double ACCELERATION = 1.5; // m/s^2
    private static final double MAX_DELTA = 0.01;
    private static final double WAITING_TIME = 10;

    private static final int PIXEL_WIDTH = 10;
    private static final int PIXEL_HEIGHT = 10;

    private static final double distanceToAccelerate = (MAX_SPEED * MAX_SPEED) / (ACCELERATION * 2);
    private static final double timeToAccelerate = MAX_SPEED / ACCELERATION;
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
        position = new Point2D.Double(index * Simulation.ELEVATOR_SPACING_PIXEL + 1, currentFloor * Tower.FLOOR_HEIGHT);
        destinationFloors = new LinkedList<>();
        actionQueue = new LinkedList<>();
        currentStatus = Status.IDLE;
        currentDirection = Direction.NONE;
    }

    public static double calculateTravelTime(int floor1, int floor2) {
        double displacement = floor1 * Tower.FLOOR_HEIGHT - floor2 * Tower.FLOOR_HEIGHT;
        double distance = Math.abs(displacement);
        if (distance > timeToAccelerate * 2) {
            return  (distance - distanceToAccelerate * 2) / MAX_SPEED + timeToAccelerate * 2;
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
        currentFloor = (int) Math.round(position.y / Tower.FLOOR_HEIGHT);

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
            double displacement = nextDestination * Tower.FLOOR_HEIGHT - position.y;
            double distance = Math.abs(displacement);

            Direction direction = Direction.NONE;
            if (distance < MAX_DELTA) {
                return;
            } else if (displacement < 0) {
                direction = Direction.DOWN;
            } else if (displacement > 0) {
                direction = Direction.UP;
            }

            if (distance > distanceToAccelerate * 2) {
                actionQueue.add(new Action(timeToAccelerate, Status.ACCELERATING, direction));
                actionQueue.add(new Action((distance - distanceToAccelerate * 2) / MAX_SPEED, Status.MOVING, direction));
                actionQueue.add(new Action(timeToAccelerate, Status.BRAKING, direction)); // evt. static Methods für die Actions?! Action.brake(timeToAccelerate, direction)
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
        position.y += velocity * Simulation.FIXED_DELTA_TIME;
    }

    @Override
    public void update(double deltaTime) {
        updateStatus(deltaTime);
    }

    @Override
    public void render(Graphics2D g, float interpolation) {
        double displayElevation = position.y + velocity * interpolation * Simulation.FIXED_DELTA_TIME;
        Point pixelPos = new Point((int) position.x, View.HEIGHT - (int) Math.round(displayElevation / Tower.FLOOR_HEIGHT * Simulation.FLOOR_HEIGHT_PIXEL) - Simulation.ELEVATOR_SPACING_PIXEL + 2);
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
