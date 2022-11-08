package project.simulation;

import project.Tower.Tower;
import project.View;

import java.util.concurrent.atomic.AtomicBoolean;

import static project.simulation.SimulationConstants.*;

public class Simulation implements Runnable {
    private final Tower tower;
    private final View view;
    private final AtomicBoolean isRunning = new AtomicBoolean();

    private double averageDeltaTime;
    private double tickLastUpdate;

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
        averageDeltaTime = 0.016d; // Some start value that will get more accurate over time
        double startTime = getTick();
        tickLastUpdate = startTime;
        double skipUpdateTicks = 1000d / UPDATES_PER_SECOND;
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

            update();
            if (getTick() >= nextFrameTick) {
                float interpolation = (float) (getTick() + skipUpdateTicks - nextGameTick) / (float) skipUpdateTicks;
                render(interpolation);
                nextFrameTick = getTick() + skipFrameTicks;
            }
        }

        System.out.println(averageDeltaTime);
        System.out.println("averageDeltaTime: " + String.format("%.10f", averageDeltaTime));
        view.close();
    }

    private void render(float interpolation) {
        view.render(interpolation);
    }

    public static double getTick() {
        return System.nanoTime() / 1000000d;
    }

    public void update() {
        double currentTick = getTick();
        double deltaTime = (currentTick - tickLastUpdate) / 1000d;
        tickLastUpdate = currentTick;

        view.getSimObjects().forEach(elevator -> elevator.update(deltaTime));

        averageDeltaTime = (deltaTime + averageDeltaTime) / 2d;
    }

    public void fixedUpdate() {
        view.getSimObjects().forEach(SimObject::fixedUpdate);
    }

    public void stop() {
        isRunning.set(false);
    }
}