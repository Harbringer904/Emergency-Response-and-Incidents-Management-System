package emergency;

import java.util.Scanner;

import emergency.dispatch.DispatchManager;
import emergency.exceptions.DuplicateIdException;
import emergency.exceptions.InsufficientResourceException;
import emergency.exceptions.InvalidOperationException;
import emergency.exceptions.NoSuitableUnitException;
import emergency.incidents.EvacuationIncident;
import emergency.incidents.FireIncident;
import emergency.incidents.Incident;
import emergency.incidents.InfrastructureIncident;
import emergency.incidents.MedicalIncident;
import emergency.incidents.SearchIncident;
import emergency.units.Ambulance;
import emergency.units.EvacuationBus;
import emergency.units.FireEngine;
import emergency.units.RepairVan;
import emergency.units.ResponseUnit;
import emergency.units.SearchDrone;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DispatchManager manager = new DispatchManager();
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt(scanner, "Enter option: ");
            try {
                switch (choice) {
                    case 1:
                        addUnit(scanner, manager);
                        break;
                    case 2:
                        manager.removeUnit(readLine(scanner, "Unit ID: "));
                        System.out.println("Unit removed.");
                        break;
                    case 3:
                        addIncident(scanner, manager);
                        break;
                    case 4:
                        manager.removeIncident(readLine(scanner, "Incident ID: "));
                        System.out.println("Incident removed.");
                        break;
                    case 5:
                        findUnit(scanner, manager);
                        break;
                    case 6:
                        findIncident(scanner, manager);
                        break;
                    case 7:
                        manager.listUnits();
                        break;
                    case 8:
                        manager.listIncidents();
                        break;
                    case 9:
                        manager.displayDispatchCandidates(readLine(scanner, "Incident ID: "));
                        break;
                    case 10:
                        ResponseUnit dispatched = manager.dispatchBestUnit(readLine(scanner, "Incident ID: "));
                        System.out.println("Dispatched unit " + dispatched.getId() + ".");
                        break;
                    case 11:
                        manager.resolveIncident(readLine(scanner, "Incident ID: "));
                        System.out.println("Incident resolved.");
                        break;
                    case 12:
                        manager.refuelAll(readDouble(scanner, "Refuel amount: "));
                        System.out.println("Fuel-powered units refuelled.");
                        break;
                    case 13:
                        manager.rechargeAll(readDouble(scanner, "Recharge amount: "));
                        System.out.println("Battery-powered units recharged.");
                        break;
                    case 14:
                        manager.refillAllWater(readDouble(scanner, "Water or foam amount: "));
                        System.out.println("Water carriers refilled.");
                        break;
                    case 15:
                        manager.restockAllSupplies(readInt(scanner, "Supply amount: "));
                        System.out.println("Supply carriers restocked.");
                        break;
                    case 16:
                        System.out.print(manager.generateReport());
                        break;
                    case 17:
                        manager.displayUnserviceableIncidents();
                        break;
                    case 18:
                        manager.saveState(readLine(scanner, "File prefix: "));
                        System.out.println("State saved.");
                        break;
                    case 19:
                        manager.loadState(readLine(scanner, "File prefix: "));
                        System.out.println("State loaded.");
                        break;
                    case 20:
                        running = false;
                        System.out.println("Goodbye.");
                        break;
                    default:
                        System.out.println("Unknown option. Choose 1-20.");
                        break;
                }
            } catch (InvalidOperationException | DuplicateIdException
                    | InsufficientResourceException | NoSuitableUnitException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error: a numeric field was not a valid number.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("================ EMERGENCY DISPATCH SYSTEM ================");
        System.out.println("1.  Add Response Unit");
        System.out.println("2.  Remove Response Unit");
        System.out.println("3.  Report New Incident");
        System.out.println("4.  Remove Incident");
        System.out.println("5.  Find Response Unit");
        System.out.println("6.  Find Incident");
        System.out.println("7.  List All Response Units");
        System.out.println("8.  List All Incidents");
        System.out.println("9.  Preview Dispatch Candidates");
        System.out.println("10. Dispatch Best Unit");
        System.out.println("11. Resolve Incident");
        System.out.println("12. Refuel Fuel-Powered Units");
        System.out.println("13. Recharge Battery-Powered Units");
        System.out.println("14. Refill Water Carriers");
        System.out.println("15. Restock Supply Carriers");
        System.out.println("16. Generate System Report");
        System.out.println("17. Display Unserviceable Incidents");
        System.out.println("18. Save State");
        System.out.println("19. Load State");
        System.out.println("20. Exit");
        System.out.println("===========================================================");
    }

    private static void findUnit(Scanner scanner, DispatchManager manager) {
        ResponseUnit unit = manager.findUnit(readLine(scanner, "Unit ID: "));
        if (unit == null) {
            System.out.println("No response unit with that ID.");
        } else {
            unit.display();
        }
    }

    private static void findIncident(Scanner scanner, DispatchManager manager) {
        Incident incident = manager.findIncident(readLine(scanner, "Incident ID: "));
        if (incident == null) {
            System.out.println("No incident with that ID.");
        } else {
            incident.display();
        }
    }

    private static void addUnit(Scanner scanner, DispatchManager manager)
            throws InvalidOperationException, DuplicateIdException {
        System.out.println("Types: AMBULANCE, FIRE_ENGINE, REPAIR_VAN, SEARCH_DRONE, EVAC_BUS");
        String type = readLine(scanner, "Unit type: ").toUpperCase();
        String id = readLine(scanner, "ID: ");
        String name = readLine(scanner, "Name: ");
        double speed = readDouble(scanner, "Max speed (km/h): ");
        ResponseUnit unit;
        if ("SEARCH_DRONE".equals(type)) {
            unit = new SearchDrone(id, name, speed);
        } else {
            double traffic = readDouble(scanner, "Traffic factor (>= 1.0): ");
            switch (type) {
                case "AMBULANCE":
                    unit = new Ambulance(id, name, speed, traffic);
                    break;
                case "FIRE_ENGINE":
                    unit = new FireEngine(id, name, speed, traffic);
                    break;
                case "REPAIR_VAN":
                    unit = new RepairVan(id, name, speed, traffic);
                    break;
                case "EVAC_BUS":
                    unit = new EvacuationBus(id, name, speed, traffic);
                    break;
                default:
                    throw new InvalidOperationException("Unsupported unit type: " + type);
            }
        }
        manager.addUnit(unit);
        System.out.println("Unit added.");
    }

    private static void addIncident(Scanner scanner, DispatchManager manager)
            throws InvalidOperationException, DuplicateIdException {
        System.out.println("Types: MEDICAL, FIRE, INFRASTRUCTURE, SEARCH, EVACUATION, GENERAL");
        String type = readLine(scanner, "Incident type: ").toUpperCase();
        String id = readLine(scanner, "ID: ");
        String description = readLine(scanner, "Description: ");
        double distance = readNonNegativeDouble(scanner, "Distance from base (km): ");
        int severity = readSeverity(scanner);
        Incident incident;
        switch (type) {
            case "MEDICAL":
                incident = new MedicalIncident(id, description, distance, severity,
                        readInt(scanner, "Patient count: "),
                        readInt(scanner, "Critical patients: "));
                break;
            case "FIRE":
                incident = new FireIncident(id, description, distance, severity,
                        readNonNegativeDouble(scanner, "Affected area: "),
                        readYesNo(scanner, "Hazardous material (y/n): "));
                break;
            case "INFRASTRUCTURE":
                incident = new InfrastructureIncident(id, description, distance, severity,
                        readInt(scanner, "Affected users: "),
                        readYesNo(scanner, "Critical service (y/n): "));
                break;
            case "SEARCH":
                incident = new SearchIncident(id, description, distance, severity,
                        readInt(scanner, "Missing persons: "),
                        readNonNegativeDouble(scanner, "Search area: "));
                break;
            case "EVACUATION":
                incident = new EvacuationIncident(id, description, distance, severity,
                        readInt(scanner, "People to move: "),
                        readNonNegativeDouble(scanner, "Remaining safe hours: "));
                break;
            case "GENERAL":
                incident = new Incident(id, description, distance, severity);
                break;
            default:
                throw new InvalidOperationException("Unsupported incident type: " + type);
        }
        manager.addIncident(incident);
        System.out.println("Incident reported.");
    }

    private static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine();
        while (line != null && line.trim().isEmpty()) {
            System.out.print(prompt);
            line = scanner.nextLine();
        }
        return line.trim();
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Double.parseDouble(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a number.");
            }
        }
    }

    private static double readNonNegativeDouble(Scanner scanner, String prompt) {
        while (true) {
            double value = readDouble(scanner, prompt);
            if (value >= 0) {
                return value;
            }
            System.out.println("Value cannot be negative.");
        }
    }

    private static int readSeverity(Scanner scanner) {
        while (true) {
            int severity = readInt(scanner, "Severity (1-5): ");
            if (severity >= 1 && severity <= 5) {
                return severity;
            }
            System.out.println("Severity must be between 1 and 5.");
        }
    }

    private static boolean readYesNo(Scanner scanner, String prompt) {
        while (true) {
            String line = readLine(scanner, prompt).toLowerCase();
            if ("y".equals(line) || "yes".equals(line)) {
                return true;
            }
            if ("n".equals(line) || "no".equals(line)) {
                return false;
            }
            System.out.println("Enter y or n.");
        }
    }
}
