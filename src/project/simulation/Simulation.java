package project.simulation;

import project.tower.Tower;
import project.View;

import java.util.concurrent.atomic.AtomicBoolean;

import static project.simulation.SimulationConstants.*;

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

    private void render(float interpolation) {
        double currentTick = getTick();
        double currentFPS = 1000d / (currentTick - tickLastFrame);
        tickLastFrame = currentTick;
        averageFPS = (currentFPS + averageFPS) / 2d;

        view.render(interpolation);
    }

    public static double getTick() {
        return System.nanoTime() / 1000000d;
    }

    public void fixedUpdate() {
        tower.fixedUpdate();
        view.getSimObjects().forEach(SimObject::fixedUpdate);
    }

    public void stop() {
        isRunning.set(false);
    }
}
