package org.ddolib.examples.hrc;

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
 * Represents an instance of the Human-Robot Collaboration (HRC) scheduling problem.
 * <p>
 * Each task must be executed in one of three modes:
 * <ul>
 *     <li><b>0 – Human</b>: the human completes the task alone.</li>
 *     <li><b>1 – Robot</b>: the robot completes the task alone.</li>
 *     <li><b>2 – Collaborative</b>: both the human and the robot work together.</li>
 * </ul>
 * <p>
 * The objective is to minimise the makespan:
 * <pre>
 *     C_max = T_C + max(T_H, T_R)
 * </pre>
 * where {@code T_H} is the total human-only time, {@code T_R} the total robot-only time,
 * and {@code T_C} the total collaborative time.
 * </p>
 * <p>
 * <b>DP encoding.</b> The state {@link HRCState} tracks the accumulated human time
 * ({@code tH}) and robot time ({@code tR}). The collaborative time is accumulated
 * through transition costs. An extra <em>dummy variable</em> (variable index {@code n})
 * is appended at the end: its transition cost equals {@code max(tH, tR)}, so that the
 * total accumulated cost along any path equals the makespan.
 * </p>
 * <p>
 * Modes whose duration is &ge; {@value #BIG_M} are considered infeasible and are
 * excluded from the domain.
 * </p>
 */
public class HRCProblem implements Problem<HRCState> {

    /** Sentinel value used to indicate an infeasible mode. */
    public static final int BIG_M = 100_000;

    /** Number of real tasks. */
    public final int n;

    /** Duration of each task when executed by the human alone. */
    public final int[] humanDurations;

    /** Duration of each task when executed by the robot alone. */
    public final int[] robotDurations;

    /** Duration of each task when executed collaboratively. */
    public final int[] collaborationDurations;

    /** Optional known optimal solution value. */
    public final Optional<Double> optimal;

    /** Optional name of the instance (usually the filename). */
    public final Optional<String> name;

    /**
     * Constructs an HRC problem instance.
     *
     * @param humanDurations         durations for the human-only mode
     * @param robotDurations         durations for the robot-only mode
     * @param collaborationDurations durations for the collaborative mode
     */
    public HRCProblem(int[] humanDurations,
                      int[] robotDurations,
                      int[] collaborationDurations) {
        assert humanDurations.length == robotDurations.length
                && robotDurations.length == collaborationDurations.length;
        this.n = humanDurations.length;
        this.humanDurations = humanDurations;
        this.robotDurations = robotDurations;
        this.collaborationDurations = collaborationDurations;
        this.optimal = Optional.empty();
        this.name = Optional.empty();
    }

    /**
     * Constructs an HRC problem from a text file.
     * <p>
     * File format (lines starting with {@code #} are ignored):
     * <ul>
     *     <li>First non-comment line: {@code n} (number of tasks), optionally followed
     *         by the known optimal value.</li>
     *     <li>Next {@code n} lines: {@code humanDur robotDur collabDur} for each task.</li>
     * </ul>
     *
     * @param fname path to the instance file
     * @throws IOException if the file cannot be read
     */
    public HRCProblem(String fname) throws IOException {
        List<String> lines = readNonCommentLines(fname);

        // --- header ---
        String[] header = lines.get(0).trim().split("\\s+");
        int numTasks = Integer.parseInt(header[0]);
        Optional<Double> opt = header.length >= 2
                ? Optional.of(Double.parseDouble(header[1]))
                : Optional.empty();

        // --- task durations ---
        int[] h = new int[numTasks];
        int[] r = new int[numTasks];
        int[] c = new int[numTasks];
        for (int i = 0; i < numTasks; i++) {
            String[] tok = lines.get(1 + i).trim().split("\\s+");
            h[i] = Integer.parseInt(tok[0]);
            r[i] = Integer.parseInt(tok[1]);
            c[i] = Integer.parseInt(tok[2]);
        }

        this.n = numTasks;
        this.humanDurations = h;
        this.robotDurations = r;
        this.collaborationDurations = c;
        this.optimal = opt;
        this.name = Optional.of(fname);
    }

    /**
     * Returns the number of decision variables: {@code n} real tasks plus one dummy
     * variable used to inject the {@code max(tH, tR)} term into the objective.
     */
    @Override
    public int nbVars() {
        return n + 1;
    }

    @Override
    public HRCState initialState() {
        return new HRCState(0, 0);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    /**
     * Returns the feasible modes for a variable.
     * <ul>
     *     <li>For real tasks (0 &le; var &lt; n): a subset of {0, 1, 2} excluding
     *         modes with duration &ge; {@link #BIG_M}.</li>
     *     <li>For the dummy variable (var == n): {0}.</li>
     * </ul>
     */
    @Override
    public Iterator<Integer> domain(HRCState state, int var) {
        if (var == n) {
            return List.of(0).iterator();
        }
        List<Integer> modes = new ArrayList<>(3);
        if (humanDurations[var] < BIG_M) modes.add(0);
        if (robotDurations[var] < BIG_M) modes.add(1);
        if (collaborationDurations[var] < BIG_M) modes.add(2);
        return modes.iterator();
    }

    /**
     * Computes the successor state after applying a decision.
     * <ul>
     *     <li>Human (0): {@code tH} increases by {@code humanDurations[var]}.</li>
     *     <li>Robot (1): {@code tR} increases by {@code robotDurations[var]}.</li>
     *     <li>Collaborative (2) or dummy: state is unchanged.</li>
     * </ul>
     */
    @Override
    public HRCState transition(HRCState state, Decision decision) {
        int var = decision.variable();
        int val = decision.value();
        if (var == n) {
            return state; // dummy
        }
        return switch (val) {
            case 0 -> new HRCState(state.tH() + humanDurations[var], state.tR());
            case 1 -> new HRCState(state.tH(), state.tR() + robotDurations[var]);
            case 2 -> state;
            default -> throw new IllegalArgumentException("Invalid mode: " + val);
        };
    }

    /**
     * Returns the incremental cost of a decision.
     * <ul>
     *     <li>Human (0) or Robot (1): cost is 0 (time tracked in the state).</li>
     *     <li>Collaborative (2): cost is the collaborative duration.</li>
     *     <li>Dummy variable: cost is {@code max(tH, tR)}.</li>
     * </ul>
     */
    @Override
    public double transitionCost(HRCState state, Decision decision) {
        int var = decision.variable();
        int val = decision.value();
        if (var == n) {
            return Math.max(state.tH(), state.tR());
        }
        return switch (val) {
            case 0, 1 -> 0;
            case 2 -> collaborationDurations[var];
            default -> throw new IllegalArgumentException("Invalid mode: " + val);
        };
    }

    /**
     * Evaluates the makespan of a complete solution.
     *
     * @param solution an array of length {@code n + 1} where {@code solution[i]} is the
     *                 mode for task {@code i} (0, 1, or 2) and {@code solution[n] == 0}.
     * @return the makespan {@code T_C + max(T_H, T_R)}
     * @throws InvalidSolutionException if the solution is malformed
     */
    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(
                    String.format("Expected %d entries but got %d", nbVars(), solution.length));
        }
        int tH = 0, tR = 0, tC = 0;
        for (int i = 0; i < n; i++) {
            switch (solution[i]) {
                case 0 -> tH += humanDurations[i];
                case 1 -> tR += robotDurations[i];
                case 2 -> tC += collaborationDurations[i];
                default -> throw new InvalidSolutionException(
                        "Invalid mode " + solution[i] + " for task " + i);
            }
        }
        return tC + Math.max(tH, tR);
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public String toString() {
        return name.orElse(String.format("HRC Problem (%d tasks)%nHuman:   %s%nRobot:   %s%nCollab:  %s",
                n,
                Arrays.toString(humanDurations),
                Arrays.toString(robotDurations),
                Arrays.toString(collaborationDurations)));
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

