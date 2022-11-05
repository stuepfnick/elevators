public class Action {
    private final double endTime;
    private final Status status;
    private final Direction direction;

    public Action(double endTime, Status status, Direction direction) {
        this.endTime = endTime;
        this.status = status;
        this.direction = direction;
    }

    public double getEndTime() {
        return endTime;
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
                "endTime=" + endTime +
                ", status=" + status +
                ", direction=" + direction +
                '}';
    }
}
