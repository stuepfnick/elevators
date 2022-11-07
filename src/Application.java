import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Application {
    public static Tower tower;
    public static void main(String[] args) {
        tower = new Tower();
        Simulation simulation = new Simulation(tower);
        Thread t = new Thread(simulation);
        t.start();

        var elevators = simulation.getTower().getElevators();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("command:");
            String input = scanner.nextLine();

            if (input.isEmpty()) {
                simulation.stop();
                break;
            }
            if (input.toLowerCase().startsWith("addrequest")) {
                String[] inputs = input.substring(11).split("\\s");
                addRequest(inputs);
            } else if (input.toLowerCase().startsWith("ar")) {
                String[] inputs = input.substring(3).split("\\s");
                addRequest(inputs);
            } else if (input.toLowerCase().startsWith("add")) {
                String[] inputs = input.substring(4).split("\\s");
                List<Integer> newDestinations = new ArrayList<>();
                for (String value : inputs) {
                    try {
                        int number = Integer.parseInt(value);
                        number = Math.max(Math.min(number, Tower.NUMBER_OF_FLOORS), 0);
                        newDestinations.add(number);
                    } catch (NumberFormatException e) {
                        System.out.println(value + " is no whole number!");
                    }
                }
                int index = newDestinations.remove(0);
                if (index >= 0 && index < Tower.NUMBER_OF_ELEVATORS) {
                    elevators.get(index).addDestinationFloors(newDestinations);
                } else {
                    System.out.println("There is no elevator " + index);
                }
            } else {
                System.out.println("Unknown command!");
            }
        }
        //System.exit(0);
    }
    private static void addRequest(String[] inputs) {
        if (inputs.length != 2) {
            System.out.println("You need to enter 2 floors for a request!");
            return;
        }
        List<Integer> request = new ArrayList<>();
        for (String value : inputs) {
            try {
                int number = Integer.parseInt(value);
                number = Math.max(Math.min(number, Tower.NUMBER_OF_FLOORS), 0);
                request.add(number);
            } catch (NumberFormatException e) {
                System.out.println(value + " is no whole number!");
            }
        }
        tower.addRequest(request.get(0), request.get(1));
    }
}
