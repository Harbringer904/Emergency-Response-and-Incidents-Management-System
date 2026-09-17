package emergency.capabilities;

import emergency.exceptions.InvalidOperationException;

public interface PatientCarrier {
    void boardPatients(int number) throws InvalidOperationException;

    void releasePatients(int number) throws InvalidOperationException;

    int getPatientCapacity();

    int getCurrentPatients();
}
