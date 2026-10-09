package org.ddolib.examples.layered.pigmentscheduling;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.common.util.io.InputReader;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.solving.ddo.core.Decision;

/**
 * Represents an instance of the Pigment Sequencing Problem (PSP) used within a Decision Diagram
 * Optimization (DDO) framework.
 *
 * <p>The {@code PSProblem} class models a single-machine scheduling problem with production
 * changeover costs, item-dependent stocking costs, and demand constraints distributed over a finite
 * planning horizon.
 *
 * <p>Each time period can be assigned to the production of one item or remain idle. The goal of the
 * optimization is to minimize the total cost, which includes:
 *
 * <ul>
 *   <li><b>Stocking costs</b> — penalizing early production of items before their demand dates.
 *   <li><b>Changeover costs</b> — incurred when switching production between different items.
 * </ul>
 *
 * <p>This class implements the {@link Problem} interface parameterized by {@link PSState}, and
 * defines all problem-specific behavior such as initial state construction, variable domain
 * generation, state transitions, and transition cost computation.
 *
 * <p>The structure of this problem is based on instances compatible with the PhD thesis of <a
 * href="https://webperso.info.ucl.ac.be/~pschaus/assets/thesis/2024-coppe.pdf">Vianney Coppe
 * (2024)</a>.
 */
public class PSProblem implements Problem<PSState> {

    /** Represents the idle state of the machine (no production). */
    public static final int IDLE = -1;

    /** Number of distinct item types. */
    final int nbItems;

    /** Total number of discrete time periods in the planning horizon. */
    final int horizon;

    /** Stocking cost for each item type (per unit time of early production). */
    final int[] stockingCost;

    /** Changeover cost matrix: cost of switching from item {@code i} to item {@code j}. */
    final int[][] changeoverCost;

    /**
     * For each item {@code i} and time {@code t}, gives the latest time slot before {@code t} in
     * which a demand for {@code i} occurred.
     *
     * <p>If no such demand exists, the value is {@code -1}.
     */
    final int[][] previousDemands;

    /** Optional known optimal objective value for benchmarking or validation purposes. */
    private final Optional<Double> optimal;

    /**
     * For each item {@code i} and time {@code t}, stores the total number of remaining demands for
     * item {@code i} in the time interval {@code [0..t]}.
     */
    int[][] remainingDemands;

    /** Optional name of the problem instance. */
    private Optional<String> name = Optional.empty();

    /**
     * Constructs a PSP instance from explicit data arrays and a known optimal value.
     *
     * @param nbItems number of item types
     * @param horizon number of time periods
     * @param stockingCost array of stocking costs for each item type
     * @param changeoverCost matrix of changeover costs between item types
     * @param previousDemands matrix of previous demand indices for each item and time
     * @param optimal known optimal objective value (if available)
     */
    public PSProblem(
            final int nbItems,
            final int horizon,
            final int[] stockingCost,
            final int[][] changeoverCost,
            final int[][] previousDemands,
            final Optional<Double> optimal) {
        this.nbItems = nbItems;
        this.horizon = horizon;
        this.stockingCost = stockingCost;
        this.changeoverCost = changeoverCost;
        this.previousDemands = previousDemands;
        this.optimal = optimal;
    }

    /**
     * Constructs a PSP instance from a data file.
     *
     * <p>The file format is expected to contain:
     *
     * <ol>
     *   <li>Horizon (number of time periods)
     *   <li>Number of item types
     *   <li>Number of orders
     *   <li>Changeover cost matrix ({@code nItems × nItems})
     *   <li>Stocking costs for each item
     *   <li>Demand matrix ({@code nItems × horizon})
     *   <li>Known optimal value (optional)
     * </ol>
     *
     * @param filename path to the input data file
     */
    public PSProblem(final String filename) {
        InputReader reader = new InputReader(filename);
        horizon = reader.getInt();
        nbItems = reader.getInt();
        reader.getInt(); // to skip the order

        changeoverCost = new int[nbItems][nbItems];
        for (int i = 0; i < nbItems; i++) {
            for (int j = 0; j < nbItems; j++) {
                changeoverCost[i][j] = reader.getInt();
            }
        }

        stockingCost = new int[nbItems];
        for (int i = 0; i < nbItems; i++) {
            stockingCost[i] = reader.getInt();
        }

        int[][] demands = new int[nbItems][horizon];
        for (int i = 0; i < nbItems; i++) {
            for (int j = 0; j < horizon; j++) {
                demands[i][j] = reader.getInt();
            }
        }

        optimal = Optional.of((double) reader.getInt());

        previousDemands = new int[nbItems][horizon + 1];
        for (int i = 0; i < nbItems; i++) {
            Arrays.fill(previousDemands[i], -1);
        }
        for (int t = 1; t <= horizon; t++) {
            for (int i = 0; i < nbItems; i++) {
                if (demands[i][t - 1] > 0) {
                    previousDemands[i][t] = t - 1;
                } else {
                    previousDemands[i][t] = previousDemands[i][t - 1];
                }
            }
        }
        remainingDemands = new int[nbItems][horizon];
        for (int i = 0; i < nbItems; i++) {
            for (int t = 0; t < horizon; t++) {
                if (t == 0) {
                    remainingDemands[i][t] = demands[i][t];
                } else {
                    remainingDemands[i][t] = remainingDemands[i][t - 1] + demands[i][t];
                }
            }
        }

        this.name = Optional.of(filename);
    }

