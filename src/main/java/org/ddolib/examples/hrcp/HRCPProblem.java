package org.ddolib.examples.hrcp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Human-Robot Collaboration with Precedences (HRCP) scheduling problem.
 * <p>
 * Each task must be executed in one of three modes:
 * <ul>
 *     <li><b>0 – Human</b>: the human completes the task alone.</li>
 *     <li><b>1 – Robot</b>: the robot completes the task alone.</li>
 *     <li><b>2 – Collaborative</b>: human and robot work together.</li>
 * </ul>
 * Tasks are linked by precedence constraints: a task cannot start until all its
 * predecessors are finished.
 * <p>
 * <b>DP encoding.</b> There are {@code n + 1} decision variables.
 * Variables {@code 0 … n-1} represent scheduling steps; their domain values encode
 * both the chosen <em>task</em> and the chosen <em>mode</em> as {@code task * 3 + mode}.
 * Variable {@code n} is a dummy whose sole purpose is to inject
 * {@code max(tH, tR)} into the accumulated cost, so that the total equals the makespan.
 * <p>
 * Modes whose duration ≥ {@value #BIG_M} are considered infeasible and excluded
 * from the domain.
 */
public class HRCPProblem implements Problem<HRCPState> {

    /** Sentinel value for infeasible modes. */
    public static final int BIG_M = 100_000;

    /** Number of real tasks. */
    public final int n;

    /** Duration of each task in human-only mode. */
    public final int[] humanDurations;

    /** Duration of each task in robot-only mode. */
    public final int[] robotDurations;

    /** Duration of each task in collaborative mode. */
    public final int[] collaborationDurations;

    /** {@code predecessors[j]} = list of immediate predecessors of task {@code j}. */
    public final List<Integer>[] predecessors;

    /** {@code successors[j]} = list of immediate successors of task {@code j}. */
    public List<Integer>[] successors;

    /** A topological ordering of all tasks (predecessors before successors). */
    public int[] topologicalOrder;

    /** Minimum feasible duration for each task: {@code min(h, r, c)} ignoring BIG_M entries. */
    public int[] minDuration;

    /** Optional known optimal solution value. */
    public final Optional<Double> optimal;

    /** Optional name of the instance (usually the filename). */
    public final Optional<String> name;

    // ------------------------------------------------------------------ //
    //  Constructors                                                       //
    // ------------------------------------------------------------------ //

    /**
     * Creates an HRCP instance.
     *
     * @param humanDurations         durations for the human-only mode
     * @param robotDurations         durations for the robot-only mode
     * @param collaborationDurations durations for the collaborative mode
     * @param precedences            {@code precedences[j]} = list of predecessors of task j
     */
    @SuppressWarnings("unchecked")
    public HRCPProblem(int[] humanDurations,
                       int[] robotDurations,
                       int[] collaborationDurations,
                       List<Integer>[] precedences) {

        this.n = humanDurations.length;
        this.humanDurations = humanDurations;
        this.robotDurations = robotDurations;
        this.collaborationDurations = collaborationDurations;
        this.predecessors = precedences;
        this.optimal = Optional.empty();
        this.name = Optional.empty();
        initDerived();
    }

    /**
     * Constructs an HRCP problem from a text file.
     * <p>
     * File format (lines starting with {@code #} are ignored):
     * <ul>
     *     <li>First non-comment line: {@code n} (number of tasks), optionally followed
     *         by the known optimal value.</li>
     *     <li>Next {@code n} lines:
     *         {@code humanDur robotDur collabDur numPreds [pred1 pred2 …]}
     *         where {@code numPreds} is the number of immediate predecessors
     *         and the remaining integers are the predecessor task indices.</li>
     * </ul>
     *
     * @param fname path to the instance file
     * @throws IOException if the file cannot be read
     */
    @SuppressWarnings("unchecked")
    public HRCPProblem(String fname) throws IOException {
        List<String> lines = readNonCommentLines(fname);

        // --- header ---
        String[] header = lines.get(0).trim().split("\\s+");
        int numTasks = Integer.parseInt(header[0]);
        this.optimal = header.length >= 2
                ? Optional.of(Double.parseDouble(header[1]))
                : Optional.empty();
        this.name = Optional.of(fname);

        // --- task data ---
        this.n = numTasks;
        this.humanDurations = new int[n];
        this.robotDurations = new int[n];
        this.collaborationDurations = new int[n];
        this.predecessors = new List[n];

        for (int i = 0; i < n; i++) {
            String[] tok = lines.get(1 + i).trim().split("\\s+");
            humanDurations[i] = Integer.parseInt(tok[0]);
            robotDurations[i] = Integer.parseInt(tok[1]);
            collaborationDurations[i] = Integer.parseInt(tok[2]);
            int numPreds = Integer.parseInt(tok[3]);
            List<Integer> preds = new ArrayList<>(numPreds);
            for (int p = 0; p < numPreds; p++) {
                preds.add(Integer.parseInt(tok[4 + p]));
            }
            predecessors[i] = preds;
        }

        initDerived();
    }

    /** Initialises successors, minDuration and topologicalOrder from the core fields. */
    @SuppressWarnings("unchecked")
    private void initDerived() {
        // --- build successors ---
        this.successors = new List[n];
        for (int j = 0; j < n; j++) successors[j] = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            for (int p : predecessors[j]) {
                successors[p].add(j);
            }
        }

        // --- minimum duration per task ---
        this.minDuration = new int[n];
        for (int j = 0; j < n; j++) {
            minDuration[j] = Math.min(humanDurations[j],
                    Math.min(robotDurations[j], collaborationDurations[j]));
        }

        // --- topological order (Kahn's algorithm) ---
        this.topologicalOrder = computeTopologicalOrder();
    }

    private int[] computeTopologicalOrder() {
        int[] inDegree = new int[n];
        for (int j = 0; j < n; j++) inDegree[j] = predecessors[j].size();

        int[] order = new int[n];
        int head = 0, tail = 0;
        for (int j = 0; j < n; j++) {
            if (inDegree[j] == 0) order[tail++] = j;
        }
        while (head < tail) {
            int j = order[head++];
            for (int s : successors[j]) {
                if (--inDegree[s] == 0) order[tail++] = s;
            }
        }
        assert tail == n : "Cycle detected in precedence graph";
        return order;
    }

    // ------------------------------------------------------------------ //
    //  Problem interface                                                   //
    // ------------------------------------------------------------------ //

    /** {@code n} scheduling steps + 1 dummy variable. */
    @Override
    public int nbVars() {
        return n + 1;
    }

    @Override
    public HRCPState initialState() {
        return new HRCPState(0L, 0, 0, new int[n]);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    /**
     * Domain for a scheduling step.
     * <ul>
     *     <li>Steps 0 … n-1: every (eligible task, feasible mode) pair, encoded as
     *         {@code task * 3 + mode}.</li>
     *     <li>Step n (dummy): single value {@code 0}.</li>
     * </ul>
     * A task is <em>eligible</em> when all its predecessors have been scheduled.
     */
    @Override
    public Iterator<Integer> domain(HRCPState state, int var) {
        if (var == n) {
            return List.of(0).iterator();
        }
        long sched = state.scheduled;
        List<Integer> values = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            if ((sched & (1L << j)) != 0) continue;        // already scheduled
            if (!allPredecessorsScheduled(j, sched)) continue; // not yet eligible
            if (humanDurations[j] < BIG_M)         values.add(j * 3);
            if (robotDurations[j] < BIG_M)          values.add(j * 3 + 1);
            if (collaborationDurations[j] < BIG_M)  values.add(j * 3 + 2);
        }
        return values.iterator();
    }

    /**
     * Transition function.
     * <p>
     * Scheduling task {@code j} in the chosen mode updates {@code tH}, {@code tR},
     * the scheduled bitmask, and the readiness vector for {@code j}'s successors.
     */
    @Override
    public HRCPState transition(HRCPState state, Decision decision) {
        int var = decision.variable();
        int val = decision.value();
        if (var == n) return state;  // dummy

        int task = val / 3;
        int mode = val % 3;

        int rj = state.readiness[task];
        int start, completion;
        int newTH = state.tH;
        int newTR = state.tR;

        switch (mode) {
            case 0 -> { // Human alone
                start = Math.max(state.tH, rj);
                completion = start + humanDurations[task];
                newTH = completion;
            }
            case 1 -> { // Robot alone
                start = Math.max(state.tR, rj);
                completion = start + robotDurations[task];
                newTR = completion;
            }
            case 2 -> { // Collaborative
                start = Math.max(Math.max(state.tH, state.tR), rj);
                completion = start + collaborationDurations[task];
                newTH = completion;
                newTR = completion;
            }
            default -> throw new IllegalArgumentException("Invalid mode: " + mode);
        }

        long newScheduled = state.scheduled | (1L << task);
        int[] newReadiness = state.readiness.clone();
        for (int k : successors[task]) {
            newReadiness[k] = Math.max(newReadiness[k], completion);
        }

        return new HRCPState(newScheduled, newTH, newTR, newReadiness);
    }

    /**
     * Transition cost.
     * <ul>
     *     <li>Real scheduling steps: cost is 0 (timing tracked in state).</li>
     *     <li>Dummy variable: cost is {@code max(tH, tR)} = makespan.</li>
     * </ul>
     */
    @Override
    public double transitionCost(HRCPState state, Decision decision) {
        if (decision.variable() == n) {
            return Math.max(state.tH, state.tR);
        }
        return 0;
    }

    /**
     * Evaluates the makespan of a complete solution.
     *
     * @param solution array of length {@code n + 1}.
     *                 {@code solution[i]} for {@code i < n} encodes the i-th scheduling
     *                 decision as {@code task * 3 + mode}.
     *                 {@code solution[n]} is the dummy value (0).
     * @return the makespan
     */
    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(
                    String.format("Expected %d entries but got %d", nbVars(), solution.length));
        }
        long sched = 0L;
        int tH = 0, tR = 0;
        int[] readiness = new int[n];

        for (int i = 0; i < n; i++) {
            int task = solution[i] / 3;
            int mode = solution[i] % 3;

            if (task < 0 || task >= n) {
                throw new InvalidSolutionException("Invalid task index " + task + " at step " + i);
            }
            if ((sched & (1L << task)) != 0) {
                throw new InvalidSolutionException("Task " + task + " scheduled twice");
            }
            if (!allPredecessorsScheduled(task, sched)) {
                throw new InvalidSolutionException(
                        "Task " + task + " scheduled before all predecessors");
            }

            int rj = readiness[task];
            int start, completion;
            switch (mode) {
                case 0 -> {
                    start = Math.max(tH, rj);
                    completion = start + humanDurations[task];
                    tH = completion;
                }
                case 1 -> {
                    start = Math.max(tR, rj);
                    completion = start + robotDurations[task];
                    tR = completion;
                }
                case 2 -> {
                    start = Math.max(Math.max(tH, tR), rj);
                    completion = start + collaborationDurations[task];
                    tH = completion;
                    tR = completion;
                }
                default -> throw new InvalidSolutionException("Invalid mode " + mode);
            }

            sched |= (1L << task);
            for (int k : successors[task]) {
                readiness[k] = Math.max(readiness[k], completion);
            }
        }

        return Math.max(tH, tR);
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    /** Returns {@code true} when every predecessor of task {@code j} is in {@code sched}. */
    boolean allPredecessorsScheduled(int j, long sched) {
        for (int p : predecessors[j]) {
            if ((sched & (1L << p)) == 0) return false;
        }
        return true;
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public String toString() {
        return name.orElseGet(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("HRCP Problem (%d tasks)%n", n));
            sb.append("Human:   ").append(Arrays.toString(humanDurations)).append('\n');
            sb.append("Robot:   ").append(Arrays.toString(robotDurations)).append('\n');
            sb.append("Collab:  ").append(Arrays.toString(collaborationDurations)).append('\n');
            for (int j = 0; j < n; j++) {
                sb.append(String.format("Task %d predecessors: %s%n", j, predecessors[j]));
            }
            return sb.toString();
        });
    }

    // ------------------------------------------------------------------ //
    //  File-reading helper                                                 //
    // ------------------------------------------------------------------ //

    /** Reads all non-empty, non-comment lines from a file. */
    private static List<String> readNonCommentLines(String fname) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }
}

