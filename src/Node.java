//
// Title:       Node — singly-linked list node for separate chaining
// Author:      [Elçin Karagül / Kayra Arı]
// ID:          [10885319050 / 10001507]
// Section:     [04]
// Assignment:  5
// Description: A minimal singly-linked list node used exclusively by
//              ChainingParkTable to build per-bucket chains.
//              Implemented from scratch as required — java.util.LinkedList
//              is not used anywhere in the project.
//
public class Node {
    // ---------------------------------------------------------------
    // Fields — package-private so ChainingParkTable can access directly
    // ---------------------------------------------------------------
    Vehicle vehicle; // The vehicle stored at this node
    Node    next; //reference to the next node in chain (null if tail)

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    // Node(Vehicle) — creates a standalone node with no successor.
    //
    // Summary:     Single-argument constructor used when inserting a
    //              vehicle into an empty bucket. next is set to null,
    //              making this node both the head and the tail of its
    //              chain.
    // Precondition:  vehicle is a non-null Vehicle object.
    // Postcondition: A Node is created with the given vehicle and
    //                next == null.
    public Node(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.next    = null;
    }

    // Node(Vehicle, Node) — prepend constructor for front-insertion.
    //
    // Summary:     Two-argument constructor used to prepend a new node
    //              at the head of an existing chain. Following the
    //              pattern from the lecture slides (slide 21):
    //                  table[i] = new Node(vehicle, table[i])
    //              The current head of the bucket is passed as 'next',
    //              making the new node the new head in O(1) time.
    // Precondition:  vehicle is a non-null Vehicle object.
    //                next may be null (empty chain) or an existing Node.
    // Postcondition: A Node is created where this.vehicle = vehicle
    //                and this.next = next (the previous head).

    public Node(Vehicle vehicle, Node next) {
        this.vehicle = vehicle;
        this.next    = next;
    }
}

