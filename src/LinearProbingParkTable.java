public class LinearProbingParkTable implements SmartParkTable {

    // Resize thresholds  double when N/M >= 1/2, halve when N/M <= 1/8
    private static final double LOAD_HIGH = 0.5;
    private static final double LOAD_LOW  = 0.125;

    // Used to mark deleted slots — setting to null would break probe chains
    private static final Vehicle DELETED = new Vehicle("__DELETED__", "", "", -1);

    private int       M;
    private int       N;
    private int       tombstones; // number of DELETED slots
    private Vehicle[] table;
    private final int INITIAL_M;

    // Statistics
    private long totalProbes     = 0;
    private long totalCollisions = 0;
    private int  resizeCount     = 0;

    // Constructors
    public LinearProbingParkTable() {
        this(16);
    }

    public LinearProbingParkTable(int initialM) {
        this.M         = initialM;
        this.INITIAL_M = initialM;
        this.table     = new Vehicle[M];
        this.N         = 0;
        this.tombstones = 0;
    }

    // hash() — Maps a vehicle to a slot index
    private int hash(Vehicle v) {
        return (v.hashCode() & 0x7fffffff) % M;
    }

    // park() — probe i, i+1, i+2, ... until empty slot found
    @Override
    public int park(Vehicle v) {
        if ((double)(N + tombstones) / M >= LOAD_HIGH) resize(M * 2);

        int home      = hash(v);
        int firstTomb = -1;
        totalProbes++;

        // Count collision if home slot is taken by a different vehicle
        if (table[home] != null && table[home] != DELETED && !table[home].equals(v))
            totalCollisions++;

        for (int step = 0; step < M; step++) {
            int idx = (home + step) % M;
            if (step > 0) totalProbes++;

            if (table[idx] == null) {
                int dest = (firstTomb != -1) ? firstTomb : idx;
                if (firstTomb != -1) tombstones--;
                table[dest] = v;
                N++;
                return dest;
            }
            if (table[idx] == DELETED) {
                if (firstTomb == -1) firstTomb = idx; // remember first tombstone
            } else if (table[idx].equals(v)) {
                table[idx] = v; // update existing
                return idx;
            }
        }

        // Fallback to first tombstone if table is full of tombstones
        if (firstTomb != -1) {
            tombstones--;
            table[firstTomb] = v;
            N++;
            return firstTomb;
        }
        return -1;
    }

    // locate()-Returns the slot index of the vehicle, or -1 if not found
    @Override
    public int locate(Vehicle v) {
        int home = hash(v);
        totalProbes++;

        for (int step = 0; step < M; step++) {
            int idx = (home + step) % M;
            if (step > 0) totalProbes++;

            if (table[idx] == null) return -1; // empty slot means key not present
            if (table[idx] != DELETED && table[idx].equals(v)) return idx;
        }
        return -1;
    }

    // leave() —  places tombstone never set to null
    @Override
    public boolean leave(String licensePlate) {
        Vehicle probe = new Vehicle(licensePlate, "", "", 0);
        int home = hash(probe);
        totalProbes++;

        for (int step = 0; step < M; step++) {
            int idx = (home + step) % M;
            if (step > 0) totalProbes++;

            if (table[idx] == null) return false;
            if (table[idx] != DELETED &&
                    table[idx].getLicensePlate().equalsIgnoreCase(licensePlate)) {
                table[idx] = DELETED; // tombstone, not null
                tombstones++;
                N--;
                if (M > INITIAL_M && (double) N / M <= LOAD_LOW)
                    resize(M / 2);
                return true;
            }
        }
        return false;
    }
    // Returns the length of the longest contiguous run of occupied slots
    @Override
    public int maxCluster() {
        int max     = 0;
        int current = 0;
        for (int i = 0; i < 2 * M; i++) {
            int idx = i % M;
            if (table[idx] != null && table[idx] != DELETED) {
                current++;
                if (current > max) max = current;
            } else {
                current = 0;
            }
            if (i >= M && current == 0) break;
        }
        return max;
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

    // Rebuilds the table with a new capacity and rehashes all entries
    private void resize(int newM) {
        if (newM < INITIAL_M) newM = INITIAL_M;
        Vehicle[] old = table;
        table      = new Vehicle[newM];
        M          = newM;
        N          = 0;
        tombstones = 0;
        resizeCount++;

        for (Vehicle v : old) {
            if (v != null && v != DELETED) {
                int idx = (v.hashCode() & 0x7fffffff) % M;
                while (table[idx] != null) idx = (idx + 1) % M;
                table[idx] = v;
                N++;
            }
        }
    }
}