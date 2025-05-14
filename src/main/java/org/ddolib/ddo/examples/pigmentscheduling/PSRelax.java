package org.ddolib.ddo.examples.pigmentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;
import org.ddolib.ddo.util.TSPLowerBound;
import static org.ddolib.ddo.examples.pigmentscheduling.PSProblem.IDLE;
import java.util.*;

public class PSRelax implements Relaxation<PSState> {

    record ItemDemand(int cost, int deadLline) { }

    // lower bound of the TSP for all subsets of items types
    // indices are the binary representation of the subsets
    public int [] tspLb;
    PSInstance instance;

    public PSRelax(PSInstance instance) {
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

    @Override
    public PSState mergeStates(final Iterator<PSState> states) {
        PSState currState = states.next();
        int[] prevDemands = Arrays.copyOf(currState.previousDemands, currState.previousDemands.length);
        int time = currState.t;
        while (states.hasNext()) {
            PSState state = states.next();
            time = Math.min(time, state.t);
            for (int i = 0; i < prevDemands.length; i++) {
                prevDemands[i] = Math.min(prevDemands[i], state.previousDemands[i]);
            }
        }
        return new PSState(time, IDLE, prevDemands);

    }


    /**
     * From the PhD Thesis of Vianney Coppe:
     * https://webperso.info.ucl.ac.be/~pschaus/assets/thesis/2024-coppe.pdf
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
     * @param state the state for which the estimate is to be computed
     * @param variables the set of unassigned variables
     * @return
     */
    @Override
    public int fastUpperBound(PSState state, final Set<Integer> variables) {
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
                stockingCostLb += item.cost * (time - item.deadLline);
            }
        }
        int ub = -changeOverLb - stockingCostLb;
        return ub;
    }


    @Override
    public int relaxEdge(PSState from, PSState to, PSState merged, Decision d, int cost) {
        return cost;
    }

    private long[] computeMST(int[][] changeover) {
        int n = changeover.length;
        long[] minEdge = new long[n];
        boolean[] inMST = new boolean[n];
        Arrays.fill(minEdge, Long.MAX_VALUE);
        minEdge[0] = 0; // Start from the first item
        long[] mstCost = new long[1 << n]; // To store the MST cost for each subset of nodes
        for (int i = 0; i < n; i++) {
            int u = -1;
            for (int j = 0; j < n; j++) {
                if (!inMST[j] && (u == -1 || minEdge[j] < minEdge[u])) {
                    u = j;
                }
            }
            inMST[u] = true;
            for (int v = 0; v < n; v++) {
                if (changeover[u][v] < minEdge[v]) {
                    minEdge[v] = changeover[u][v];
                }
            }
            // Update the MST cost for the current subset
            for (int mask = 0; mask < (1 << n); mask++) {
                if ((mask & (1 << u)) == 0) {
                    mstCost[mask | (1 << u)] = Math.min(mstCost[mask | (1 << u)], mstCost[mask] + minEdge[u]);
                }
            }
        }
        return mstCost;
    }

}