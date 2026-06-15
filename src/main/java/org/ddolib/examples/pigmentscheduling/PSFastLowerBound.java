package org.ddolib.examples.pigmentscheduling;

import org.ddolib.modeling.FastLowerBound;
import org.ddolib.util.TSPLowerBound;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Computes a fast lower bound for the Pigment Sequencing Problem (PSP).
 * <p>
 * This class implements the {@link FastLowerBound} interface for {@link PSState}
 * and combines two complementary lower-bound estimations:
 * </p>
 * <ul>
 *     <li>A <b>changeover cost lower bound</b> derived from a precomputed
 *         Travelling Salesman Problem (TSP) lower bound on all subsets of items.</li>
 *     <li>A <b>stocking cost lower bound</b> computed using the Wagner-Whitin approach
 *         on the remaining items to produce.</li>
 * </ul>
 * <p>
 * These estimations follow the principle presented in the
 * <a href="https://webperso.info.ucl.ac.be/~pschaus/assets/thesis/2024-coppe.pdf">
 * PhD Thesis of Vianney Coppe (2024)</a>, which states that since the two cost components
 * (changeover and stocking) do not overlap, their respective lower bounds can be summed
 * to produce a stronger Relaxed Lower Bound (RLB) for the PSP.
 * </p>
 *
 * <p><b>Key idea:</b> The changeover costs are bounded using a TSP-like structure,
 * while the stocking costs are bounded based on the minimal inventory holding
 * costs required to satisfy all pending demands.</p>
 */
public class PSFastLowerBound implements FastLowerBound<PSState> {
    /** The production scheduling problem instance. */
    private final PSProblem problem;

    /** Precomputed TSP lower bounds for all subsets of items, indexed by bitmask. */
    private final int[] tspLb;
    /**
     * Constructs a fast lower bound evaluator for a given PSP instance.
     * <p>
     * During initialization, the TSP lower bounds for all subsets of item types
     * are precomputed using the  {@link TSPLowerBound#lowerBoundForAllSubsets(int[][])} method,
     * which allows for fast lookup during the search.
     * </p>
     *
     * @param problem the PSP instance for which this lower bound will be computed
     */
    public PSFastLowerBound(PSProblem problem) {
        this.problem = problem;
        tspLb = TSPLowerBound.lowerBoundForAllSubsets(problem.changeoverCost);
    }

    /**
     * Extracts the set of item types that are relevant in the given state.
     * <p>
     * This includes all item types that still have unmet demand and the next
     * item type to be produced, if any.
     * </p>
     *
     * @param state the current PSP state
     * @return a set of indices representing items that remain to be produced
     */
    private static Set<Integer> members(PSState state) {
        Set<Integer> mem = new HashSet<>();
        for (int i = 0; i < state.previousDemands.length; i++) {
            if (state.previousDemands[i] >= 0) {
                mem.add(i);
            }
        }
        if (state.next != -1) {
            mem.add(state.next);
        }
        return mem;
    }


    /**
     * Computes a fast (but admissible) lower bound on the remaining production cost
     * for the given PSP state.
     * <p>
     * The bound combines:
     * </p>
     * <ul>
     *     <li>A <b>changeover cost lower bound</b> obtained from a precomputed TSP bound
     *     on the subset of item types that remain to be produced.</li>
     *     <li>A <b>stocking cost lower bound</b> derived from the minimal inventory
     *     cost required to satisfy future demands, following a Wagner-Whitin formulation.</li>
     * </ul>
     * <p>
     * This heuristic offers an efficient way to estimate the minimal achievable cost
     * without solving the entire subproblem, thus improving pruning during the search.
     * </p>
     *
     * @param state     the current partial PSP state
     * @param variables the set of decision variables yet to be assigned
     * @return a non-negative lower bound on the remaining total cost
     */
    @Override
    public double fastLowerBound(PSState state, Set<Integer> variables) {
        // Convert to bitset-like index
        int idx = members(state).stream().
                mapToInt(Integer::intValue).
                reduce(0, (a, b) -> a | (1 << b));

        int changeOverLb = tspLb[idx]; // lower-bound on the changeOverCost

        int stockingCostLb = 0; // lower-bound on the stocking cost
        PriorityQueue<ItemDemand> itemDemands = new PriorityQueue<>(Comparator.comparingInt(ItemDemand::cost));
        for (int time = state.t - 1; time >= 0; time--) {
            for (int i = 0; i < state.previousDemands.length; i++) {
                int demand = state.previousDemands[i]; // previous demand of item i
                while (demand >= time) {
                    itemDemands.offer(new ItemDemand(problem.stockingCost[i], demand)); // Assuming pb.stocking is defined
                    demand = problem.previousDemands[i][demand];
                }
            }
            if (!itemDemands.isEmpty()) {
                ItemDemand item = itemDemands.poll();
                stockingCostLb += item.cost() * (item.deadLine() - time);
            }
        }
        return changeOverLb + stockingCostLb;
    }
    /**
     * A simple record used to represent an item demand, defined by its
     * stocking cost and deadline.
     *
     * @param cost     the unit stocking cost of the item
     * @param deadLine the time period by which the item must be produced
     */
    private record ItemDemand(int cost, int deadLine) {
    }
}
