package project;

import project.tower.Tower;
import project.tower.TowerConstants;
import project.simulation.Simulation;

import java.util.*;

/**
 * Elevators coding challenge for DC tower
 * @author Stefan Sigmund
 */

public class Application {
    private final Simulation simulation;

    /**
     * Application entry point.
     * @param args ignored
     */
    public static void main(String[] args) {
        new Application();
    }

    /**
     * Creates new Simulation with new Tower and starts the Simulation
     * as a new thread, so you can still enter commands in command line.
     */
    public Application() {
        simulation = new Simulation(new Tower());
        Thread t = new Thread(simulation);
        t.start();

        askForInput();
    }

    /**
     * The input main loop.<br>
     * Here you enter the commands to add new Requests.
     */
    public void askForInput() {
        var elevators = simulation.getTower().getElevators();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Available commands: 'addRequest #FROM #TO' (short 'ar') or 'addRandomRequest' (short 'arr')");
        while (true) {
            System.out.println("command:");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                simulation.stop();
                break;
            }

            if (input.equalsIgnoreCase("arr") || input.equalsIgnoreCase("addRandomRequest")) {
                addRandomRequest();

            } else if (input.toLowerCase().startsWith("arr") || input.toLowerCase().startsWith("addrandomrequest")) {
                List<String> inputs = new LinkedList<>(List.of(input.split("\\s")));
                inputs.remove(0);
                if (inputs.size() != 1) {
                    System.out.println("This command needs 1 parameter!");
                } else {
                    String s = inputs.get(0);
                    try {
                        int numberOfRequests = Integer.parseInt(s);
                        if (numberOfRequests > 12) {
                            System.out.println("Limited to 12 random requests at once.");
                            continue;
                        }
                        for (int i = 0; i < numberOfRequests; i++) {
                            addRandomRequest();
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(s + " is no whole number!");
                    }
                }

            } else if (input.toLowerCase().startsWith("addrequest") || input.toLowerCase().startsWith("ar")) {
                List<String> inputs = new LinkedList<>(List.of(input.split("\\s")));
                inputs.remove(0);
                addRequest(inputs);

            } else {
                System.out.println("Unknown command!");
            }
        }
    }

    /**
     * Method for adding a random Request, so askForInput does not get to overloaded.
     */
    private void addRandomRequest() {
        Random random = new Random();
        int randomFloor = random.nextInt(TowerConstants.NUMBER_OF_FLOORS) + 1;
        if (random.nextBoolean()) {
            simulation.getTower().addRequest(0, randomFloor);
        } else {
            simulation.getTower().addRequest(randomFloor, 0);
        }
    }

    /**
     * Method for adding a regular Request, so askForInput does not get to overloaded.
     * @param inputs as a List of Strings (only works with a size of 2)
     */
    private void addRequest(List<String> inputs) {
        if (inputs.size() != 2) {
            System.out.println("You need to enter 2 floors for a request!");
            return;
        }
        List<Integer> numbers = new ArrayList<>();
        for (String value : inputs) {
            try {
                int number = Integer.parseInt(value);
                if (number > TowerConstants.NUMBER_OF_FLOORS || number < 0) {
                    System.out.println(number + " is not a valid floor number");
                    return;
                }
                numbers.add(number);
            } catch (NumberFormatException e) {
                System.out.println(value + " is no whole number!");
                return;
            }
        }
        simulation.getTower().addRequest(numbers.get(0), numbers.get(1));
    }
}
