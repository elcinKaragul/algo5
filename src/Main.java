public class Main {
    public static void main(String[] args) {

        Vehicle v1 = new Vehicle("06ABC123", "Ali Yilmaz",   "CAR",        1000L);
        Vehicle v2 = new Vehicle("06DEF456", "Ayse Kaya",    "SUV",        2000L);
        Vehicle v3 = new Vehicle("34XYZ789", "Mehmet Demir", "TRUCK",      3000L);
        Vehicle v4 = new Vehicle("06GHI321", "Fatma Celik",  "CAR",        4000L);
        Vehicle v5 = new Vehicle("35JKL654", "Hasan Ozturk", "MOTORCYCLE", 5000L);

        for (boolean full : new boolean[]{true, false}) {
            Vehicle.setUseFullHash(full);
            System.out.println("\n====== Hash Mode: " + (full ? "HASH_FULL" : "HASH_CITY") + " ======");

            SmartParkTable[] tables = {
                    new ChainingParkTable(4),
                    new LinearProbingParkTable(4),
                    new QuadraticProbingParkTable(4)
            };

            for (SmartParkTable t : tables) {
                System.out.println("\n--- " + t.getClass().getSimpleName() + " ---");

                System.out.println("park(06ABC123) -> slot: " + t.park(v1));
                System.out.println("park(06DEF456) -> slot: " + t.park(v2));
                System.out.println("park(34XYZ789) -> slot: " + t.park(v3));
                System.out.println("park(06GHI321) -> slot: " + t.park(v4));
                System.out.println("park(35JKL654) -> slot: " + t.park(v5));

                System.out.println("locate(34XYZ789) -> slot: " + t.locate(v3));

                System.out.println("leave(06ABC123)  -> " + t.leave("06ABC123"));
                System.out.println("locate(06ABC123) -> " + t.locate(v1) + " (expected -1)");

                t.stats();
            }
        }
    }
}