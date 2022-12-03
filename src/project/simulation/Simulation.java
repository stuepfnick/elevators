package project.simulation;

import project.tower.Tower;
import project.View;

import java.util.concurrent.atomic.AtomicBoolean;

import static project.simulation.SimulationConstants.*;

/**
 * The Simulation implementing a Runnable to be executed as Thread.
 */
public class Simulation implements Runnable {
    private final Tower tower;
    private final View view;
    private final AtomicBoolean isRunning = new AtomicBoolean();

    private double averageFPS, tickLastFrame;

    public Simulation(Tower tower) {
        this.tower = tower;
        view = new View();
        view.getSimObjects().addAll(tower.getElevators());
    }

    public Tower getTower() {
        return tower;
    }

    /**
     * Holds the main loop
     * Does the fixedUpdate at a certain rate, even if it can not hold the FPS.<br>
     * Also calculates an interpolation between fixedUpdates (0.0 - 1.0), so
     * the rendering can be smooth, even if the fixedUpdate rate is much lower than FPS.
     */
    @Override
    public void run() {
        averageFPS = FRAMES_PER_SECOND;
        double startTime = getTick();
        tickLastFrame = startTime;
        double skipUpdateTicks = 1000d / FIXED_UPDATES_PER_SECOND;
        double nextGameTick = startTime + skipUpdateTicks;
        double skipFrameTicks = 1000d / FRAMES_PER_SECOND;
        double nextFrameTick = startTime + skipFrameTicks;
        isRunning.set(true);

        while (isRunning.get()) {
            int loops = 0;
            int MAX_FRAME_SKIP = 5;
            while (getTick() > nextGameTick && loops < MAX_FRAME_SKIP) {
                fixedUpdate();
                nextGameTick += skipUpdateTicks;
                loops++;
            }

            double currentFrameTick = getTick();
            if (currentFrameTick >= nextFrameTick) {
                nextFrameTick = currentFrameTick + skipFrameTicks;
                float interpolation = (float) (currentFrameTick + skipUpdateTicks - nextGameTick) / (float) skipUpdateTicks;
                render(interpolation);
            }
        }

        System.out.println();
        System.out.println("averageFPS: " + averageFPS);
        view.close();
    }

    /**
     * Calculates currentFPS and averageFPS and
     * calls the render method for the view.
     * @param interpolation gets passed on from main sim loop
     */
    private void render(float interpolation) {
        double currentTick = getTick();
        double currentFPS = 1000d / (currentTick - tickLastFrame);
        tickLastFrame = currentTick;
        averageFPS = (currentFPS + averageFPS) / 2d;

        view.render(interpolation);
    }

    /**
     * A static method for getting the current tick as double.
     * @return current tick as double
     */
    public static double getTick() {
        return System.nanoTime() / 1000000d;
    }

    /**
     * Updates the tower and calls fixedUpdate for each SimObject.<br>
     * Passes deltaTime to all objects.
     */
    public void fixedUpdate() {
        tower.update();
        view.getSimObjects().forEach(simObject -> simObject.fixedUpdate(FIXED_DELTA_TIME));
    }

    /**
     * Stops the simulation.
     */
    public void stop() {
        isRunning.set(false);
    }
}
