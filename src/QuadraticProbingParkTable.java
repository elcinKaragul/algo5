//
// Title:       QuadraticProbingParkTable — hash table with quadratic probing
// Author:      [Elçin Karagül / Kayra Arı]
// ID:          [10885319050 / 10001507]
// Section:     [04]
// Assignment:  5
// Description: Implements the SmartParkTable interface using open
//              addressing with quadratic probing.  On a collision the
//              table checks slots h, h+1², h+2², h+3², ... (mod M).
//              Using quadratic steps reduces primary clustering compared
//              to linear probing, because colliding keys scatter to
//              different slots rather than piling up consecutively.
//              Table size M is always kept prime so that the quadratic
//              sequence visits at least M/2 distinct slots, guaranteeing
//              an empty slot will be found when α < 0.5.
//              The table doubles (to next prime) when
//              (N+tombstones)/M >= 0.5 and halves when N/M <= 0.125.
//
public class QuadraticProbingParkTable implements SmartParkTable {
    // ---------------------------------------------------------------
    // Resize thresholds — same as linear probing.
    // Keeping α below 0.5 is critical for quadratic probing: when M is
    // prime and α < 0.5, the quadratic sequence is guaranteed to find an
    // empty slot within M/2 probes.
    // ---------------------------------------------------------------
    private static final double LOAD_HIGH = 0.5;
    private static final double LOAD_LOW  = 0.125;

    // DELETED sentinel — same rationale as in LinearProbingParkTable.
    // Quadratic probe chains must not be broken by setting slots to null
    // on deletion; a tombstone preserves chain continuity.
    private static final Vehicle DELETED = new Vehicle("__DELETED__", "", "", -1);

    private int       M; //current slot count
    private int       N; //number of live vehicles currently stored
    private int       tombstones; // number of DELETED slots
    private Vehicle[] table;//flat array of vehicle references
    private final int INITIAL_M; // mininmum capacity do not shrink below this

    // Statistics
    private long totalProbes     = 0; //every slot examined counts as one probe
    private long totalCollisions = 0; //incremented when home slot is occupied by another vehicle
    private int  resizeCount     = 0; //number of resize operations performed

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    // QuadraticProbingParkTable() — default constructor; capacity set to
    // the next prime >= 16.
    //
    // Summary:     Creates an empty quadratic-probing table with a small
    //              default prime capacity.
    // Precondition:  None.
    // Postcondition: An empty table with M = nextPrime(16) = 17 is ready.
    public QuadraticProbingParkTable() {
        this(nextPrime(16));
    }