    /**
     * Assigns a name to the problem instance for display or debugging purposes.
     *
     * @param name the name of the instance
     */
    public void setName(String name) {
        this.name = Optional.of(name);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name.orElse(super.toString());
    }

    /** {@inheritDoc} */
    @Override
    public int nbVars() {
        return horizon;
    }

    /** {@inheritDoc} */
    @Override
    public double initialValue() {
        return 0;
    }

    /**
     * Builds the initial problem state, where no items have been produced and the machine is idle.
     *
     * @return the initial {@link PSState} representing the start of the planning horizon
     */
    @Override
    public PSState initialState() {
        int[] prevDemands = new int[nbItems];
        for (int i = 0; i < nbItems; i++) {
            prevDemands[i] = previousDemands[i][horizon];
        }
        return new PSState(horizon, IDLE, prevDemands);
    }

    /**
     * Defines the domain of feasible decisions (items to produce or idle) for a given state and
     * time depth.
     *
     * <p>The domain depends on the remaining demands and available time periods. If there is
     * insufficient time to satisfy all remaining demands, the domain becomes empty. Otherwise, the
     * IDLE option may be included if there is enough slack in the schedule.
     *
     * @param state the current scheduling state
     * @param depth the depth of the decision variable (i.e., time index)
     * @return an iterator over feasible item indices (and possibly {@link #IDLE})
     */
    @Override
    public Iterator<Integer> domain(PSState state, int depth) {

        int t = horizon - depth - 1;
        IntStream dom = IntStream.range(0, nbItems).filter(i -> state.previousDemands[i] >= t);

        // total number of remaining demands <= t
        int remDemands =
                IntStream.range(0, nbItems)
                        .filter(i -> state.previousDemands[i] >= 0)
                        .map(i -> remainingDemands[i][state.previousDemands[i]])
                        .sum();

        // System.out.println("remDemands: " + remDemands);
        if (remDemands > t + 1) {
            // fail to produce all the remaining demands
            return Collections.emptyIterator();
        }
        if (remDemands < t + 1) {
            // ok to add IDLE, we have enough time to produce all the remaining demands
            return IntStream.concat(dom, IntStream.of(IDLE)).iterator();
        } else {
            // just enough time to produce remaining demands, no IDLE possible
            assert remDemands == t + 1;
            return dom.iterator();
        }
    }

    /**
     * Applies a production decision to the current state and returns the resulting new state.
     *
     * @param state the current scheduling state
     * @param decision the production decision (item index or {@link #IDLE})
     * @return the successor state after applying the decision
     */
    @Override
    public PSState transition(PSState state, Decision decision) {
        PSState ret = state.clone();
        int item = decision.value();
        if (item != IDLE) {
            ret.next = item;
            ret.previousDemands[item] = previousDemands[item][state.previousDemands[item]];
        }
        return ret;
    }

    /**
     * Computes the cost incurred by executing a given decision from the current state.
     *
     * <p>The cost consists of two components:
     *
     * <ul>
     *   <li><b>Stocking cost</b>: proportional to how early the produced item is made relative to
     *       its next demand time.
     *   <li><b>Changeover cost</b>: cost of switching from the last produced item to the current
     *       one.
     * </ul>
     *
     * @param state the current state
     * @param decision the production decision (item or idle)
     * @return the transition cost incurred by the decision
     */
    @Override
    public double transitionCost(PSState state, Decision decision) {
        int item = decision.value();
        if (item == IDLE) {
            return 0;
        } else {
            int t = (horizon - decision.variable() - 1);
            int duration = state.previousDemands[item] - t;
            int stocking = stockingCost[item] * duration;
            int changeover = state.next != IDLE ? changeoverCost[item][state.next] : 0;
            // stocking cost + changeover cost
            return changeover + stocking;
        }
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(
                    String.format(
                            "The solution %s does not match " + "the number %d variables",
                            Arrays.toString(solution), nbVars()));
        }

        // For each item, contains the time slots of the demands
        List<LinkedList<Integer>> nextDemand =
                Stream.generate(LinkedList<Integer>::new)
                        .limit(nbItems)
                        .collect(Collectors.toCollection(LinkedList::new));

        for (int i = 0; i < nbItems; i++) {
            for (int t = 0; t < horizon; t++) {
                int demand =
                        t == 0
                                ? remainingDemands[i][t]
                                : remainingDemands[i][t] - remainingDemands[i][t - 1];

                if (demand != 0) {
                    nextDemand.get(i).add(t);
                }
            }
        }

        int value = 0;
        int prevItem = solution[solution.length - 1];
        for (int t = 0; t < horizon; t++) {
            // The solution starts with the last time slots
            int item = solution[solution.length - 1 - t];
            int demand = nextDemand.get(item).remove();
            int duration = demand - t;
            value += duration * stockingCost[item];
            if (prevItem != item) {
                value += changeoverCost[prevItem][item];
            }
            prevItem = item;
        }

        return value;
    }
}
