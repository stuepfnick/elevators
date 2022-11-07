public class Action {
    private final double duration;
    private final Status status;
    private final Direction direction;

    public Action(double duration, Status status, Direction direction) {
        this.duration = duration;
        this.status = status;
        this.direction = direction;
    }

    public double getDuration() {
        return duration;
    }

    public Status getStatus() {
        return status;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return "Action{" +
                "endTime=" + duration +
                ", status=" + status +
                ", direction=" + direction +
                '}';
    }
}
