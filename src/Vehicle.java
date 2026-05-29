//
// Title:       Vehicle — data object representing a parked vehicle
// Author:      [Elçin Karagül / Kayra Arı]
// ID:          [10885319050 / 10001507]
// Section:     [04]
// Assignment:  5
// Description: Represents a vehicle entering a SmartPark facility.
//              Implements two hash functions (HASH_FULL and HASH_CITY)
//              that are selected at runtime via a static flag.
//
public class Vehicle implements Comparable<Vehicle>
{
    private String licensePlate; // Format: "06ABC123" (2-digit city code + letters + digits)
    private String ownerName; // Owner's full name
    private String vehicleType; // "CAR", "SUV", "MOTORCYCLE", "TRUCK"
    private long entryTimestamp; // Unix timestamp at gate entry
    // Constructor, getters, equals(), compareTo() are required.

    // ---------------------------------------------------------------
    // Static flag — controls which hash function hashCode() delegates to.
    // true  → HASH_FULL  (good quality, uses entire plate string)
    // false → HASH_CITY  (poor quality, uses only first 2 characters)
    // ---------------------------------------------------------------
    private static boolean useFullHash = true;
    // setUseFullHash() — switches the active hash mode for ALL vehicles.
    //
    // Summary:     Sets the static flag that determines which hash function
    //              hashCode() will call. Must be called before inserting
    //              vehicles into a table so that all probes use the same mode.
    // Precondition:  val is a boolean (true = FULL, false = CITY).
    // Postcondition: Every subsequent call to hashCode() on any Vehicle
    //                instance will use the chosen function.
    public static void setUseFullHash(boolean val) {
        useFullHash = val;
    }

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    // Vehicle() — creates a vehicle with all required fields.
    //
    // Summary:     Initialises a Vehicle object. The license plate is
    //              converted to upper-case to ensure case-insensitive
    //              equality and consistent hashing.
    // Precondition:  licensePlate is non-null and follows the Turkish
    //                plate format (e.g. "06ABC123"). ownerName and
    //                vehicleType are non-null strings. entryTimestamp
    //                is a valid Unix timestamp (>= 0).
    // Postcondition: A fully initialised Vehicle is returned with
    //                licensePlate stored in upper-case.
    public Vehicle(String licensePlate,String ownerName,String vehicleType, long entryTimestamp){
        this.licensePlate=licensePlate.toUpperCase();
        this.ownerName=ownerName;
        this.vehicleType=vehicleType;
        this.entryTimestamp=entryTimestamp;


    }
    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------

    // getLicensePlate() — returns the plate string (always upper-case).
    //
    // Summary:     Accessor for the license plate field.
    // Precondition:  None.
    // Postcondition: Returns the upper-case license plate string.
    public String getLicensePlate() {
        return licensePlate;
    }
    // getOwnerName() — returns the full name of the vehicle owner.
    //
    // Summary:     Accessor for the ownerName field.
    // Precondition:  None.
    // Postcondition: Returns the owner's full name as stored.
    public String getOwnerName(){
        return ownerName;
    }
    // getVehicleType() — returns the vehicle category string.
    //
    // Summary:     Accessor for the vehicleType field.
    // Precondition:  None.
    // Postcondition: Returns one of "CAR", "SUV", "MOTORCYCLE", "TRUCK".
    public String getVehicleType(){
        return vehicleType;
    }
    // getEntryTimestamp() — returns the Unix entry timestamp.
    //
    // Summary:     Accessor for the entryTimestamp field.
    // Precondition:  None.
    // Postcondition: Returns the long Unix timestamp recorded at gate entry.
    public long getEntryTimestamp(){
        return entryTimestamp;
    }
    // ---------------------------------------------------------------
    // equals() and compareTo()
    // ---------------------------------------------------------------

    // equals() — compares two vehicles by license plate (case-insensitive).
    //
    // Summary:     Two Vehicle objects are considered equal when their
    //              license plates are identical ignoring case. All other
    //              fields (owner, type, timestamp) are irrelevant for
    //              equality, which allows locate/leave to search only
    //              by plate.
    // Precondition:  obj is a Vehicle instance (cast is performed directly
    //                per the assignment specification).
    // Postcondition: Returns true iff the plates match case-insensitively.

    @Override
    public boolean equals(Object obj) {
        Vehicle otherV = (Vehicle) obj;
        return licensePlate.equalsIgnoreCase(otherV.licensePlate);
    }

    // compareTo() — lexicographic order on license plates (case-insensitive).
    //
    // Summary:     Allows Vehicle objects to be sorted or used in ordered
    //              data structures. Ordering is based solely on the plate
    //              string, case-insensitively.
    // Precondition:  otherV is a non-null Vehicle.
    // Postcondition: Returns negative, zero, or positive int consistent
    //                with String.compareToIgnoreCase().
    public int compareTo(Vehicle otherV) {
        return this.licensePlate.compareToIgnoreCase(otherV.licensePlate);
    }

    // ---------------------------------------------------------------
    // Hash functions
    // ---------------------------------------------------------------

    // HASH_FULL() — Horner's method over the entire license plate string.
    //
    // Summary:     Computes a polynomial rolling hash using base 31,
    //              scanning every character of the plate. This produces
    //              a high-quality, well-distributed hash value because
    //              every character contributes to the result.
    //              Formula (slide 5 / slide 10):
    //                h = s[0]*31^(L-1) + s[1]*31^(L-2) + ... + s[L-1]
    //              evaluated incrementally as:
    //                hash = 31 * hash + charAt(i)   for i in [0, L)
    // Precondition:  licensePlate is non-null and has length >= 1.
    // Postcondition: Returns an int hash code (may be negative due to
    //                overflow — callers must apply & 0x7fffffff before
    //                taking modulo).
    public int HASH_FULL(){
        int hash=0;
        for(int i=0;i<licensePlate.length();i++){
            hash = 31 * hash + licensePlate.charAt(i);
        }
        return hash;

    }
    // HASH_CITY() — Horner's method over only the first 2 characters (city code).
    //
    // Summary:     Computes the same polynomial rolling hash as HASH_FULL
    //              but stops after the first two characters, which encode
    //              the Turkish city code (01–81). Because only ~81 distinct
    //              two-character prefixes exist, this function maps all
    //              vehicles from the same city to the same bucket, producing
    //              severe clustering. Included deliberately as the "bad"
    //              hash function for Experiment 1.
    // Precondition:  licensePlate is non-null and has length >= 2.
    // Postcondition: Returns an int hash code derived from at most 2 chars.
    //                Only ~81 distinct values are ever produced regardless
    //                of table size M.
    public int HASH_CITY(){
        int hash=0;
        for(int i=0;i<2;i++){
            hash = 31 * hash + licensePlate.charAt(i);

        }
        return hash;
    }
    // hashCode() — delegates to HASH_FULL or HASH_CITY based on static flag.
    //
    // Summary:     Overrides Object.hashCode() so that the hash table
    //              classes can call v.hashCode() uniformly without knowing
    //              which mode is active. The active mode is controlled by
    //              the static flag useFullHash set via setUseFullHash().
    // Precondition:  useFullHash has been set to the desired mode before
    //                any vehicles are inserted into a table.
    // Postcondition: Returns HASH_FULL() if useFullHash is true,
    //                HASH_CITY() otherwise.
    @Override
    public int hashCode() {
        if (useFullHash) return HASH_FULL();
        else             return HASH_CITY();
    }


}