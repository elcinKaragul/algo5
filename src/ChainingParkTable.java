//
// Title:       ChainingParkTable — hash table with separate chaining
// Author:      [Elçin Karagül / Kayra Arı]
// ID:          [10885319050 / 10001507]
// Section:     [04]
// Assignment:  5
// Description: Implements the SmartParkTable interface using separate
//              chaining collision resolution.  Each bucket holds a
//              hand-written singly-linked list of Node objects.
//              The table doubles when N/M >= 8 and halves when N/M <= 2
//              (and M is above its initial size).  All keys are rehashed
//              on every resize.
//
public class ChainingParkTable implements SmartParkTable {

    // ---------------------------------------------------------------
    // Resize thresholds (separate chaining uses higher thresholds than
    // open addressing because chains can grow arbitrarily).
    //
    // LOAD_HIGH: double the table when average chain length hits 8.
    // LOAD_LOW:  halve the table when average chain length drops to 2.
    // ---------------------------------------------------------------
    private static final int LOAD_HIGH = 8;
    private static final int LOAD_LOW  = 2;

    private int    M;           // number of buckets (table capacity)
    private int    N;           // number of vehicles currently stored
    private Node[] table;       //Array of chain heads, table[i] is the head of the linked list at bucket i
    private final int INITIAL_M; // minimum capacity do not shrink below this

    // Statistics
    private long totalProbes     = 0;// Every slot/node examined counts as one probe
    private long totalCollisions = 0;// Incremented when home bucket is already occupied
    private int  resizeCount     = 0;// Number of resize operations performed

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    // ChainingParkTable() — default constructor with capacity 16.
    //
    // Summary:     Creates an empty chaining table with a small default
    //              initial capacity. Suitable for general use when the
    //              expected load is not known in advance.
    // Precondition:  None.
    // Postcondition: An empty table with M=16 buckets is ready.
    public ChainingParkTable() {
        this(16);
    }

