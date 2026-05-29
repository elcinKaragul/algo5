//
// Title:       SmartParkTable — common interface for all parking hash tables
// Author:      [Elçin Karagül / Kayra Arı]
// ID:          [10885319050 / 10001507]
// Section:     [04]
// Assignment:  5
// Description: Defines the contract that every SmartPark hash table
//              implementation must fulfil.  The three concrete classes
//              (ChainingParkTable, LinearProbingParkTable,
//              QuadraticProbingParkTable) each implement this interface,
//              allowing the tester and experiment drivers to work with
//              any strategy through a single reference type.
//
public interface    SmartParkTable {
    // park() — inserts a vehicle into the hash table.
    //
    // Summary:     Computes the home slot from v.hashCode(), resolves
    //              any collision using the implementing strategy, and
    //              stores the vehicle. Triggers a resize if the load
    //              factor crosses the high threshold after insertion.
    // Precondition:  v is a non-null Vehicle with a valid license plate.
    // Postcondition: The vehicle is stored in the table. Returns the
    //                slot index where it was placed, or -1 if the table
    //                is completely full (only possible for open-addressing
    //                tables that reach 100 % occupancy before resizing).
    int park(Vehicle v);
    // locate() — searches for a vehicle by its license plate.
    //
    // Summary:     Computes the home slot from v.hashCode() and probes
    //              according to the implementing strategy until the
    //              vehicle is found or it is certain the plate is absent.
    // Precondition:  v is a non-null Vehicle whose licensePlate field
    //                holds the plate to search for.
    // Postcondition: Returns the slot index of the matching vehicle,
    //                or -1 if no vehicle with that plate exists.
    int locate(Vehicle v);
    // leave() — removes a vehicle identified by its license plate.
    //
    // Summary:     Locates the vehicle with the given plate and removes
    //              it from the table. For open-addressing tables a
    //              tombstone (DELETED sentinel) is placed so that probe
    //              chains remain intact. Triggers a resize if the load
    //              factor drops below the low threshold after removal.
    // Precondition:  licensePlate is a non-null, non-empty string.
    // Postcondition: Returns true and decrements N if the vehicle was
    //                found and removed; returns false otherwise.
    boolean leave( String licensePlate);
    // stats() — prints one-line statistics to standard output.
    //
    // Summary:     Prints the current state of the table in the format:
    //                N=<n> M=<m> ALPHA=<alpha_3dp> COLLISIONS=<c> MAX_CLUSTER=<mc>
    //              where ALPHA is the load factor to three decimal places,
    //              COLLISIONS is the cumulative count since INIT, and
    //              MAX_CLUSTER is the longest chain (chaining) or the
    //              longest contiguous occupied run (open addressing).
    // Precondition:  None.
    // Postcondition: Exactly one line is printed; no state is modified.
    void stats();
    // maxCluster() — returns the length of the largest cluster or chain.
    //
    // Summary:     For ChainingParkTable: scans all buckets and returns
    //              the length of the longest linked chain.
    //              For open-addressing tables: scans the flat array and
    //              returns the length of the longest contiguous run of
    //              occupied (non-null, non-DELETED) slots, wrapping
    //              around the end of the array.
    // Precondition:  None.
    // Postcondition: Returns a non-negative integer. Returns 0 if the
    //                table is empty.
    int     maxCluster();
    // getN() — returns the number of vehicles currently in the table.
    //
    // Summary:     Accessor for the N (element count) field.
    // Precondition:  None.
    // Postcondition: Returns N >= 0.
    int     getN();
    // getM() — returns the current table capacity.
    //
    // Summary:     Accessor for the M (bucket / slot count) field.
    // Precondition:  None.
    // Postcondition: Returns M >= 1.
    int     getM();
    // getM() — returns the current table capacity.
    //
    // Summary:     Accessor for the M (bucket / slot count) field.
    // Precondition:  None.
    // Postcondition: Returns M >= 1.
    long    getCollisions();
}
