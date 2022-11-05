public class Simulation implements Runnable {
    public static final int UPDATES_PER_SECOND = 50;
    public static final int FRAMES_PER_SECOND = 60;
    public static final double FIXED_DELTA_TIME = 1d / UPDATES_PER_SECOND;
    private final double SKIP_UPDATE_TICKS = 1000 / UPDATES_PER_SECOND;
    private final double SKIP_FRAME_TICKS = 1000 / FRAMES_PER_SECOND;
    private final int MAX_FRAME_SKIP = 5;
    private final Tower tower;
    private final Window window;
    private boolean isRunning;
    private double startTime;

    private double averageDeltaTime;
    private double tickLastUpdate;

    public Simulation(Tower tower) {
        this.tower = tower;
        window = new Window();
        window.getRenderObjects().addAll(tower.getElevators());
    }

    public Tower getTower() {
        return tower;
    }

    @Override
    public void run() {
        averageDeltaTime = 0.01d; // Some start value that will get more accurate over time
        startTime = getTick();
        tickLastUpdate = startTime;
        double nextGameTick = startTime + SKIP_UPDATE_TICKS;
        double nextFrameTick = startTime + SKIP_FRAME_TICKS;
        isRunning = true;

        while (isRunning) {
            int loops = 0;
            while (getTick() > nextGameTick && loops < MAX_FRAME_SKIP) {
                fixedUpdate();

                nextGameTick += SKIP_UPDATE_TICKS;
                loops++;
            }

            if (getTick() >= nextFrameTick) {
                update();
                float interpolation = (float) (getTick() + SKIP_UPDATE_TICKS - nextGameTick) / (float) SKIP_UPDATE_TICKS;
                render(interpolation);
                nextFrameTick = getTick() + SKIP_FRAME_TICKS;
            }
        }

        System.out.println("averageDeltaTime: " + String.format("%.10f", averageDeltaTime));
    }

    private void render(float interpolation) {
        window.render(interpolation);
    }

    public static double getTick() {
        return System.nanoTime() / 1000000d;
    }

    public void update() {
        double deltaTime = (getTick() - tickLastUpdate) / 1000d;
        tickLastUpdate = getTick();

        tower.getElevators().forEach(elevator -> elevator.update(deltaTime));

        averageDeltaTime = (deltaTime + averageDeltaTime) / 2d;
    }

    public void fixedUpdate() {
        tower.getElevators().forEach(Elevator::fixedUpdate);
    }

    public void stop() {
        isRunning = false;
    }
}
