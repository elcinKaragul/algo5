public class Node {
    Vehicle vehicle;
    Node    next;

    //  constructor
    public Node(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.next    = null;
    }

    // Prepend constructor we need for chaining insert at front

    public Node(Vehicle vehicle, Node next) {
        this.vehicle = vehicle;
        this.next    = next;
    }
}