    // QuadraticProbingParkTable(int) — constructor with caller-specified capacity.
    //
    // Summary:     The requested capacity is rounded up to the next prime
    //              before use.  The prime requirement ensures full coverage
    //              of the quadratic probe sequence.
    // Precondition:  initialM >= 2.
    // Postcondition: An empty table with M = nextPrime(initialM) is ready.
    //                INITIAL_M is fixed to this prime for the lifetime of
    //                the instance.
    public QuadraticProbingParkTable(int initialM) {
        this.M          = nextPrime(initialM); // table size must be prime for full coverage
        this.INITIAL_M  = this.M;
        this.table      = new Vehicle[this.M];
        this.N          = 0;
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

    // park() — inserts or updates a vehicle using quadratic probing.
    //
    // Summary:     The probe sequence uses offsets 0², 1², 2², 3², ...
    //              i.e. slot = (home + k²) % M  for k = 0, 1, 2, ...
    //              The loop variable k starts at 1 and the index is
    //              computed as (home + (k-1)²) % M so that k=1 gives
    //              offset 0 (the home slot) and k=2 gives offset 1², etc.
    //              This matches the lecture slide convention.
    //              Long arithmetic is used for (k-1)*(k-1) to prevent
    //              integer overflow for large k.
    //              The first tombstone slot encountered is remembered as
    //              a candidate insertion point; the probe continues to
    //              check whether the plate already exists further along.
    //              A collision is counted when the home slot is occupied
    //              by a *different* live vehicle.
    //              Resize is checked *before* insertion using live +
    //              tombstone count (tombstones consume probe-chain slots).
    // Precondition:  v is a non-null Vehicle with a valid license plate.
    // Postcondition: v is stored in the table (or updated). Returns the
    //                slot index where v was placed, or -1 if no slot was
    //                found (should not happen with correct resize logic).
    @Override
    public int park(Vehicle v) {
        //pre-insertion resize check
        if ((double)(N + tombstones) / M >= LOAD_HIGH) resize(nextPrime(M * 2));

        int home      = hash(v);
        int firstTomb = -1; //index of the  first deleted slot encounter
        totalProbes++;// Probing the home slot (k=1, offset=0²=0)


        // Count collision if home slot is taken by a different vehicle
        if (table[home] != null && table[home] != DELETED && !table[home].equals(v))
            totalCollisions++;

        // Quadratic probe sequence: offsets 0, 1, 4, 9, 16, ... (k-1)²
        for (int k = 1; k <= M; k++) {
            // Use long arithmetic to avoid overflow: (k-1)² can be large
            int idx = (int)(((long) home + (long)(k-1)*(k-1)) % M);
            if (k > 1) totalProbes++;// Probe count for every slot after home

            if (table[idx] == null) {
                // Empty slot — insert at firstTomb if available, else here
                int dest = (firstTomb != -1) ? firstTomb : idx;
                if (firstTomb != -1) tombstones--;// Reusing a tombstone slot
                table[dest] = v;
                N++;
                return dest;
            }
            if (table[idx] == DELETED) {
                // Record the first tombstone for potential reuse
                if (firstTomb == -1) firstTomb = idx;
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

    // locate() — searches for a vehicle using quadratic probing.
    //
    // Summary:     Probes the same quadratic sequence as park().
    //              Stops as soon as a null slot is found (the key cannot
    //              exist beyond a gap in the probe chain) or the vehicle
    //              is found.  DELETED slots are skipped without stopping.
    // Precondition:  v is a non-null Vehicle whose licensePlate field
    //                holds the plate to search for.
    // Postcondition: Returns the slot index of the matching vehicle,
    //                or -1 if the plate is not in the table.
    @Override
    public int locate(Vehicle v) {
        int home = hash(v);
        totalProbes++;

        for (int k = 1; k <= M; k++) {
            int idx = (int)(((long) home + (long)(k-1)*(k-1)) % M);
            if (k > 1) totalProbes++;

            if (table[idx] == null) return -1; // empty slot means key not present
            if (table[idx] != DELETED && table[idx].equals(v)) return idx;
        }
        return -1;
    }

    // leave() — removes a vehicle by placing a DELETED tombstone.
    //
    // Summary:     Probes the same quadratic sequence to find the vehicle.
    //              Replaces its slot with DELETED rather than null, so
    //              that probe chains for other vehicles are not broken.
    //              After removal, shrinks if N/M <= LOAD_LOW.
    // Precondition:  licensePlate is a non-null, non-empty string.
    // Postcondition: Returns true if found and tombstoned; false otherwise.
    @Override
    public boolean leave(String licensePlate) {
        Vehicle probe = new Vehicle(licensePlate, "", "", 0);
        int home = hash(probe);
        totalProbes++;

        for (int k = 1; k <= M; k++) {
            int idx = (int)(((long) home + (long)(k-1)*(k-1)) % M);
            if (k > 1) totalProbes++;

            if (table[idx] == null) return false;
            if (table[idx] != DELETED &&
                    table[idx].getLicensePlate().equalsIgnoreCase(licensePlate)) {
                table[idx] = DELETED; // tombstone, not null
                tombstones++;
                N--;
                if (M > INITIAL_M && (double) N / M <= LOAD_LOW)
                    resize(nextPrime(M / 2));
                return true;
            }
        }
        return false;
    }

    // maxCluster() — returns the longest contiguous run of occupied slots.
    //
    // Summary:     Scans the table twice (2*M iterations) to handle runs
    //              that wrap around the array boundary.  Only live
    //              (non-null, non-DELETED) slots count as occupied.
    //              Note: for quadratic probing, primary clustering is
    //              reduced but secondary clustering (keys with the same
    //              home slot share the same probe sequence) can still
    //              produce runs; this metric captures that.
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
            if (i >= M && current == 0) break;
        }
        return max;
    }

    // stats() — prints current table statistics on one line.
    //
    // Summary:     Outputs: N=<n> M=<m> ALPHA=<alpha_3dp> COLLISIONS=<c>
    //              MAX_CLUSTER=<mc>  where MAX_CLUSTER is the longest
    //              contiguous run of live slots.
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

   // ---------------------------------------------------------------
    // Private resize helper
    // ---------------------------------------------------------------

    // resize() — rebuilds the table with a new prime capacity and rehashes all live entries.
    //
    // Summary:     The requested new size is rounded up to the next prime
    //              (required for quadratic probing coverage guarantees).
    //              Allocates a fresh array, clears tombstones and N, then
    //              reinserts every live vehicle using the quadratic probe
    //              sequence under the new M.  Tombstones are not copied —
    //              the rebuilt table starts clean.
    // Precondition:  newM >= 1.  Called only when a threshold is crossed.
    // Postcondition: table, M, N, and tombstones reflect the new capacity.
    //                totalCollisions is NOT reset — it remains cumulative.
    //                resizeCount increments by 1.
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
                int home = (v.hashCode() & 0x7fffffff) % M;
                for (int k = 1; k <= M; k++) {
                    int idx = (int)(((long) home + (long)(k-1)*(k-1)) % M);
                    if (table[idx] == null) { table[idx] = v; N++; break; }
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Prime-number utilities (required to keep M prime)
    // ---------------------------------------------------------------

    // nextPrime() — returns the smallest prime number >= n.
    //
    // Summary:     Starts from n (made odd if even) and increments by 2
    //              until isPrime() returns true.  Handles edge cases n < 2
    //              and n == 2.  Used to compute new table sizes after a
    //              resize so that M remains prime throughout the table's
    //              lifetime.
    // Precondition:  n >= 1.
    // Postcondition: Returns a prime p such that p >= n.
    static int nextPrime(int n) {
        if (n < 2) return 2;
        if (n % 2 == 0) n++;
        while (!isPrime(n)) n += 2;
        return n;
    }

    // isPrime() — trial-division primality test.
    //
    // Summary:     Returns true iff n is a prime number.  Uses trial
    //              division up to sqrt(n), checking only odd divisors
    //              after handling 2 as a special case.  Sufficient for
    //              the table sizes used in this assignment (up to ~10^6).
    // Precondition:  n >= 0.
    // Postcondition: Returns true iff n has no divisors other than 1 and itself.

    private static boolean isPrime(int n) {
        if (n < 2)  return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        // Check odd divisors up to sqrt(n); cast to long to avoid overflow
        for (int i = 3; (long)i*i <= n; i += 2)
            if (n % i == 0) return false;
        return true;
    }
}