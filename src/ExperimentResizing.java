public class ExperimentResizing {
    public static void main(String[] args) {

        Vehicle.setUseFullHash(true);

        SmartParkTable[] tables = {
                new ChainingParkTable(4),
                new LinearProbingParkTable(4),
                new QuadraticProbingParkTable(4)
        };

        for (SmartParkTable table : tables) {

            System.out.println("\n====================================");
            System.out.println(table.getClass().getSimpleName());
            System.out.println("====================================");

            // INSERTIONS
            for (int i = 0; i < 50; i++) {

                String plate =
                        String.format("35AAA%03d", i);

                Vehicle v = new Vehicle(
                        plate,
                        "Owner" + i,
                        "TRUCK",
                        i
                );

                table.park(v);

                // Print every 10 insertions
                if ((i + 1) % 10 == 0) {

                    System.out.println(
                            "\nAfter inserting " +
                                    (i + 1) + " vehicles:"
                    );

                    table.stats();
                }
            }

            // DELETIONS
            for (int i = 0; i < 40; i++) {

                String plate =
                        String.format("35AAA%03d", i);

                table.leave(plate);

                if ((i + 1) % 10 == 0) {

                    System.out.println(
                            "\nAfter deleting " +
                                    (i + 1) + " vehicles:"
                    );

                    table.stats();
                }
            }
        }
    }
}
