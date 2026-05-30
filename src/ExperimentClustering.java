public class ExperimentClustering {
    private static final int VEHICLE_COUNT = 500;

    public static void main(String[] args) {

        Vehicle.setUseFullHash(true);

        SmartParkTable linear =
                new LinearProbingParkTable(16);

        SmartParkTable quadratic =
                new QuadraticProbingParkTable(16);

        for (int i = 0; i < VEHICLE_COUNT; i++) {

            String plate =
                    String.format("34QWE%04d", i);

            Vehicle v = new Vehicle(
                    plate,
                    "Owner" + i,
                    "CAR",
                    i
            );

            linear.park(v);
            quadratic.park(v);
        }

        System.out.println("\nLINEAR PROBING");
        linear.stats();

        System.out.println("\nQUADRATIC PROBING");
        quadratic.stats();
    }
}
