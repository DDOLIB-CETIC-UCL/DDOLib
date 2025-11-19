package org.ddolib.examples.msct;


import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Represents an instance of the **Maximum Sum of Completion Times (MSCT)** problem.
 * <p>
 * The MSCT problem consists in scheduling a set of jobs on a single machine
 * to minimize the total completion time (or sum of finishing times).
 * Each job {@code j} has:
 * <ul>
 *   <li>a <b>release date</b> {@code release[j]} — the earliest time the job can start,</li>
 *   <li>a <b>processing time</b> {@code processing[j]} — the duration required to complete the job.</li>
 * </ul>
 * The problem can be defined formally as:
 * <pre>
 *   minimize   ∑ C_j
 *   subject to start_j ≥ release_j
 *               start_j + processing_j = C_j
 *               machine processes only one job at a time
 * </pre>
 *
 * <p>
 * This class implements the {@link Problem} interface to be used by generic optimization
 * solvers (A*, ACS, DDO, etc.) from the decision diagram framework.
 * </p>
 *
 * <p>
 * Multiple constructors are provided to:
 * </p>
 * <ul>
 *   <li>Load a problem instance from a text file,</li>
 *   <li>Manually provide release and processing times,</li>
 *   <li>Randomly generate a synthetic instance for testing.</li>
 * </ul>
 *
 *
 * <p><b>Expected file format:</b></p>
 * <ul>
 *   <li>The first line contains the number of jobs, optionally followed by the known optimal value.</li>
 *   <li>Each of the next {@code n} lines contains two integers:
 *       the release time and processing time of each job.</li>
 *   <li>Example:</li>
 * </ul>
 * <pre>
 * 5 215.0
 * 0 3
 * 1 2
 * 2 1
 * 0 4
 * 3 2
 * </pre>
 *
 * @see MSCTState
 * @see MSCTFastLowerBound
 * @see MSCTRelax
 * @see MSCTRanking
 */
public class MSCTProblem implements Problem<MSCTState> {

    /**
     * The release time of each job.
     * {@code release[i]} gives the earliest time at which job {@code i} can start.
     */
    final int[] release;

    /**
     * The processing time of each job.
     * {@code processing[i]} gives the duration required to complete job {@code i}.
     */
    final int[] processing;

    /**
     * The known optimal solution value, if available.
     * Used for testing or benchmarking purposes.
     */
    final Optional<Double> optimal;

    private Optional<String> name = Optional.empty();

    /**
     * Constructs an MSCT problem from explicit arrays of release and processing times,
     * with an optional known optimal value.
     *
     * @param release    an array of release times for each job.
     * @param processing an array of processing times for each job.
     * @param optimal    an {@link Optional} containing the known optimal solution value.
     */
    public MSCTProblem(final int[] release, final int[] processing, Optional<Double> optimal) {
        this.release = release;
        this.processing = processing;
        this.optimal = optimal;
    }

    /**
     * Constructs an MSCT problem from explicit arrays of release and processing times,
     * without specifying an optimal value.
     *
     * @param release    an array of release times for each job.
     * @param processing an array of processing times for each job.
     */
    public MSCTProblem(final int[] release, final int[] processing) {
        this.release = release;
        this.processing = processing;
        this.optimal = Optional.empty();
    }

    /**
     * Constructs an MSCT problem by reading data from a text file.
     * <p>
     * The file must contain the number of jobs, optionally followed by the optimal value,
     * and then one line per job with its release and processing times.
     * </p>
     *
     * @param fname the path to the file containing the problem instance.
     * @throws IOException if an error occurs while reading the file.
     */
    public MSCTProblem(final String fname) throws IOException {
        boolean isFirst = true;
        String line;
        int count = 0;
        int nVar = 0;
        int[] releas = new int[0];
        int[] proces = new int[0];
        Optional<Double> optimal = Optional.empty();
        try (final BufferedReader bf = new BufferedReader(new FileReader(fname))) {
            while ((line = bf.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    String[] tokens = line.split("\\s+");
                    nVar = Integer.parseInt(tokens[0]);
                    if (tokens.length == 2) {
                        optimal = Optional.of(Double.parseDouble(tokens[1]));
                    }
                    releas = new int[nVar];
                    proces = new int[nVar];
                } else {
                    if (count < nVar) {
                        String[] tokens = line.split("\\s+");
                        releas[count] = Integer.parseInt(tokens[0]);
                        proces[count] = Integer.parseInt(tokens[1]);
                        count++;
                    }
                }
            }
        }
        this.release = releas;
        this.processing = proces;
        this.optimal = optimal;
        this.name = Optional.of(fname);
    }

