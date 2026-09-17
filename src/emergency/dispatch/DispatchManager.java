package emergency.dispatch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import emergency.capabilities.BatteryPowered;
import emergency.capabilities.FuelPowered;
import emergency.capabilities.SupplyCarrier;
import emergency.capabilities.WaterCarrier;
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

public class DispatchManager {
    public static final int DEFAULT_MAX_UNITS = 50;
    public static final int DEFAULT_MAX_INCIDENTS = 100;

    private ResponseUnit[] units;
    private Incident[] incidents;
    private int unitCount;
    private int incidentCount;
    private DispatchPolicy policy;

    public DispatchManager() {
        this(DEFAULT_MAX_UNITS, DEFAULT_MAX_INCIDENTS, new DispatchPolicy());
    }

    public DispatchManager(int maxUnits, int maxIncidents, DispatchPolicy policy) {
        this.units = new ResponseUnit[maxUnits];
        this.incidents = new Incident[maxIncidents];
        this.unitCount = 0;
        this.incidentCount = 0;
        this.policy = policy;
    }

    public DispatchPolicy getPolicy() {
        return policy;
    }

    public int getUnitCount() {
        return unitCount;
    }

    public int getIncidentCount() {
        return incidentCount;
    }

    public int getMaxUnits() {
        return units.length;
    }

    public int getMaxIncidents() {
        return incidents.length;
    }

    public void addUnit(ResponseUnit unit) throws InvalidOperationException, DuplicateIdException {
        if (unit == null) {
            throw new InvalidOperationException("Cannot add a null response unit.");
        }
        if (unitCount >= units.length) {
            throw new InvalidOperationException("Response unit array is full.");
        }
        if (findUnit(unit.getId()) != null) {
            throw new DuplicateIdException("Duplicate unit ID: " + unit.getId());
        }
        units[unitCount] = unit;
        unitCount++;
    }

    public void addIncident(Incident incident) throws InvalidOperationException, DuplicateIdException {
        if (incident == null) {
            throw new InvalidOperationException("Cannot add a null incident.");
        }
        if (incidentCount >= incidents.length) {
            throw new InvalidOperationException("Incident array is full.");
        }
        if (findIncident(incident.getId()) != null) {
            throw new DuplicateIdException("Duplicate incident ID: " + incident.getId());
        }
        incidents[incidentCount] = incident;
        incidentCount++;
    }

