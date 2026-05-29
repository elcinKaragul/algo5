//
// Title:       LinearProbingParkTable — hash table with linear probing
// Author:      [Elçin Karagül / Kayra Arı]
// ID:          [10885319050 / 10001507]
// Section:     [04]
// Assignment:  5
// Description: Implements the SmartParkTable interface using open
//              addressing with linear probing.  On a collision the table
//              checks slots i, i+1, i+2, ... (wrapping around) until an
//              empty slot or the target vehicle is found.
//              Deleted slots use a DELETED tombstone sentinel so that
//              existing probe chains are not broken.
//              The table doubles when (N+tombstones)/M >= 0.5 and
//              halves when N/M <= 0.125 (and M > initial size).
//
public class LinearProbingParkTable implements SmartParkTable {

    // ---------------------------------------------------------------
    // Resize thresholds (open addressing is sensitive to load factor;
    // thresholds are much lower than for chaining).
    //
    // LOAD_HIGH: double when effective occupancy (live + tombstones) >= 50%.
    //            Tombstones are included because they consume probe-chain
    //            slots just like live entries.
    // LOAD_LOW:  halve when live occupancy <= 12.5 % (and M > INITIAL_M).
    // ---------------------------------------------------------------
    private static final double LOAD_HIGH = 0.5;
    private static final double LOAD_LOW  = 0.125;

    // DELETED sentinel — placed in a slot when a vehicle leaves so that
    // the probe chain for other vehicles through this slot is not broken.
    // It is a real Vehicle object but with a plate that can never be
    // entered legitimately, so equals() will never match it accidentally.
    private static final Vehicle DELETED = new Vehicle("__DELETED__", "", "", -1);

    private int       M;// Current slot count (table capacity)
    private int       N;// Number of live vehicles currently stored
    private int       tombstones; // number of DELETED slots
    private Vehicle[] table;// Flat array of Vehicle references
    private final int INITIAL_M;// Minimum capacity — never shrink below this

    // Statistics
    private long totalProbes     = 0;// Every slot examined counts as one probe
    private long totalCollisions = 0;// Incremented when home slot is occupied by another vehicle
    private int  resizeCount     = 0;// Number of resize operations performed

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    // LinearProbingParkTable() — default constructor with capacity 16.
    //
    // Summary:     Creates an empty linear-probing table with a small
    //              default initial capacity.
    // Precondition:  None.
    // Postcondition: An empty table with M=16 slots is ready.
    public LinearProbingParkTable() {
        this(16);
    }

