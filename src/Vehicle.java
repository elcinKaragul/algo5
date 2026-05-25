public class Vehicle implements Comparable<Vehicle>
{
    private String licensePlate; // Format: "06ABC123" (2-digit city code + letters + digits)
    private String ownerName; // Owner's full name
    private String vehicleType; // "CAR", "SUV", "MOTORCYCLE", "TRUCK"
    private long entryTimestamp; // Unix timestamp at gate entry
    // Constructor, getters, equals(), compareTo() are required.
    //Constructor
    public Vehicle(String licensePlate,String ownerName,String vehicleType, long entryTimestamp){
        this.licensePlate=licensePlate.toUpperCase();
        this.ownerName=ownerName;
        this.vehicleType=vehicleType;
        this.entryTimestamp=entryTimestamp;


    }
    //getters
    public String getLicensePlate() {
        return licensePlate;
    }
    public String getOwnerName(){
        return ownerName;
    }
    public String getVehicleType(){
        return vehicleType;
    }
    public long getEntryTimestamp(){
        return entryTimestamp;
    }
    //equals()

    @Override
    public boolean equals(Object obj) {
        Vehicle otherV = (Vehicle) obj;
        return licensePlate.equalsIgnoreCase(otherV.licensePlate);
    }

    //compareTo()
    public int compareTo(Vehicle otherV) {
        return this.licensePlate.compareToIgnoreCase(otherV.licensePlate);
    }

    // hashCode() MUST be implemented in two versions (see below).
    // h = s[0]*31^(L-1) + s[1]*31^(L-2) + ... + s[L-1]
    public int HASH_FULL(){
        int hash=0;
        for(int i=0;i<licensePlate.length();i++){
            hash = 31 * hash + licensePlate.charAt(i);
        }
        return hash;

    }
    public int HASH_CITY(){
        int hash=0;
        for(int i=0;i<2;i++){
            hash = 31 * hash + licensePlate.charAt(i);

        }
        return hash;
    }


}