    public ResponseUnit findUnit(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < unitCount; i++) {
            if (units[i].getId().equals(id)) {
                return units[i];
            }
        }
        return null;
    }

    public Incident findIncident(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < incidentCount; i++) {
            if (incidents[i].getId().equals(id)) {
                return incidents[i];
            }
        }
        return null;
    }

    public void removeUnit(String id) throws InvalidOperationException {
        int index = indexOfUnit(id);
        if (index < 0) {
            throw new InvalidOperationException("No response unit with ID " + id + ".");
        }
        if (!units[index].isAvailable()) {
            throw new InvalidOperationException("Cannot remove a unit that is currently assigned.");
        }
        for (int i = index; i < unitCount - 1; i++) {
            units[i] = units[i + 1];
        }
        units[unitCount - 1] = null;
        unitCount--;
    }

    public void removeIncident(String id) throws InvalidOperationException {
        int index = indexOfIncident(id);
        if (index < 0) {
            throw new InvalidOperationException("No incident with ID " + id + ".");
        }
        if (Incident.STATUS_ASSIGNED.equals(incidents[index].getStatus())) {
            throw new InvalidOperationException("Cannot remove an ASSIGNED incident.");
        }
        for (int i = index; i < incidentCount - 1; i++) {
            incidents[i] = incidents[i + 1];
        }
        incidents[incidentCount - 1] = null;
        incidentCount--;
    }

    private int indexOfUnit(String id) {
        for (int i = 0; i < unitCount; i++) {
            if (units[i].getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private int indexOfIncident(String id) {
        for (int i = 0; i < incidentCount; i++) {
            if (incidents[i].getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public ResponseUnit dispatchBestUnit(String incidentId)
            throws InvalidOperationException, NoSuitableUnitException, InsufficientResourceException {
        Incident incident = requireOpenIncident(incidentId);
        ResponseUnit best = null;
        double bestScore = 0.0;
        for (int i = 0; i < unitCount; i++) {
            ResponseUnit unit = units[i];
            if (unit.ineligibilityReason(incident) != null) {
                continue;
            }
            double score = unit.calculateDispatchScore(incident, policy);
            if (best == null || score < bestScore
                    || (score == bestScore && unit.getId().compareTo(best.getId()) < 0)) {
                best = unit;
                bestScore = score;
            }
        }
        if (best == null) {
            throw new NoSuitableUnitException("No suitable unit for incident " + incidentId + ".");
        }
        best.assignIncident(incident.getId());
        incident.assignUnit(best.getId());
        best.move(incident.getDistanceFromBase());
        return best;
    }

    public void displayDispatchCandidates(String incidentId) throws InvalidOperationException {
        Incident incident = requireExistingIncident(incidentId);
        System.out.println("Incident: " + incident.getId());
        System.out.println("Description: " + incident.getDescription());
        System.out.println("Severity: " + incident.getSeverity());
        ResponseUnit best = null;
        double bestScore = 0.0;
        for (int i = 0; i < unitCount; i++) {
            ResponseUnit unit = units[i];
            String reason = unit.ineligibilityReason(incident);
            System.out.println(unit.getId());
            if (reason != null) {
                System.out.println("REJECTED");
                System.out.println(reason);
            } else {
                double score = unit.calculateDispatchScore(incident, policy);
                System.out.println("ELIGIBLE");
                System.out.println("Score = " + score);
                if (best == null || score < bestScore
                        || (score == bestScore && unit.getId().compareTo(best.getId()) < 0)) {
                    best = unit;
                    bestScore = score;
                }
            }
        }
        if (best == null) {
            System.out.println("Best candidate: none");
        } else {
            System.out.println("Best candidate: " + best.getId());
        }
    }

    public void resolveIncident(String incidentId)
            throws InvalidOperationException, InsufficientResourceException {
        Incident incident = requireExistingIncident(incidentId);
        if (!Incident.STATUS_ASSIGNED.equals(incident.getStatus())) {
            throw new InvalidOperationException("Incident " + incidentId + " is not ASSIGNED.");
        }
        ResponseUnit unit = findUnit(incident.getAssignedUnitId());
        if (unit == null) {
            throw new InvalidOperationException("Assigned unit is missing from the system.");
        }
        unit.performWork(incident);
        unit.move(incident.getDistanceFromBase());
        unit.releaseIncident();
        unit.incrementCompletedIncidents();
        incident.resolve();
    }

    public void refuelAll(double amount) throws InvalidOperationException {
        for (int i = 0; i < unitCount; i++) {
            if (units[i] instanceof FuelPowered) {
                FuelPowered fuelled = (FuelPowered) units[i];
                fuelled.refuel(amount);
            }
        }
    }

    public void rechargeAll(double amount) throws InvalidOperationException {
        for (int i = 0; i < unitCount; i++) {
            if (units[i] instanceof BatteryPowered) {
                BatteryPowered battery = (BatteryPowered) units[i];
                battery.recharge(amount);
            }
        }
    }

    public void refillAllWater(double amount) throws InvalidOperationException {
        for (int i = 0; i < unitCount; i++) {
            if (units[i] instanceof WaterCarrier) {
                WaterCarrier water = (WaterCarrier) units[i];
                water.refillWater(amount);
            }
        }
    }

    public void restockAllSupplies(int amount) throws InvalidOperationException {
        for (int i = 0; i < unitCount; i++) {
            if (units[i] instanceof SupplyCarrier) {
                SupplyCarrier supplies = (SupplyCarrier) units[i];
                supplies.loadSupplies(amount);
            }
        }
    }

    public String generateReport() {
        int available = 0;
        int busy = 0;
        double totalDistance = 0.0;
        int completed = 0;
        int open = 0;
        int assigned = 0;
        int resolved = 0;
        int highUnresolved = 0;
        ResponseUnit topUnit = null;
        Incident topOpen = null;
        int unserviceable = 0;

        for (int i = 0; i < unitCount; i++) {
            ResponseUnit unit = units[i];
            if (unit.isAvailable()) {
                available++;
            } else {
                busy++;
            }
            totalDistance += unit.getTotalDistanceTravelled();
            completed += unit.getCompletedIncidents();
            if (topUnit == null
                    || unit.getCompletedIncidents() > topUnit.getCompletedIncidents()
                    || (unit.getCompletedIncidents() == topUnit.getCompletedIncidents()
                    && unit.getId().compareTo(topUnit.getId()) < 0)) {
                topUnit = unit;
            }
        }

        for (int i = 0; i < incidentCount; i++) {
            Incident incident = incidents[i];
            if (Incident.STATUS_OPEN.equals(incident.getStatus())) {
                open++;
                if (incident.getSeverity() >= 4) {
                    highUnresolved++;
                }
                if (topOpen == null
                        || incident.getPriorityWeight() > topOpen.getPriorityWeight()
                        || (incident.getPriorityWeight() == topOpen.getPriorityWeight()
                        && incident.getId().compareTo(topOpen.getId()) < 0)) {
                    topOpen = incident;
                }
                if (isUnserviceable(incident)) {
                    unserviceable++;
                }
            } else if (Incident.STATUS_ASSIGNED.equals(incident.getStatus())) {
                assigned++;
                if (incident.getSeverity() >= 4) {
                    highUnresolved++;
                }
            } else if (Incident.STATUS_RESOLVED.equals(incident.getStatus())) {
                resolved++;
            }
        }

        StringBuilder report = new StringBuilder();
        report.append("===== SYSTEM REPORT =====\n");
        report.append("Total response units: ").append(unitCount).append("\n");
        report.append("Available units: ").append(available).append("\n");
        report.append("Busy units: ").append(busy).append("\n");
        report.append("Total distance travelled: ").append(totalDistance).append(" km\n");
        report.append("Total incidents completed by units: ").append(completed).append("\n");
        report.append("OPEN incidents: ").append(open).append("\n");
        report.append("ASSIGNED incidents: ").append(assigned).append("\n");
        report.append("RESOLVED incidents: ").append(resolved).append("\n");
        report.append("Unresolved severity 4-5: ").append(highUnresolved).append("\n");
        report.append("Most completed unit: ")
                .append(topUnit == null ? "-" : topUnit.getId() + " (" + topUnit.getCompletedIncidents() + ")")
                .append("\n");
        report.append("Highest-priority OPEN incident: ")
                .append(topOpen == null ? "-" : topOpen.getId() + " (weight " + topOpen.getPriorityWeight() + ")")
                .append("\n");
        report.append("Currently unserviceable incidents: ").append(unserviceable).append("\n");
        return report.toString();
    }

    public void displayUnserviceableIncidents() {
        boolean any = false;
        System.out.println("Currently Unserviceable Incidents");
        for (int i = 0; i < incidentCount; i++) {
            Incident incident = incidents[i];
            if (!Incident.STATUS_OPEN.equals(incident.getStatus())) {
                continue;
            }
            if (!isUnserviceable(incident)) {
                continue;
            }
            any = true;
            System.out.println(incident.getId());
            System.out.println(incident.getRequiredCapability());
            System.out.println(unserviceableReason(incident));
        }
        if (!any) {
            System.out.println("Every OPEN incident is currently serviceable.");
        }
    }

    private boolean isUnserviceable(Incident incident) {
        for (int i = 0; i < unitCount; i++) {
            ResponseUnit unit = units[i];
            if (unit.isAvailable() && unit.canHandle(incident) && unit.hasResourcesFor(incident)) {
                return false;
            }
        }
        return true;
    }

    private String unserviceableReason(Incident incident) {
        boolean sawCapable = false;
        for (int i = 0; i < unitCount; i++) {
            ResponseUnit unit = units[i];
            if (!unit.isAvailable()) {
                continue;
            }
            if (unit.canHandle(incident)) {
                sawCapable = true;
            }
        }
        if (sawCapable) {
            return "Insufficient resources";
        }
        return "No suitable available unit";
    }

    public void listUnits() {
        if (unitCount == 0) {
            System.out.println("No response units stored.");
            return;
        }
        for (int i = 0; i < unitCount; i++) {
            units[i].display();
            System.out.println();
        }
    }

    public void listIncidents() {
        if (incidentCount == 0) {
            System.out.println("No incidents stored.");
            return;
        }
        for (int i = 0; i < incidentCount; i++) {
            incidents[i].display();
            System.out.println();
        }
    }

    public void saveState(String prefix) throws InvalidOperationException {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new InvalidOperationException("Save prefix cannot be empty.");
        }
        String unitFile = prefix.trim() + "_units.csv";
        String incidentFile = prefix.trim() + "_incidents.csv";
        try {
            PrintWriter unitOut = new PrintWriter(new FileWriter(unitFile));
            for (int i = 0; i < unitCount; i++) {
                unitOut.println(units[i].toCsvRecord());
            }
            unitOut.close();
            PrintWriter incidentOut = new PrintWriter(new FileWriter(incidentFile));
            for (int i = 0; i < incidentCount; i++) {
                Incident incident = incidents[i];
                String assigned = incident.getAssignedUnitId() == null ? "" : incident.getAssignedUnitId();
                incidentOut.println(incident.getTypeName() + "," + incident.getId() + ","
                        + incident.getDescription().replace(",", ";") + ","
                        + incident.getDistanceFromBase() + "," + incident.getSeverity() + ","
                        + incident.getStatus() + "," + assigned + incident.extraCsvFields());
            }
            incidentOut.close();
        } catch (IOException e) {
            throw new InvalidOperationException("Failed to save state: " + e.getMessage());
        }
    }

    public void loadState(String prefix) throws InvalidOperationException {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new InvalidOperationException("Load prefix cannot be empty.");
        }
        clearState();
        String unitFile = prefix.trim() + "_units.csv";
        String incidentFile = prefix.trim() + "_incidents.csv";
        try {
            BufferedReader unitIn = new BufferedReader(new FileReader(unitFile));
            String line = unitIn.readLine();
            while (line != null) {
                if (!line.trim().isEmpty()) {
                    addUnit(parseUnit(line));
                }
                line = unitIn.readLine();
            }
            unitIn.close();
            BufferedReader incidentIn = new BufferedReader(new FileReader(incidentFile));
            line = incidentIn.readLine();
            while (line != null) {
                if (!line.trim().isEmpty()) {
                    addIncident(parseIncident(line));
                }
                line = incidentIn.readLine();
            }
            incidentIn.close();
        } catch (IOException e) {
            clearState();
            throw new InvalidOperationException("Failed to load state: " + e.getMessage());
        } catch (DuplicateIdException e) {
            clearState();
            throw new InvalidOperationException("Malformed saved state: " + e.getMessage());
        }
    }

    private void clearState() {
        for (int i = 0; i < unitCount; i++) {
            units[i] = null;
        }
        for (int i = 0; i < incidentCount; i++) {
            incidents[i] = null;
        }
        unitCount = 0;
        incidentCount = 0;
    }

    private Incident requireExistingIncident(String incidentId) throws InvalidOperationException {
        Incident incident = findIncident(incidentId);
        if (incident == null) {
            throw new InvalidOperationException("No incident with ID " + incidentId + ".");
        }
        return incident;
    }

    private Incident requireOpenIncident(String incidentId) throws InvalidOperationException {
        Incident incident = requireExistingIncident(incidentId);
        if (!Incident.STATUS_OPEN.equals(incident.getStatus())) {
            throw new InvalidOperationException("Incident " + incidentId + " is not OPEN.");
        }
        return incident;
    }

    private static String dashToNull(String value) {
        if (value == null || value.isEmpty() || "-".equals(value)) {
            return null;
        }
        return value;
    }

    private ResponseUnit parseUnit(String line) throws InvalidOperationException {
        String[] f = line.split(",", -1);
        if (f.length < 8) {
            throw new InvalidOperationException("Malformed unit record: " + line);
        }
        String type = f[0];
        String id = f[1];
        String name = f[2].replace(";", ",");
        double speed = Double.parseDouble(f[3]);
        double distance = Double.parseDouble(f[4]);
        boolean available = Boolean.parseBoolean(f[5]);
        String assigned = dashToNull(f[6]);
        int completed = Integer.parseInt(f[7]);
        try {
            switch (type) {
                case "AMBULANCE":
                    return new Ambulance(id, name, speed, Double.parseDouble(f[8]),
                            Double.parseDouble(f[9]), Integer.parseInt(f[10]),
                            distance, available, assigned, completed);
                case "FIRE_ENGINE":
                    return new FireEngine(id, name, speed, Double.parseDouble(f[8]),
                            Double.parseDouble(f[9]), Double.parseDouble(f[10]),
                            distance, available, assigned, completed);
                case "REPAIR_VAN":
                    return new RepairVan(id, name, speed, Double.parseDouble(f[8]),
                            Double.parseDouble(f[9]), Integer.parseInt(f[10]),
                            distance, available, assigned, completed);
                case "SEARCH_DRONE":
                    return new SearchDrone(id, name, speed, Double.parseDouble(f[8]),
                            distance, available, assigned, completed);
                case "EVAC_BUS":
                    return new EvacuationBus(id, name, speed, Double.parseDouble(f[8]),
                            Double.parseDouble(f[9]), Integer.parseInt(f[10]),
                            distance, available, assigned, completed);
                default:
                    throw new InvalidOperationException("Unknown unit type: " + type);
            }
        } catch (NumberFormatException e) {
            throw new InvalidOperationException("Malformed numeric field in unit record: " + line);
        }
    }

    private Incident parseIncident(String line) throws InvalidOperationException {
        String[] f = line.split(",", -1);
        if (f.length < 7) {
            throw new InvalidOperationException("Malformed incident record: " + line);
        }
        String type = f[0];
        String id = f[1];
        String description = f[2].replace(";", ",");
        try {
            double distance = Double.parseDouble(f[3]);
            int severity = Integer.parseInt(f[4]);
            String status = f[5];
            String assigned = dashToNull(f[6]);
            switch (type) {
                case "GENERAL":
                    return new Incident(id, description, distance, severity, status, assigned);
                case "MEDICAL":
                    return new MedicalIncident(id, description, distance, severity, status, assigned,
                            Integer.parseInt(f[7]), Integer.parseInt(f[8]));
                case "FIRE":
                    return new FireIncident(id, description, distance, severity, status, assigned,
                            Double.parseDouble(f[7]), Boolean.parseBoolean(f[8]));
                case "INFRASTRUCTURE":
                    return new InfrastructureIncident(id, description, distance, severity, status, assigned,
                            Integer.parseInt(f[7]), Boolean.parseBoolean(f[8]));
                case "SEARCH":
                    return new SearchIncident(id, description, distance, severity, status, assigned,
                            Integer.parseInt(f[7]), Double.parseDouble(f[8]));
                case "EVACUATION":
                    return new EvacuationIncident(id, description, distance, severity, status, assigned,
                            Integer.parseInt(f[7]), Double.parseDouble(f[8]));
                default:
                    throw new InvalidOperationException("Unknown incident type: " + type);
            }
        } catch (NumberFormatException e) {
            throw new InvalidOperationException("Malformed numeric field in incident record: " + line);
        }
    }
}
