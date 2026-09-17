1. Student name and roll number
Student name: Krunal Patel
Roll number: 20252777

2. Compile
javac -d bin -encoding UTF-8 src/emergency/Main.java -sourcepath src

3. Run
java -cp bin emergency.Main

Load the bundled sample with menu option 19 and prefix: sample
That reads sample_units.csv and sample_incidents.csv in the current working directory.

4. Class hierarchy
Response-unit tree (every class is concrete; none are abstract):

ResponseUnit
    GroundResponseUnit
        Ambulance           implements FuelPowered, PatientCarrier
        FireEngine          implements FuelPowered, WaterCarrier
        RepairVan           implements FuelPowered, SupplyCarrier
        EvacuationBus       implements FuelPowered, PatientCarrier   (roll-number extension)
    SearchDrone             implements BatteryPowered

Incident tree:

Incident                    (type GENERAL; any unit may handle it)
    MedicalIncident         required capability MEDICAL
    FireIncident            required capability FIRE
    InfrastructureIncident  required capability REPAIR
    SearchIncident          required capability SEARCH
    EvacuationIncident      required capability EVACUATION           (roll-number extension)

DispatchManager stores mixed subtypes in ResponseUnit[] and Incident[] with unitCount / incidentCount.

5. Overriding examples
- GroundResponseUnit.estimateArrivalTime multiplies distance/maxSpeed by trafficFactor so road delay is modelled once for every ground unit.
- SearchDrone.estimateArrivalTime uses the base hours and then multiplies by 0.85 because a drone can fly a more direct path.
- Ambulance.move consumes fuel, then calls GroundResponseUnit.move so distance is recorded and the inherited road-travel message is printed. SearchDrone.move consumes battery, then calls ResponseUnit.move.

Other overrides used in dispatch: canHandle, hasResourcesFor, calculateDispatchScore, performWork, getCapability, getPriorityWeight, getWorkload.

6. Three polymorphism sites
- dispatchBestUnit loops over ResponseUnit[] and calls isAvailable, canHandle, hasResourcesFor, calculateDispatchScore, assignIncident and move. It never uses instanceof Ambulance / FireEngine / RepairVan / SearchDrone / EvacuationBus.
- resolveIncident calls unit.performWork(incident) then unit.move(...) through a ResponseUnit reference, so boarding patients, using water, using supplies, draining drone battery, or boarding evacuees is chosen at runtime.
- generateReport walks ResponseUnit[] / Incident[] and uses getCompletedIncidents and getPriorityWeight; listUnits / listIncidents call display() the same way.

7. Interfaces
FuelPowered     Ambulance, FireEngine, RepairVan, EvacuationBus
BatteryPowered  SearchDrone
PatientCarrier  Ambulance, EvacuationBus
WaterCarrier    FireEngine
SupplyCarrier   RepairVan

DispatchManager.refuelAll / rechargeAll / refillAllWater / restockAllSupplies use instanceof FuelPowered, BatteryPowered, WaterCarrier and SupplyCarrier. That tests an independent capability. It is not a substitute for polymorphic dispatch.

8. Individual extension (last digit 6-7: Large-Scale Evacuation)
EvacuationIncident extra fields: peopleToMove, remainingSafeHours.
Capability: EVACUATION.
Priority: 5 * severity + peopleToMove / 10, plus +20 if remainingSafeHours < 2.
Workload: peopleToMove (number of seats required).

EvacuationBus: fuel capacity 200 L, 4 km/L, 40 seats, traffic-aware ground unit.
hasResourcesFor requires round-trip fuel and enough free seats for the workload.
Score: base score + 0.5 * current passengers + low-fuel penalty when fuel is below the policy threshold.
CLI / CSV types: EVAC_BUS and EVACUATION.
dispatchBestUnit did not need rewriting to support this pair.

9. CSV field order
saveState / loadState write prefix_units.csv and prefix_incidents.csv.

Unit row:
TYPE,id,name,maxSpeed,totalDistanceTravelled,available,assignedIncidentId,completedIncidents[,extras]

AMBULANCE    extras: trafficFactor,fuelLevel,currentPatients
FIRE_ENGINE  extras: trafficFactor,fuelLevel,waterLevel
REPAIR_VAN   extras: trafficFactor,fuelLevel,supplyLevel
SEARCH_DRONE extras: batteryLevel
EVAC_BUS     extras: trafficFactor,fuelLevel,currentPatients

