package project.elevator;

import java.util.Objects;

public class Request {
    private final int originFloor;
    private final int destinationFloor;
    private int numberOfPassengers;

    public Request(int originFloor, int destinationFloor) {
        this.originFloor = originFloor;
        this.destinationFloor = destinationFloor;
        numberOfPassengers = 1;
    }

    public int getOriginFloor() {
        return originFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public int getNumberOfPassengers() {
        return numberOfPassengers;
    }

    public void addPassenger() {
        numberOfPassengers++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return originFloor == request.originFloor && destinationFloor == request.destinationFloor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originFloor, destinationFloor);
    }

    @Override
    public String toString() {
        return originFloor +
                "-" + destinationFloor +
                " (" + numberOfPassengers +
                ')';
    }
}
