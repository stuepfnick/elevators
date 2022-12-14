package project.simulation;

/**
 * Constants needed for the Simulation
 */
public class SimulationConstants {
    public static final int FIXED_UPDATES_PER_SECOND = 25;
    public static final int FRAMES_PER_SECOND = 60;
    public static final int FLOOR_HEIGHT_PIXEL = 12;
    public static final int ELEVATOR_SPACING_PIXEL = 12;

    // Calculated
    public static final double FIXED_DELTA_TIME = 1d / FIXED_UPDATES_PER_SECOND;
}