    // ChainingParkTable(int) — constructor with caller-specified capacity.
    //
    // Summary:     Creates an empty chaining table with the given initial
    //              capacity. Used by the command parser (INIT ... M ...)
    //              and by experiment drivers that need precise control
    //              over starting size.
    // Precondition:  initialM >= 1.
    // Postcondition: An empty table with M = initialM buckets is ready.
    //                INITIAL_M is fixed to initialM for the lifetime of
    //                this table instance.
    public ChainingParkTable(int initialM) {
        this.M         = initialM;
        this.INITIAL_M = initialM;
        this.table     = new Node[M];
        this.N         = 0;
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    // hash() — maps a vehicle to a bucket index in [0, M).
    //
    // Summary:     Strips the sign bit from v.hashCode() with & 0x7fffffff
    //              (guaranteeing a non-negative value) then takes modulo M
    //              to obtain a valid bucket index.  The mask is needed
    //              because Java's int can be negative after Horner overflow.
    // Precondition:  v is a non-null Vehicle; M >= 1.
    // Postcondition: Returns an int in [0, M).
    private int hash(Vehicle v) {
        return (v.hashCode() & 0x7fffffff) % M;
    }

    // ---------------------------------------------------------------
    // SmartParkTable interface implementations
    // ---------------------------------------------------------------

    // park() — inserts or updates a vehicle using front-insertion chaining.
    //
    // Summary:     Computes the home bucket with hash(v).  Walks the chain
    //              at that bucket to check whether the plate already exists
    //              (update-in-place if so).  If the plate is new, prepends
    //              a fresh Node at the front of the chain in O(1) — the
    //              same pattern as slide 21:
    //                  table[i] = new Node(vehicle, table[i])
    //              A collision is counted when the home bucket is non-empty
    //              and the first node does NOT equal v.  After insertion,
    //              if N/M >= LOAD_HIGH the table doubles.
    // Precondition:  v is a non-null Vehicle with a valid license plate.
    // Postcondition: v is stored in the table (or updated if already
    //                present). Returns the bucket index where v lives.
    //                totalCollisions is updated if the home bucket was
    //                already occupied by a different vehicle.
    @Override
    public int park(Vehicle v) {
        int idx = hash(v);
        totalProbes++;// Probing the home bucket counts as one probe

        // If plate already exists, update and return
        for (Node x = table[idx]; x != null; x = x.next) {
            totalProbes++;
            if (x.vehicle.equals(v)) {
                x.vehicle = v;
                return idx;
            }
        }

        // Count collision if bucket is not empty
        if (table[idx] != null) totalCollisions++;

        // Prepend to chain — slide 21: st[i] = new Node(key, val, st[i])
        table[idx] = new Node(v, table[idx]);
        N++;

        if ((double) N / M >= LOAD_HIGH) resize(M * 2);

        return idx;
    }

    // locate() — searches the bucket for a vehicle with the given plate.
    //
    // Summary:     Hashes v to find its home bucket and walks only that
    //              chain, comparing plates case-insensitively via equals().
    //              Because chaining confines each key to a single bucket,
    //              no cross-bucket probing is needed.
    // Precondition:  v is a non-null Vehicle whose licensePlate field
    //                holds the plate to search for.
    // Postcondition: Returns the bucket index of the matching vehicle,
    //                or -1 if the plate is not found in the table.
    @Override
    public int locate(Vehicle v) {
        int idx = hash(v);
        totalProbes++;
        for (Node x = table[idx]; x != null; x = x.next) {
            totalProbes++;
            if (x.vehicle.equals(v)) return idx;
        }
        return -1;
    }

    // leave() — removes the vehicle with the given plate from its chain.
    //
    // Summary:     Hashes the plate to find the home bucket, then walks
    //              the chain maintaining a 'prev' pointer for standard
    //              singly-linked-list node removal:
    //                - Removing the head: table[idx] = cur.next
    //                - Removing mid/tail: prev.next  = cur.next
    //              After removal N is decremented.  If N/M <= LOAD_LOW
    //              and the table is above its minimum size, it halves.
    // Precondition:  licensePlate is a non-null, non-empty string.
    // Postcondition: Returns true and removes the node if found.
    //                Returns false without modifying the table if the
    //                plate does not exist.
    @Override
    public boolean leave(String licensePlate) {
        Vehicle probe = new Vehicle(licensePlate, "", "", 0);
        int  idx  = hash(probe);
        Node cur  = table[idx];
        Node prev = null;
        totalProbes++;

        while (cur != null) {
            totalProbes++;
            if (cur.vehicle.getLicensePlate().equalsIgnoreCase(licensePlate)) {
                if (prev == null) table[idx] = cur.next; // removing head node
                else              prev.next  = cur.next; // removing mid/tail node
                N--;
                if (M > INITIAL_M && (double) N / M <= LOAD_LOW)
                    resize(M / 2);
                return true;
            }
            prev = cur;
            cur  = cur.next;
        }
        return false; // not found
    }

    // stats() — prints current table statistics on one line.
    //
    // Summary:     Outputs exactly: N=<n> M=<m> ALPHA=<alpha> COLLISIONS=<c>
    //              MAX_CLUSTER=<mc> where ALPHA is rounded to 3 decimal
    //              places and MAX_CLUSTER is the longest chain length.
    // Precondition:  None.
    // Postcondition: One line printed to stdout; no state is changed.
    @Override
    public void stats() {
        System.out.println("N=" + N + " M=" + M +
                String.format(" ALPHA=%.3f", (double) N / M) +
                " COLLISIONS=" + totalCollisions +
                " MAX_CLUSTER=" + maxCluster());
    }

    @Override public int  getN()          { return N; }
    @Override public int  getM()          { return M; }
    @Override public long getCollisions() { return totalCollisions; }

    // maxCluster() — returns the length of the longest chain in the table.
    //
    // Summary:     Iterates over all M buckets, counts the number of nodes
    //              in each chain, and returns the maximum. An empty bucket
    //              (null head) has chain length 0.
    // Precondition:  None.
    // Postcondition: Returns a non-negative int. Returns 0 iff N == 0.
    @Override
    public int maxCluster() {
        int max = 0;
        for (int i = 0; i < M; i++) {
            int len = 0;
            for (Node x = table[i]; x != null; x = x.next)
                len++;
            if (len > max) max = len;
        }
        return max;
    }
    // ---------------------------------------------------------------
    // Private resize helper
    // ---------------------------------------------------------------

    // resize() — rehashes all entries into a new table of size newM.
    //
    // Summary:     Allocates a fresh Node array of size newM, recomputes
    //              each vehicle's bucket index under the new M, and
    //              prepends it to the new chain using front-insertion.
    //              N is recounted from scratch during rehashing so it
    //              stays consistent.  The minimum size is clamped to
    //              INITIAL_M so the table never shrinks below its
    //              original capacity.
    // Precondition:  newM >= 1.  Called only when a threshold is crossed.
    // Postcondition: table, M, and N are updated to reflect the new
    //                capacity. totalCollisions is NOT reset — it remains
    //                a lifetime cumulative counter. resizeCount increments.
    private void resize(int newM) {
        if (newM < INITIAL_M) newM = INITIAL_M; //clamp to minimum capacity
        Node[] oldTable = table;//save reference to the old bucket array
        table = new Node[newM];//allocate a fresh array of the new size
        M     = newM;
        N     = 0;
        resizeCount++;
        // Rehash every vehicle from the old table into the new one
        for (Node head : oldTable) {
            for (Node cur = head; cur != null; cur = cur.next) {
                int idx = (cur.vehicle.hashCode() & 0x7fffffff) % M;
                table[idx] = new Node(cur.vehicle, table[idx]);
                N++;
            }
        }
    }
}