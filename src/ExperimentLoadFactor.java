 // ------------------------------------------------------------
// ExperimentLoadFactor.java
// Observes behavior as load factor increases
// ------------------------------------------------------------

public class ExperimentLoadFactor {

    public static void main(String[] args) {

        Vehicle.setUseFullHash(true);

        SmartParkTable[] tables = {
                new ChainingParkTable(16),
                new LinearProbingParkTable(16),
                new QuadraticProbingParkTable(16)
        };

        for (SmartParkTable table : tables) {

            System.out.println("\n====================================");
            System.out.println(table.getClass().getSimpleName());
            System.out.println("====================================");

            for (int size = 100;
                 size <= 1000;
                 size += 100) {

                // Insert additional vehicles
                for (int i = table.getN();
                     i < size;
                     i++) {

                    String plate =
                            String.format("34XYZ%04d", i);

                    Vehicle v = new Vehicle(
                            plate,
                            "Owner" + i,
                            "SUV",
                            i
                    );

                    table.park(v);
                }

                System.out.println("\nAfter " +
                        size + " insertions:");

                table.stats();
            }
        }
    }
}

