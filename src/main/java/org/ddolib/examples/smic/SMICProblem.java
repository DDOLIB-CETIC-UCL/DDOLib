package org.ddolib.examples.smic;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.File;
import java.io.IOException;
import java.util.*;
/**
 * The {@code SMICProblem} class represents an instance of the
 * <b>Single Machine with Inventory Constraint (SMIC)</b> scheduling problem.
 * <p>
 * In this problem, a set of jobs must be processed on a single machine,
 * subject to release times, inventory capacity limits, and job types
 * that either consume or produce inventory units.
 * The objective is typically to minimize the total completion time or a related cost.
 * </p>
 *
 * <p>
 * Each job is defined by:
 * </p>
 * <ul>
 *     <li>Its {@code processing} time,</li>
 *     <li>Its {@code release} time,</li>
 *     <li>Its {@code type} (0 = consuming, 1 = producing inventory),</li>
 *     <li>Its {@code inventory} change (how much it consumes or produces),</li>
 *     <li>Its {@code weight}, used in objective computations (if applicable).</li>
 * </ul>
 * The machine starts with an initial inventory {@code initInventory} and must never
 * exceed the maximum capacity {@code capaInventory} nor drop below zero.
 *
 * <p>
 * This class implements the {@link Problem} interface, making it compatible
 * with DDO (Decision Diagram Optimization) and other optimization frameworks.
 * It provides methods to define:
 * </p>
 * <ul>
 *     <li>The initial state of the problem,</li>
 *     <li>The possible transitions between states,</li>
 *     <li>The associated transition costs,</li>
 *     <li>The domain of feasible decisions at each step.</li>
 * </ul>
 *
 * @see SMICState
 * @see SMICFastLowerBound
 * @see SMICRelax
 * @see SMICRanking
 * @see SMICDominance
 */
public class SMICProblem implements Problem<SMICState> {

    /** Name or identifier of the problem instance. */
    final String name;

    /** Total number of jobs in the instance. */
    final int nbJob;

    /** Initial inventory level at the beginning of the schedule. */
    final int initInventory;

    /** Maximum allowed inventory capacity. */
    final int capaInventory;

    /** Job types (0 for consumption, 1 for production). */
    final int[] type;

    /** Processing times of each job. */
    final int[] processing;

    /** Weights associated with each job (optional for weighted objectives). */
    final int[] weight;

    /** Release times for each job (when the job becomes available). */
    final int[] release;

    /** Inventory variation of each job (how much it consumes or produces). */
    final int[] inventory;

