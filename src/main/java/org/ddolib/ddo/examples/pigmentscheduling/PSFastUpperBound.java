package org.ddolib.ddo.examples.pigmentscheduling;

import org.ddolib.ddo.heuristics.FastUpperBound;
import org.ddolib.ddo.util.TSPLowerBound;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Implementation of a fast upper bound for the PSP.
 */
public class PSFastUpperBound implements FastUpperBound<PSState> {
    private final PSInstance instance;
    private final int[] tspLb;

    public PSFastUpperBound(PSInstance instance) {
        this.instance = instance;
        tspLb = TSPLowerBound.lowerBoundForAllSubsets(instance.changeoverCost);
    }

    // Set of item types that have an unsatisfied demand,
    // plus the next item type produced if any
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
     * From the:
     * <a href="https://webperso.info.ucl.ac.be/~pschaus/assets/thesis/2024-coppe.pdf"> PhD Thesis of Vianney Coppe</a>
     * "When the changeover costs are ignored, the PSP falls under the Wagner-
     * Whitin conditions that allow to compute the optimal stocking cost
     * for a given set of remaining items to produce. Conversely, if the stocking
     * costs and the delivery constraints are omitted, the PSP can be reduced to the
     * TSP. Therefore, a valid lower bound on the total changeover cost to produce
     * a remaining set of items is to take the total weight of a Minimum Spanning
     * Tree computed on the graph of changeover costs limited to item types that
     * still need to be produced. The optimal weight for all these spanning trees can
     * be precomputed because the number of items is usually small. As there is no
     * overlap between the two lower bounds described, the RLB for the PSP can
     * sum their individual contributions to obtain a stronger lower bound."
     *
     * @param state     the state for which the estimate is to be computed
     * @param variables the set of unassigned variables
     * @return
     */
    @Override
    public double fastUpperBound(PSState state, Set<Integer> variables) {
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
                    itemDemands.offer(new ItemDemand(instance.stockingCost[i], demand)); // Assuming pb.stocking is defined
                    demand = instance.previousDemands[i][demand];
                }
            }
            if (!itemDemands.isEmpty()) {
                ItemDemand item = itemDemands.poll();
                stockingCostLb += item.cost() * (time - item.deadLine());
            }
        }
        return -changeOverLb - stockingCostLb;
    }

    private record ItemDemand(int cost, int deadLine) {
    }
}
