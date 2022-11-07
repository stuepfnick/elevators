import java.awt.*;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Queue;

public class Elevator implements SimObject {

    // Constants for ALL elevators
    public static final double MAX_SPEED = 8.0;    // m/s
    public static final double ACCELERATION = 1.5; // m/s^2
    private static final double MAX_DELTA = 0.01;
    private static final double WAITING_TIME = 10;

    private final double distanceToAccelerate = (MAX_SPEED * MAX_SPEED) / (ACCELERATION * 2);
    private final double timeToAccelerate = MAX_SPEED / ACCELERATION;
    private final Point.Double position;
    private double speed, velocity;
    private int currentFloor;
    private int nextDestination;
    private final Queue<Integer> destinationFloors;
    private final Queue<Action> actionQueue;

    private Status currentStatus;
    private Direction currentDirection;
    private double actionEndTime;

    public Elevator(int index, int currentFloor) {
        this.currentFloor = currentFloor;
        position = new Point2D.Double(index * 10, currentFloor * Tower.FLOOR_HEIGHT);
        destinationFloors = new LinkedList<>();
        actionQueue = new LinkedList<>();
        currentStatus = Status.IDLE;
    }

    public Queue<Integer> getDestinationFloors() {
        return destinationFloors;
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
                if (currentStatus == Status.STOPPED) {
                    System.out.println("speed: " + speed);
                    System.out.println((int) (position.x / 10) + " Floor: " + currentFloor + " @elevation: " + String.format("%.14f", position.y));
                }
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
                case BRAKING, STOPPED -> {
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
            nextDestination = destinationFloors.remove();
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
                actionQueue.add(new Action(timeToAccelerate, Status.BRAKING, direction)); // evt. static Methods für die Actions?!
            } else {
                double halfTime = Math.sqrt(distance / ACCELERATION);
                actionQueue.add(new Action(halfTime, Status.ACCELERATING, direction));
                actionQueue.add(new Action(halfTime, Status.BRAKING, direction));
            }
            actionQueue.add(new Action(WAITING_TIME, Status.STOPPED, Direction.NONE));
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
        g.fillRect((int) position.x, (int) Math.round(displayElevation / Tower.FLOOR_HEIGHT * 10), 10, 10);
    }
}