    /** Optional known optimal value (used for benchmarking). */
    private Optional<Double> optimal;
    /**
     * Constructs a {@code SMICProblem} instance with full specification.
     *
     * @param name           the name of the instance
     * @param nbJob          number of jobs
     * @param initInventory  initial inventory level
     * @param capaInventory  maximum inventory capacity
     * @param type           job types (0 = consume, 1 = produce)
     * @param processing     processing times for each job
     * @param weight         weights associated with each job
     * @param release        release times for each job
     * @param inventory      inventory change (positive for production, negative for consumption)
     * @param optimal        optional optimal objective value (if known)
     */
    public SMICProblem(String name,
                       int nbJob,
                       int initInventory,
                       int capaInventory,
                       int[] type,
                       int[] processing,
                       int[] weight,
                       int[] release,
                       int[] inventory,
                       Optional<Double> optimal) {
        this.name = name;
        this.nbJob = nbJob;
        this.initInventory = initInventory;
        this.capaInventory = capaInventory;
        this.type = type;
        this.processing = processing;
        this.weight = weight;
        this.release = release;
        this.inventory = inventory;
        this.optimal = optimal;
    }
    /**
     * Constructs a {@code SMICProblem} instance without a known optimal value.
     *
     * @param name           the name of the instance
     * @param nbJob          number of jobs
     * @param initInventory  initial inventory level
     * @param capaInventory  maximum inventory capacity
     * @param type           job types (0 = consume, 1 = produce)
     * @param processing     processing times for each job
     * @param weight         weights associated with each job
     * @param release        release times for each job
     * @param inventory      inventory change (positive for production, negative for consumption)
     */
    public SMICProblem(String name,
                       int nbJob,
                       int initInventory,
                       int capaInventory,
                       int[] type,
                       int[] processing,
                       int[] weight,
                       int[] release,
                       int[] inventory) {
        this.name = name;
        this.nbJob = nbJob;
        this.initInventory = initInventory;
        this.capaInventory = capaInventory;
        this.type = type;
        this.processing = processing;
        this.weight = weight;
        this.release = release;
        this.inventory = inventory;
        this.optimal = Optional.empty();
    }
    /**
     * Constructs a {@code SMICProblem} instance by parsing a text file.
     * <p>
     * The file format must follow the convention:
     * </p>
     * <pre>
     * nbJob  initInventory  capaInventory
     * type_i  processing_i  weight_i  release_i  inventory_i  (for each job)
     * [optional: optimal_value]
     * </pre>
     *
     * @param filename the path to the problem file
     * @throws IOException if the file cannot be read or parsed correctly
     */
    public SMICProblem(String filename) throws IOException {
        Scanner s = new Scanner(new File(filename)).useDelimiter("\\s+");
        int nbJob = 0;
        int initInventory = 0;
        int capaInventory = 0;
        int[] type = new int[0];
        int[] processing = new int[0];
        int[] weight = new int[0];
        int[] release = new int[0];
        int[] inventory = new int[0];
        this.name = filename;
        while (!s.hasNextLine()) {
            s.nextLine();
        }
        nbJob = s.nextInt();
        initInventory = s.nextInt();
        capaInventory = s.nextInt();
        type = new int[nbJob];
        processing = new int[nbJob];
        weight = new int[nbJob];
        release = new int[nbJob];
        inventory = new int[nbJob];
        Optional<Double> opti = Optional.empty();
        for (int i = 0; i < nbJob; i++) {
            type[i] = s.nextInt();
            processing[i] = s.nextInt();
            weight[i] = s.nextInt();
            release[i] = s.nextInt();
            inventory[i] = s.nextInt();
        }
        if (s.hasNextInt()) {
            opti = Optional.of(s.nextDouble());
        }
        s.close();
        this.nbJob = nbJob;
        this.initInventory = initInventory;
        this.capaInventory = capaInventory;
        this.type = type;
        this.processing = processing;
        this.weight = weight;
        this.release = release;
        this.inventory = inventory;
        this.optimal = opti;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public int nbVars() {
        return nbJob;
    }
    /**
     * Returns the initial state of the problem, where all jobs remain to be processed
     * and the machine starts at time 0 with the initial inventory.
     *
     * @return the initial {@link SMICState}
     */
    @Override
    public SMICState initialState() {
        Set<Integer> jobs = new HashSet<>();
        for (int i = 0; i < nbVars(); i++) {
            jobs.add(i);
        }
        return new SMICState(jobs, 0, initInventory, initInventory);
    }

    /** {@inheritDoc} */
    @Override
    public double initialValue() {
        return 0;
    }

    /**
     * Returns the feasible domain of jobs that can be scheduled next,
     * given the current inventory and remaining capacity constraints.
     *
     * @param state the current {@link SMICState}
     * @param var   the current decision variable index (unused)
     * @return an iterator over feasible job indices
     */

    @Override
    public Iterator<Integer> domain(SMICState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        for (Integer job : state.remainingJobs()) {
            int deltaInventory = (type[job] == 0) ? - inventory[job] : + inventory[job];
            int minCurrentInventory = state.minCurrentInventory() + deltaInventory;
            int maxCurrentInventory = state.maxCurrentInventory() + deltaInventory;
            if (maxCurrentInventory >= 0 && minCurrentInventory <= capaInventory) {
                domain.add(job);
            }
        }
        return domain.iterator();
    }
    /**
     * Applies a decision to transition from the current state to the next.
     * <p>
     * The method removes the scheduled job from the remaining set,
     * updates the current time and adjusts inventory levels.
     * </p>
     *
     * @param state    the current state
     * @param decision the job to schedule next
     * @return the resulting {@link SMICState} after applying the decision
     */
    @Override
    public SMICState transition(SMICState state, Decision decision) {
        Set<Integer> remaining = new HashSet<>(state.remainingJobs());
        int job = decision.val();
        remaining.remove(job);
        int currentTime = Math.max(state.currentTime(), release[job]) + processing[job];
        int deltaInventory = (type[job] == 0) ? - inventory[job] : + inventory[job];
        int minCurrentInventory = state.minCurrentInventory() + deltaInventory;
        int maxCurrentInventory = state.maxCurrentInventory() + deltaInventory;
        return new SMICState(remaining, currentTime, minCurrentInventory, maxCurrentInventory);
    }
    /**
     * Computes the cost associated with scheduling a job from the current state.
     * <p>
     * The cost includes any waiting time due to release constraints and
     * the job’s processing time.
     * </p>
     *
     * @param state    the current state
     * @param decision the decision (job) being scheduled
     * @return the cost of performing the transition
     */
    @Override
    public double transitionCost(SMICState state, Decision decision) {
        return Math.max(release[decision.val()] - state.currentTime(), 0) + processing[decision.val()];
    }
}
