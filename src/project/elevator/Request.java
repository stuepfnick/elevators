package project.elevator;

import java.util.Objects;

/**
 * Data class for holding a Request.
 */
public class Request {
    private final int originFloor;
    private final int destinationFloor;
    private int numberOfPassengers;

    /**
     * Constructor always takes 2 parameters.
     * @param originFloor from floor
     * @param destinationFloor to floor
     */
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

    /**
     * Simply increase passenger count by 1.
     */
    public void addPassenger() {
        numberOfPassengers++;
    }

    /**
     * Only checks the other object for origin and destination. <br>
     * Does <b>not</b> compare numberOfPassengers, as Requests should be considered as equal from just the floors.
     * (independent of passenger count)
     * @param o other Object
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return originFloor == request.originFloor && destinationFloor == request.destinationFloor;
    }

    /**
     * hashCode also only for originFloor and destinationFloor.
     * @return the hash as int
     */
    @Override
    public int hashCode() {
        return Objects.hash(originFloor, destinationFloor);
    }

    /**
     * toString method has a custom formatting
     * @return a String like: "0-15 (1)"
     */
    @Override
    public String toString() {
        return originFloor +
                "-" + destinationFloor +
                " (" + numberOfPassengers +
                ')';
    }
}
