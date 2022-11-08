package project.elevator;

public class ElevatorConstants {
    // Constants for ALL elevators
    static final double MAX_SPEED = 8.0;    // m/s
    static final double ACCELERATION = 1.5; // m/s^2
    static final double DISTANCE_TO_ACCELERATE = (MAX_SPEED * MAX_SPEED) / (ACCELERATION * 2);
    static final double TIME_TO_ACCELERATE = MAX_SPEED / ACCELERATION;
    static final double MAX_DELTA = 0.01;
    static final double WAITING_TIME = 10;
    static final int PIXEL_WIDTH = 10;
    static final int PIXEL_HEIGHT = 10;
}
