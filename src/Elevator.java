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

    public Elevator(int index, int currentFloor) {
        this.currentFloor = currentFloor;
        position = new Point2D.Double(index * 10, currentFloor * Tower.FLOOR_HEIGHT);
        destinationFloors = new LinkedList<>();
        actionQueue = new LinkedList<>();
    }

    public Queue<Integer> getDestinationFloors() {
        return destinationFloors;
    }

    public Integer nextDestination() {
        return destinationFloors.peek();
    }

    public Action currentAction() {
        return actionQueue.peek();
    }

    public void updateStatus(double deltaTime) {
        currentFloor = (int) Math.round(position.y / Tower.FLOOR_HEIGHT);

        if (actionQueue.isEmpty()) {
            speed = 0d;
            velocity = speed;
            evaluateActions();
        }
        if (!actionQueue.isEmpty()) {
            double currentTime = Simulation.getTick() / 1000d;
            double endTime = currentAction().getEndTime() - (deltaTime * 4);
            if (endTime > currentTime) {
                Status currentStatus = currentAction().getStatus();
                Direction direction = currentAction().getDirection();

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
                switch (direction) {
                    case UP, NONE -> velocity = speed;
                    case DOWN -> velocity = -speed;
                }
            } else {
                Action lastAction = actionQueue.remove();
                if (lastAction.getStatus() == Status.BRAKING) {
                    System.out.println("Speed: " + speed);
                    System.out.println((int) (position.x / 10) + " Floor: " + currentFloor + " @elevation: " + String.format("%.14f", position.y));
                }
            }
        }
    }

    public void evaluateActions() {
        if (nextDestination() != null) {
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

            double startTime = Simulation.getTick() / 1000d;
            if (distance > distanceToAccelerate * 2) {
                double accelerationEndTime = startTime + timeToAccelerate;
                actionQueue.add(new Action(accelerationEndTime, Status.ACCELERATING, direction));
                double movingEndTime = accelerationEndTime + (distance - distanceToAccelerate * 2) / MAX_SPEED;
                actionQueue.add(new Action(movingEndTime, Status.MOVING, direction));
                double brakingEndTime = movingEndTime + timeToAccelerate;
                actionQueue.add(new Action(brakingEndTime, Status.BRAKING, direction)); // evt. static Methods für die Actions?!
                double waitingEndTime = brakingEndTime + WAITING_TIME;
                actionQueue.add(new Action(waitingEndTime, Status.STOPPED, Direction.NONE)); // evt. endTime von vorheriger Action oder doch duration?!
            } else {
                double halfTime = Math.sqrt(distance / ACCELERATION);
                double accelerationEndTime = startTime + halfTime;
                actionQueue.add(new Action(accelerationEndTime, Status.ACCELERATING, direction));
                double brakingEndTime = accelerationEndTime + halfTime;
                actionQueue.add(new Action(brakingEndTime, Status.BRAKING, direction));
                double waitingEndTime = brakingEndTime + WAITING_TIME; // Mit duration bräuchten wir das nicht doppelt!
                actionQueue.add(new Action(waitingEndTime, Status.STOPPED, Direction.NONE));
            }
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
