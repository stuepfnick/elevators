package project.elevator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import project.tower.TowerConstants;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static project.elevator.ElevatorConstants.*;

public class ElevatorTest {

    @ParameterizedTest
    @MethodSource("timeToFloorParameters_requestQueue")
    void calculateTimeToFloor_requestQueue(int startingFloor, List<Request> requests, int destinationFloor, double expectedTime) {
        Elevator elevator = new Elevator(0, startingFloor);
        for (var request : requests) {
            elevator.addRequest(request);
        }
        var result = elevator.calculateTimeToFloor(destinationFloor);
        assertEquals(expectedTime, result, 0.00001d);
    }

    @ParameterizedTest
    @MethodSource("timeToFloorParameters_requestQueue")
    void calculateTimeToFloor_actionAndRequestQueue(int startingFloor, List<Request> requests, int destinationFloor, double expectedTime) {
        Elevator elevator = new Elevator(0, startingFloor);
        for (var request : requests) {
            elevator.addRequest(request);
        }
        elevator.update(0.00000001d); // Convert first Request to Actions
        var result = elevator.calculateTimeToFloor(destinationFloor);
        assertEquals(expectedTime, result, 0.0005d);
    }

    private static Stream<Arguments> timeToFloorParameters_requestQueue() {
        return Stream.of(
                Arguments.of(
                        0, List.of(), 0, 0d
                ), Arguments.of(
                        15, List.of(), 0, Elevator.calculateTravelTime(15, 0) + WAITING_TIME
                ), Arguments.of(
                        0, List.of(new Request(0, 15)), 0,
                        WAITING_TIME + Elevator.calculateTravelTime(0, 15) + WAITING_TIME + Elevator.calculateTravelTime(15, 0) + WAITING_TIME
                ), Arguments.of(
                        0, List.of(new Request(15, 0)), 0,
                        Elevator.calculateTravelTime(0, 15) + WAITING_TIME + Elevator.calculateTravelTime(15, 0) + WAITING_TIME
                ), Arguments.of(
                        0, List.of(new Request(15, 0)), 20,
                        Elevator.calculateTravelTime(0, 15) + WAITING_TIME + Elevator.calculateTravelTime(15, 0) + WAITING_TIME + Elevator.calculateTravelTime(0, 20) + WAITING_TIME
                ), Arguments.of(
                        0, List.of(new Request(0, 15)), 20,
                        WAITING_TIME + Elevator.calculateTravelTime(0, 15) + WAITING_TIME + Elevator.calculateTravelTime(15, 20) + WAITING_TIME
                ), Arguments.of(
                        0, List.of(new Request(0, 16), new Request(20, 0)), 25,
                        WAITING_TIME + Elevator.calculateTravelTime(0, 16) + WAITING_TIME + Elevator.calculateTravelTime(20, 0) + WAITING_TIME
                                + Elevator.calculateTravelTime(16, 20) + WAITING_TIME + Elevator.calculateTravelTime(0, 25) + WAITING_TIME
                ));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, 0d",
            "0, 1, 3.2659863237", // VALUES need to be adjusted if Constants change
            "0, 2, 4.6188021535",
            "0, 3, 5.6568542495",
            "0, 4, 6.5319726474",
            "0, 5, 7.3029674334",
            "0, 10, 10.3279555899",
            "0, 11, 10.833333333333333",
            "0, 12, 11.333333333333333",
            "0, 15, 12.833333333333333"
    })
    void calculateTravelTime(int originFloor, int destinationFloor, double expectedTime) {
        var result = Elevator.calculateTravelTime(originFloor, destinationFloor);
        assertEquals(expectedTime, result, 0.0000000001d);
    }

    @ParameterizedTest
    @MethodSource("travelTimeParameters")
    void calculateTravelTime_withConstants(int originFloor, int destinationFloor, double expectedTime) {
        var result = Elevator.calculateTravelTime(originFloor, destinationFloor);
        assertEquals(expectedTime, result, 0.0000000001d);
    }

    private static Stream<Arguments> travelTimeParameters() {
        return Stream.of(
                Arguments.of(0, 1, Math.sqrt(TowerConstants.FLOOR_HEIGHT / ACCELERATION) * 2),
                Arguments.of(0, 10, Math.sqrt(10 * TowerConstants.FLOOR_HEIGHT / ACCELERATION) * 2),
                Arguments.of(12, 1, (11 * TowerConstants.FLOOR_HEIGHT - DISTANCE_TO_ACCELERATE * 2) / MAX_SPEED + TIME_TO_ACCELERATE * 2),
                Arguments.of(0, 15, (15 * TowerConstants.FLOOR_HEIGHT - DISTANCE_TO_ACCELERATE * 2) / MAX_SPEED + TIME_TO_ACCELERATE * 2),
                Arguments.of(43, 0, (43 * TowerConstants.FLOOR_HEIGHT - DISTANCE_TO_ACCELERATE * 2) / MAX_SPEED + TIME_TO_ACCELERATE * 2)
        );
    }
}
