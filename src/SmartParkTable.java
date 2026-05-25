public interface SmartParkTable {
    int park(Vehicle v);
    int locate(Vehicle v);
    boolean leave(String licensePlate);
    void stats();
}
