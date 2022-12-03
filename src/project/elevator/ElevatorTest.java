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

/**
 * Does some Tests for the Elevator Class
 */
public class ElevatorTest {

    @ParameterizedTest
    @MethodSource("timeToFloorParameters_requestQueue")
    void calculateTimeToFloor_requestQueue(int startingFloor, List<Request> requests, Request mewRequest, double expectedTime) {
        Elevator elevator = new Elevator(0, startingFloor);
        for (var request : requests) {
            elevator.addRequest(request);
        }
        var result = elevator.calculateTimeToRequest(mewRequest);
        assertEquals(expectedTime, result, 0.00001d);
    }

    @ParameterizedTest
    @MethodSource("timeToFloorParameters_requestQueue")
    void calculateTimeToFloor_actionAndRequestQueue(int startingFloor, List<Request> requests, Request mewRequest, double expectedTime) {
        Elevator elevator = new Elevator(0, startingFloor);
        for (var request : requests) {
            elevator.addRequest(request);
        }
        elevator.fixedUpdate(0.02d); // Convert first Request to Actions
        var result = elevator.calculateTimeToRequest(mewRequest);
        assertEquals(expectedTime, result, 0.0001d); // lower delta as very little time has passed
    }

    private static Stream<Arguments> timeToFloorParameters_requestQueue() {
        return Stream.of(
                Arguments.of( // 1
                        0, List.of(), new Request(0, 1), 0d
                ), Arguments.of( // 2
                        15, List.of(), new Request(0, 1), Elevator.calculateTravelTime(15, 0) + WAITING_TIME
                ), Arguments.of( // 3
                        0, List.of(new Request(0, 15)), new Request(0, 1),
                        WAITING_TIME + Elevator.calculateTravelTime(0, 15) + WAITING_TIME + Elevator.calculateTravelTime(15, 0) + WAITING_TIME
                ), Arguments.of( // 4
                        0, List.of(new Request(15, 0)), new Request(0, 1),
                        Elevator.calculateTravelTime(0, 15) + WAITING_TIME + Elevator.calculateTravelTime(15, 0) + WAITING_TIME
                ), Arguments.of( // 5
                        0, List.of(new Request(15, 0)), new Request(20, 0),
                        Elevator.calculateTravelTime(0, 15) + WAITING_TIME + Elevator.calculateTravelTime(15, 0) + WAITING_TIME
                                + Elevator.calculateTravelTime(0, 20) + WAITING_TIME
                ), Arguments.of( // 6
                        0, List.of(new Request(0, 15)), new Request(20, 0),
                        WAITING_TIME + Elevator.calculateTravelTime(0, 15) + WAITING_TIME + Elevator.calculateTravelTime(15, 20) + WAITING_TIME
                ), Arguments.of( // 7
                        0, List.of(new Request(0, 16), new Request(20, 0)), new Request(25, 0),
                        WAITING_TIME + Elevator.calculateTravelTime(0, 16) + WAITING_TIME + Elevator.calculateTravelTime(20, 0) + WAITING_TIME
                                + Elevator.calculateTravelTime(16, 20) + WAITING_TIME + Elevator.calculateTravelTime(0, 25) + WAITING_TIME
                ), Arguments.of( // 8
                        20, List.of(new Request(0, 25), new Request(23, 0)), new Request(0, 1),
                        Elevator.calculateTravelTime(20, 0) + WAITING_TIME + Elevator.calculateTravelTime(0, 25) + WAITING_TIME
                                + Elevator.calculateTravelTime(25, 23) + WAITING_TIME + Elevator.calculateTravelTime(23, 0) + WAITING_TIME
                ), Arguments.of( // 9
                        0, List.of(new Request(0, 20), new Request(0, 30)), new Request(20, 0),
                        WAITING_TIME + Elevator.calculateTravelTime(0, 20) + WAITING_TIME
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
            "0, 15, 12.833333333333333",
            "20, 0, 15.333333333333333",
            "0, 25, 17.833333333333333",
            "30, 0, 20.333333333333333",
            "0, 35, 22.833333333333333",
            "40, 0, 25.333333333333333",
            "0, 45, 27.833333333333333",
            "0, 50, 30.333333333333333",
            "55, 0, 32.833333333333333"
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
