package project.simulation;

import java.awt.*;

/**
 * Interface for all objects that should be part of the Simulation
 */
public interface SimObject {

    /**
     * fixedUpdate is called at a fixed rate.
     * @param deltaTime time since last call in seconds
     */
    void fixedUpdate(double deltaTime);

    /**
     * render() tries to achieve a specified framerate.
     * @param g the graphics context, where the object will be rendered.
     * @param interpolation is a float that gives the interpolation percentage between fixedUpdates between 0 and 1.
     */
    void render(Graphics2D g, float interpolation);

    /**
     * Every SimObject needs to provide a Status Text.
     * @return Status Text for the SimObject
     */
    String getStatusText();
}