    // LinearProbingParkTable(int) — constructor with caller-specified capacity.
    //
    // Summary:     Creates an empty linear-probing table with the given
    //              initial capacity. Used by the command parser (INIT ... M)
    //              and by experiment drivers.
    // Precondition:  initialM >= 2 (open addressing requires M > N at all
    //                times, so M must be at least 2 to hold one element).
    // Postcondition: An empty table with M = initialM slots is ready.
    //                INITIAL_M is fixed to initialM for this instance.
    public LinearProbingParkTable(int initialM) {
        this.M         = initialM;
        this.INITIAL_M = initialM;
        this.table     = new Vehicle[M];
        this.N         = 0;
        this.tombstones = 0;
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    // hash() — maps a vehicle to a slot index in [0, M).
    //
    // Summary:     Strips the sign bit from v.hashCode() with & 0x7fffffff
    //              then takes modulo M to obtain a valid array index.
    // Precondition:  v is a non-null Vehicle; M >= 1.
    // Postcondition: Returns an int in [0, M).
    private int hash(Vehicle v) {
        return (v.hashCode() & 0x7fffffff) % M;
    }

    // ---------------------------------------------------------------
    // SmartParkTable interface implementations
    // ---------------------------------------------------------------

    // park() — inserts or updates a vehicle using linear probing.
    //
    // Summary:     First checks whether a resize is needed (using live +
    //              tombstone count to avoid probing a near-full table).
    //              Computes the home slot, then probes i, i+1, i+2, ...
    //              (mod M) until:
    //                (a) a null slot is found → insert here (or at the
    //                    first tombstone seen, whichever came first),
    //                (b) a DELETED slot is found → remember it as a
    //                    candidate insertion point and continue probing
    //                    (the real key may lie further along the chain),
    //                (c) the exact vehicle is found → update in-place.
    //              A collision is counted when the home slot is occupied
    //              by a *different* (non-null, non-DELETED) vehicle.
    // Precondition:  v is a non-null Vehicle with a valid license plate.
    // Postcondition: v is stored in the table (or updated). Returns the
    //                slot index where v was placed, or -1 if all M slots
    //                are tombstones with no room (should never occur in
    //                normal operation due to resize thresholds).
    @Override
    public int park(Vehicle v) {
        // Pre-insertion resize check (include tombstones in load estimate)
        if ((double)(N + tombstones) / M >= LOAD_HIGH) resize(M * 2);

        int home      = hash(v);
        int firstTomb = -1;// Index of the first DELETED slot encountered
        totalProbes++;// Probing the home slot counts as one probe

        // Count collision if home slot is taken by a different vehicle
        if (table[home] != null && table[home] != DELETED && !table[home].equals(v))
            totalCollisions++;

        // Linear probe sequence: home, home+1, home+2, ... (mod M)
        for (int step = 0; step < M; step++) {
            int idx = (home + step) % M;
            if (step > 0) totalProbes++;// Probe count for every slot after home

            if (table[idx] == null) {
                // Empty slot found — insert at firstTomb if we saw one,
                // otherwise insert here.  Using firstTomb recycles deleted
                // slots and keeps the probe chain as short as possible.
                int dest = (firstTomb != -1) ? firstTomb : idx;
                if (firstTomb != -1) tombstones--;// One tombstone is being reused
                table[dest] = v;
                N++;
                return dest;
            }
            if (table[idx] == DELETED) {// Remember the first tombstone for potential reuse
                if (firstTomb == -1) firstTomb = idx;
            } else if (table[idx].equals(v)) {
                // Same plate already present — update all fields in-place
                table[idx] = v;
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
        return -1;// Table is completely full (should not happen with resize)
    }

    // locate() — searches for a vehicle by linear probing.
    //
    // Summary:     Probes i, i+1, i+2, ... starting from the home slot.
    //              Stops as soon as a null slot is reached (the key cannot
    //              be further along the chain) or the vehicle is found.
    //              DELETED slots are skipped without stopping the search.
    // Precondition:  v is a non-null Vehicle whose licensePlate field
    //                holds the plate to search for.
    // Postcondition: Returns the slot index of the matching vehicle,
    //                or -1 if the plate is not in the table.
    @Override
    public int locate(Vehicle v) {
        int home = hash(v);
        totalProbes++;// Probing the home slot

        for (int step = 0; step < M; step++) {
            int idx = (home + step) % M;
            if (step > 0) totalProbes++;

            if (table[idx] == null) return -1; // Gap in probe chain — key absent
            if (table[idx] != DELETED && table[idx].equals(v)) return idx;
        }
        return -1;
    }

    // leave() — removes a vehicle by placing a DELETED tombstone.
    //
    // Summary:     Probes the same sequence as locate().  When the
    //              vehicle is found, it is replaced with the DELETED
    //              sentinel rather than null.  Setting the slot to null
    //              would break probe chains for vehicles whose home slot
    //              is behind this one in the sequence.
    //              After removal, if N/M <= LOAD_LOW the table halves.
    // Precondition:  licensePlate is a non-null, non-empty string.
    // Postcondition: Returns true if the vehicle was found and tombstoned.
    //                Returns false if the plate does not exist in the table.
    @Override
    public boolean leave(String licensePlate) {
        // Build a probe Vehicle solely to compute the home slot
        Vehicle probe = new Vehicle(licensePlate, "", "", 0);
        int home = hash(probe);
        totalProbes++; //probing the home slot

        for (int step = 0; step < M; step++) {
            int idx = (home + step) % M;
            if (step > 0) totalProbes++;

            if (table[idx] == null) return false; //probe chain ended / not found
            if (table[idx] != DELETED &&
                    table[idx].getLicensePlate().equalsIgnoreCase(licensePlate)) {
                table[idx] = DELETED; // tombstone, not null
                tombstones++;
                N--;
                //shrink the table if load factor has fallen below threshold
                if (M > INITIAL_M && (double) N / M <= LOAD_LOW)
                    resize(M / 2);
                return true;
            }
        }
        return false;
    }
    // maxCluster() — returns the longest contiguous run of occupied slots.
    //
    // Summary:     Scans the table array twice (2*M iterations) to handle
    //              clusters that wrap around the end of the array.  Counts
    //              only live (non-null, non-DELETED) slots as occupied.
    //              Resets the counter to 0 whenever a gap (null or DELETED)
    //              is encountered.  Terminates early once the second pass
    //              produces a gap, meaning no wraparound cluster extends
    //              further.
    // Precondition:  None.
    // Postcondition: Returns a non-negative int. Returns 0 iff N == 0.
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
            // Once we are in the second pass and hit a gap, no wraparound
            // cluster can be longer than what we have already counted
            if (i >= M && current == 0) break;
        }
        return max;
    }
    // stats() — prints current table statistics on one line.
    //
    // Summary:     Outputs: N=<n> M=<m> ALPHA=<alpha_3dp> COLLISIONS=<c>
    //              MAX_CLUSTER=<mc>  where MAX_CLUSTER is the longest
    //              contiguous run of occupied (live) slots.
    // Precondition:  None.
    // Postcondition: One line printed to stdout; no state is changed.
    @Override
    public void stats() {
        System.out.println("N=" + N + " M=" + M +
                String.format(" ALPHA=%.3f", (double) N / M) +
                " COLLISIONS=" + totalCollisions +
                " MAX_CLUSTER=" + maxCluster());
    }
    // Simple accessors for external statistics collection
    @Override public int  getN()          { return N; }
    @Override public int  getM()          { return M; }
    @Override public long getCollisions() { return totalCollisions; }

    // ---------------------------------------------------------------
    // Private resize helper
    // ---------------------------------------------------------------

    // resize() — rebuilds the table with a new capacity and rehashes all live entries.
    //
    // Summary:     Allocates a new Vehicle array of size newM, clears
    //              tombstones and N, then reinserts every live vehicle
    //              using plain linear probing under the new M.  Tombstones
    //              are not copied — the clean table has no deleted slots
    //              after a resize.  The minimum size is clamped to
    //              INITIAL_M so the table never shrinks below its
    //              original capacity.
    // Precondition:  newM >= 1.  Called only when a threshold is crossed.
    // Postcondition: table, M, N, and tombstones are updated.
    //                totalCollisions is NOT reset — it remains cumulative.
    //                resizeCount increments by 1.
    private void resize(int newM) {
        if (newM < INITIAL_M) newM = INITIAL_M; //clamp to minimum capacity

        Vehicle[] old = table;//save reference to the old flat array

        table      = new Vehicle[newM];
        M          = newM;
        N          = 0; //recounted during rehash
        tombstones = 0;//fresh table has no deleted slots
        resizeCount++;

        //rehash every live vehicle into new table
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