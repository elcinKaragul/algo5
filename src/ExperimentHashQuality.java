public class ExperimentHashQuality {
    private static final int VEHICLE_COUNT =1000;
    public static void main(String[] args) {

        runExperiment(true);   // HASH_FULL
        runExperiment(false);  // HASH_CITY
    }

    private static void runExperiment(boolean fullHash) {

        Vehicle.setUseFullHash(fullHash);

        System.out.println("\n=================================================");
        System.out.println("HASH MODE: " +
                (fullHash ? "HASH_FULL" : "HASH_CITY"));
        System.out.println("=================================================");

        SmartParkTable[] tables = {
                new ChainingParkTable(16),
                new LinearProbingParkTable(16),
                new QuadraticProbingParkTable(16)
        };

        for (SmartParkTable table : tables) {

            System.out.println("\n--- " +
                    table.getClass().getSimpleName() + " ---");

            // Insert many vehicles
            for (int i = 0; i < VEHICLE_COUNT; i++) {

                String plate =
                        String.format("06ABC%04d", i);

                Vehicle v = new Vehicle(
                        plate,
                        "Owner" + i,
                        "CAR",
                        i
                );

                table.park(v);
            }

            table.stats();
        }
    }
}