    /**
     * Constructs a randomly generated MSCT problem instance for testing purposes.
     *
     * @param n the number of jobs to generate.
     */
    public MSCTProblem(final int n) {
        int[] release = new int[n];
        int[] processing = new int[n];
        Random rand = new Random(100);
        for (int i = 0; i < n; i++) {
            release[i] = rand.nextInt(10);
            processing[i] = 1 + rand.nextInt(10);
        }
        this.release = release;
        this.processing = processing;
        this.optimal = Optional.empty();

    }

    /**
     * Returns the known optimal value of this instance, if available.
     *
     * @return the negated optimal value wrapped in an {@link Optional}, or empty if unknown.
     */
    @Override
    public Optional<Double> optimalValue() {
        return optimal.map(x -> -x);
    }

    /**
     * Returns the number of jobs in this instance.
     *
     * @return the number of variables (jobs).
     */
    @Override
    public int nbVars() {
        return release.length;
    }

    /**
     * Returns the initial state of the problem, where all jobs are unscheduled.
     *
     * @return an {@link MSCTState} representing all jobs remaining and current time = 0.
     */
    @Override
    public MSCTState initialState() {
        Set<Integer> jobs = new HashSet<>();
        for (int i = 0; i < nbVars(); i++) {
            jobs.add(i);
        }
        return new MSCTState(jobs, 0);
    }

    /**
     * Returns the initial cost value of the problem (always 0 at the start).
     *
     * @return 0.0
     */
    @Override
    public double initialValue() {
        return 0;
    }

    /**
     * Returns the domain of possible decisions at the current state.
     * <p>
     * The domain corresponds to the indices of remaining jobs that can still be scheduled.
     * </p>
     *
     * @param state the current state.
     * @param var   the variable index (not used here but required by the interface).
     * @return an iterator over the remaining job indices.
     */
    @Override
    public Iterator<Integer> domain(MSCTState state, int var) {
        return state.remainingJobs().iterator();
    }

    /**
     * Computes the next state resulting from scheduling a given job.
     *
     * @param state    the current scheduling state.
     * @param decision the decision representing the next job to schedule.
     * @return a new {@link MSCTState} reflecting the updated remaining jobs and current time.
     */
    @Override
    public MSCTState transition(MSCTState state, Decision decision) {
        Set<Integer> remaining = new HashSet<>(state.remainingJobs());
        remaining.remove(decision.val());
        int currentTime = Math.max(state.currentTime(), release[decision.val()]) + processing[decision.val()];
        return new MSCTState(remaining, currentTime);
    }

    /**
     * Returns the cost of scheduling a given job from the current state.
     * <p>
     * The cost corresponds to the completion time of the scheduled job.
     * </p>
     *
     * @param state    the current scheduling state.
     * @param decision the decision representing the next job to schedule.
     * @return the completion time of the selected job.
     */
    @Override
    public double transitionCost(MSCTState state, Decision decision) {
        return Math.max(state.currentTime(), release[decision.val()]) + processing[decision.val()];
    }

    /**
     * Returns a string representation of this MSCT instance for debugging purposes.
     *
     * @return a string listing the release and processing times.
     */
    @Override
    public String toString() {
        return name.orElse(String.format("release: %s - processing: %s", Arrays.toString(release),
                Arrays.toString(processing)));
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not match " +
                    "the number %d variables", Arrays.toString(solution), nbVars()));
        }

        int t = 0;
        int value = 0;
        for (int job : solution) {
            t = Math.max(t, release[job]) + processing[job];
            value += t;
        }

        return value;
    }
}
