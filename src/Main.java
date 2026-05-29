//
// Title:       Main — manual smoke-test driver for all three SmartPark strategies
// Author:      [Elçin Karagül / Kayra Arı]
// ID:          [10885319050 / 10001507]
// Section:     [04]
// Assignment:  5
// Description: A simple driver that exercises ChainingParkTable,
//              LinearProbingParkTable, and QuadraticProbingParkTable
//              with a fixed set of five vehicles, running under both
//              HASH_FULL and HASH_CITY modes.  This is NOT the graded
//              tester (see SmartParkTester.java); it is intended for
//              quick manual verification during development.
//
public class Main {
    // main() — entry point for the manual smoke test.
    //
    // Summary:     Creates five Vehicle objects, then for each hash mode
    //              (HASH_FULL and HASH_CITY) iterates over the three table
    //              implementations and exercises park, locate, leave, and
    //              stats operations on each.  Results are printed to stdout
    //              so the developer can visually inspect slot assignments
    //              and statistics.
    // Precondition:  No command-line arguments are required.
    // Postcondition: Each of the six table/mode combinations is exercised
    //                and its output printed; no files are written.
    public static void main(String[] args) {
// --- Create a representative set of vehicles for testing ---
        Vehicle v1 = new Vehicle("06ABC123", "Ali Yilmaz",   "CAR",        1000L);
        Vehicle v2 = new Vehicle("06DEF456", "Ayse Kaya",    "SUV",        2000L);
        Vehicle v3 = new Vehicle("34XYZ789", "Mehmet Demir", "TRUCK",      3000L);
        Vehicle v4 = new Vehicle("06GHI321", "Fatma Celik",  "CAR",        4000L);
        Vehicle v5 = new Vehicle("35JKL654", "Hasan Ozturk", "MOTORCYCLE", 5000L);

        // Test under both hash modes so we can observe the difference in
        // slot distribution between HASH_FULL and HASH_CITY
        for (boolean full : new boolean[]{true, false}) {
            // Switch the static flag that controls which hashCode() variant is used
            Vehicle.setUseFullHash(full);
            System.out.println("\n====== Hash Mode: " + (full ? "HASH_FULL" : "HASH_CITY") + " ======");
            // Instantiate all three strategies with a small initial capacity (4)
            // so that resize events are triggered during the test

            SmartParkTable[] tables = {
                    new ChainingParkTable(4),
                    new LinearProbingParkTable(4),
                    new QuadraticProbingParkTable(4)
            };

            for (SmartParkTable t : tables) {
                System.out.println("\n--- " + t.getClass().getSimpleName() + " ---");

                // --- PARK: insert all five vehicles and record slot assignments ---
                System.out.println("park(06ABC123) -> slot: " + t.park(v1));
                System.out.println("park(06DEF456) -> slot: " + t.park(v2));
                System.out.println("park(34XYZ789) -> slot: " + t.park(v3));
                System.out.println("park(06GHI321) -> slot: " + t.park(v4));
                System.out.println("park(35JKL654) -> slot: " + t.park(v5));

                // --- LOCATE: search for a vehicle that is present ---
                System.out.println("locate(34XYZ789) -> slot: " + t.locate(v3));

                // --- LEAVE + LOCATE: remove a vehicle then confirm it is gone ---
                System.out.println("leave(06ABC123)  -> " + t.leave("06ABC123"));
                System.out.println("locate(06ABC123) -> " + t.locate(v1) + " (expected -1)");

                // --- STATS: print summary of the current table state ---
                t.stats();
            }
        }
    }
}