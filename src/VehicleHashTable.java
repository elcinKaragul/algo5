public class VehicleHashTable implements SmartParkTable {

    private int size;
    private int capacity;

    public VehicleHashTable(int size, int capacity){
        this.size=size;
        this.capacity=capacity;
    }

    private static class Node {
        Vehicle vehicle;
        Node next;
        Node(Vehicle vehicle){
            this.vehicle=vehicle;
            this.next=null;
        }
    }
    @Override
    public int park(Vehicle V){
    }
    @Override
    public int locate(Vehicle V){
    }
    @Override
    public boolean leave(String licensePlate){
    }

    @Override
    public void stats(){
    }
}