Incident row:
TYPE,id,description,distanceFromBase,severity,status,assignedUnitId[,extras]

GENERAL         no extras
MEDICAL         extras: patientCount,criticalPatients
FIRE            extras: affectedArea,hazardousMaterial
INFRASTRUCTURE  extras: affectedUsers,criticalService
SEARCH          extras: missingPersons,searchArea
EVACUATION      extras: peopleToMove,remainingSafeHours

10. Assumptions
- Commas in names/descriptions are stored as semicolons and restored on load.
- A GENERAL incident is accepted by every unit (canHandle); fuel/battery round-trip checks still apply for units that override hasResourcesFor.
- Fuel-powered units always require enough fuel for the outbound trip plus the return before they are eligible.
- Search drones also reserve battery for the search workload between outbound and return.
- Lower dispatch score wins; equal scores pick the lexicographically smaller ID.
- Assigned units cannot be removed; ASSIGNED incidents cannot be removed.
- Default array limits are 50 units and 100 incidents.
- DispatchPolicy defaults are lowFuel 0.25/12, lowBattery 0.30/15, lowWater 0.40/20, lowSupply 0.25/10.
- Sample files live in the A1 folder. Use prefix sample from that folder.
- Only the evacuation extension is included (roll number ends in 7). Flood, security, power and hazmat types are not supported.
- loadState parses both CSV files into temporary arrays and replaces live state only after both files succeed. A missing or malformed file leaves the current in-memory fleet unchanged.
- Negative travel distance throws InvalidOperationException and does not change fuel, battery, or total distance.

11. The fifteen tests and what was observed
Sample: two ambulances (U01 traffic 1.2, U02 traffic 1.0 and 20 L fuel), U04 low water, U07 low battery, U03/U08 identical fire engines, U11 evacuation bus, I05 with 6 patients, I11/I12 evacuations.

1. Heterogeneous array. Option 19 sample, option 7. Ambulance, FireEngine, RepairVan, SearchDrone and EvacuationBus print different extra fields. Later moves print inherited messages (ground units: travelling by road with traffic factor; drone: Unit ... travelling ... km).

2. Duplicate ID. Adding another unit U01 prints Error: Duplicate unit ID: U01. The menu keeps running.

3. Full array. Default size is 50. DispatchManager(1, 1, new DispatchPolicy()) throws Response unit array is full after the first unit.

4. Removal shift. Add U01, U02, U03, remove U02. Remaining order is U01 then U03. findUnit("U02") is empty.

5. Search. Find U01 shows Central Ambulance. Find ZZZ prints No response unit with that ID.

6. Dispatch selection. I01: U01 score about -18.82, U02 score -19.5. Best candidate U02 because of lower traffic (fuel 20 L is not strictly below 25 percent of 80 L, so no penalty).

7. Tie. I06: U03 and U08 both eligible with score about -9.67. Best candidate U03 (smaller ID). U04 rejected: Insufficient water.

8. Insufficient travel resource. I07 needs 168 battery units. U06 and U07 both Insufficient battery. Dispatch: No suitable unit for incident I07.

9. Insufficient operational resource. I05: Insufficient patient capacity (6 vs 4 seats). I02 vs U04: Insufficient water. Fresh I12: Insufficient passenger capacity (50 vs 40 seats).

10. Busy unit. After dispatching I02 to U03, preview of I06 shows U03 Currently assigned. Best candidate becomes U08.

11. Unserviceable. Option 17 lists I05 MEDICAL Insufficient resources, I07 SEARCH Insufficient resources, I12 EVACUATION Insufficient resources.

12. Resolve. Dispatch I01 to U02, option 11. Distance 25.0 km (out and back). Fuel 20.0 L to about 17.92 L. Unit available, completedIncidents = 1, I01 RESOLVED, patients onboard 0.

13. Redispatch. After resolve, U02 is available again. A new small medical job while U01 is free may select U01, because U02 is now under the 25 percent fuel threshold (+12 penalty). If U01 is already busy, the next medical incident goes to U02. Refuel restores U02's score advantage from traffic 1.0.

14. Persistence. Option 18 prefix demo, exit, run again, option 19 prefix demo. Lists restore type, fuel/battery, status, assignments and completed counts.

15. Extension. I11 (25 people, 1.5 safe hours) dispatches U11. Score about -23.3 including the <2 hour urgency bonus. I12 (50 people) fails for seats when the bus is free, or Currently assigned after I11 is dispatched. dispatchBestUnit was not changed for this pair.
