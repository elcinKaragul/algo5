public class ChainingParkTable implements SmartParkTable {

    // Resize thresholds ( double when N/M >= 8, halve when N/M <= 2)
    private static final int LOAD_HIGH = 8;
    private static final int LOAD_LOW  = 2;

    private int    M;           // number of buckets (table capacity)
    private int    N;           // number of vehicles currently stored
    private Node[] table;
    private final int INITIAL_M;

    // Statistics
    private long totalProbes     = 0;
    private long totalCollisions = 0;
    private int  resizeCount     = 0;

    // Constructors
    public ChainingParkTable() {
        this(16);
    }

    public ChainingParkTable(int initialM) {
        this.M         = initialM;
        this.INITIAL_M = initialM;
        this.table     = new Node[M];
        this.N         = 0;
    }

    // Maps a vehicle to a bucket index
    private int hash(Vehicle v) {
        return (v.hashCode() & 0x7fffffff) % M;
    }

    // Inserts vehicle at the front of the chain at its bucket
    @Override
    public int park(Vehicle v) {
        int idx = hash(v);
        totalProbes++;

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

    // locate() — search only the relevant bucket
    // Returns the slot index of the vehicle, or -1 if not found
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

    // leave() — only modify the chain containing the key
    // Removes vehicle with given plate, returns true if found
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

    // Returns the length of the longest chain
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

    // resize() — rehash all keys into new table
    private void resize(int newM) {
        if (newM < INITIAL_M) newM = INITIAL_M;
        Node[] oldTable = table;
        table = new Node[newM];
        M     = newM;
        N     = 0;
        resizeCount++;

        for (Node head : oldTable) {
            for (Node cur = head; cur != null; cur = cur.next) {
                int idx = (cur.vehicle.hashCode() & 0x7fffffff) % M;
                table[idx] = new Node(cur.vehicle, table[idx]);
                N++;
            }
        }
    }
}