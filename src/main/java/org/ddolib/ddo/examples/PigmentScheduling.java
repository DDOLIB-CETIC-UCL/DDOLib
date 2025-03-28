package org.ddolib.ddo.examples;


import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.core.SearchStatistics;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.ddolib.ddo.io.InputReader;
import org.ddolib.ddo.util.TSPLowerBound;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * The Pigment Sequencing Problem (PSP) is a single-machine production planning problem
 * that aims to minimize the stocking and changeover costs while satisfying a set of orders.
 * There are different item types I = {0,...,𝑛−1}.
 * For each type, a given stocking cost S_i to pay for each time period
 * between the production and the deadline of an order.
 * For each pair i,j in I of item types, a changeover cost C_ij is incurred
 * whenever the machine switches the production from item type i to j.
 * Finally, the demand matrix Q contains all the orders: Q_i^p in {0,1}
 * indicates whether there is an order for item type i in I at time period p.
 * 0 ≤ p &lt; H where H is the time horizon.
 */
public class PigmentScheduling {

    public static final int IDLE = -1; // represent the idle state of the machine i.e. no production

    static class PSPState {
        int t; // the current time slot
        int next; // the item type produced at time t+1, -1 means we don't know yet
        int [] previousDemands; // previousDemands[i] = largest time slot < t where a demand for item i occurs,
                                //                      -1 if no more demands before

        public PSPState(int t, int next, int[] previousDemands) {
            this.t = t;
            this.next = next;
            this.previousDemands = previousDemands;
        }

        @Override
        protected PSPState clone() {
            return new PSPState(t, next, Arrays.copyOf(previousDemands, previousDemands.length));
        }
    }

    static class PSP implements Problem<PSPState> {

        private final PSPInstance instance;

        public PSP(PSPInstance instance) {
            this.instance = instance;
        }

        @Override
        public int nbVars() {
            return instance.horizon;
        }

        @Override
        public int initialValue() {
            return 0;
        }

        @Override
        public PSPState initialState() {
            int prevDemands[] = new int[instance.nItems];
            for (int i = 0; i < instance.nItems; i++) {
                prevDemands[i] = instance.previousDemands[i][instance.horizon];
            }
            return new PSPState(instance.horizon, IDLE, prevDemands);
        }

        /**
         *
         * @param state the state from which the transitions should be applicable
         * @param depth the variable whose domain in being queried
         * @return
         */
        @Override
        public Iterator<Integer> domain(PSPState state, int depth) {

            int t = instance.horizon - depth - 1;
            IntStream dom = IntStream.range(0, instance.nItems)
                    .filter(i -> state.previousDemands[i] >= t);

            int [] dom2 = IntStream.range(0, instance.nItems)
                    .filter(i -> state.previousDemands[i] >= t).toArray();


            // total number of remaining demands <= t
            int remDemands = IntStream.range(0, instance.nItems)
                    .filter(i -> state.previousDemands[i] >= 0)
                    .map(i -> instance.remainingDemands[i][state.previousDemands[i]])
                    .sum();

            //System.out.println("remDemands: " + remDemands);
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

        @Override
        public PSPState transition(PSPState state, Decision decision) {
            PSPState ret = state.clone();
            int item = decision.val();
            if (item != IDLE) {
                ret.next = item;
                ret.previousDemands[item] = instance.previousDemands[item][state.previousDemands[item]];
            }
            return ret;
        }

        @Override
        public int transitionCost(PSPState state, Decision decision) {
            int item = decision.val();
            if (item == IDLE) {
                return 0;
            } else {
                int t = (instance.horizon - decision.var() - 1);
                int duration = state.previousDemands[item] - t;
                int stocking = instance.stockingCost[item] * duration;
                int changeover = state.next != -1 ? instance.changeoverCost[item][state.next] : 0;
                // stocking cost + changeover cost
                return -(changeover + stocking);
            }
        }
    }

    public static class PSPRelax implements Relaxation<PSPState> {

        record ItemDemand(int cost, int deadLline) { }

        // lower bound of the TSP for all subsets of items types
        // indices are the binary representation of the subsets
        public int [] tspLb;
        PSPInstance instance;

        public PSPRelax(PSPInstance instance) {
            this.instance = instance;
            tspLb = TSPLowerBound.lowerBoundForAllSubsets(instance.changeoverCost);
        }

        // Set of item types that have an unsatisfied demand,
        // plus the next item type produced if any
        private static Set<Integer> members(PSPState state) {
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
        public PSPState mergeStates(final Iterator<PSPState> states) {
            PSPState currState = states.next();
            int[] prevDemands = Arrays.copyOf(currState.previousDemands, currState.previousDemands.length);
            int time = currState.t;
            while (states.hasNext()) {
                PSPState state = states.next();
                time = Math.min(time, state.t);
                for (int i = 0; i < prevDemands.length; i++) {
                    prevDemands[i] = Math.min(prevDemands[i], state.previousDemands[i]);
                }
            }
            return new PSPState(time, IDLE, prevDemands);
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
        public int fastUpperBound(PSPState state, final Set<Integer> variables) {
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
            /*
        let mut prev_demands = state.prev_demands.clone();
        let mut ww = 0;
        let mut items = BinaryHeap::new();
        for time in (0..state.time).rev() {
            for (i, demand) in prev_demands.iter_mut().enumerate() {
                while *demand >= time as isize {
                    items.push((self.pb.stocking[i], *demand));
                    *demand = self.pb.prev_demands[i][*demand as usize];
                }
            }

            if let Some((cost, deadline)) = items.pop() {
                ww += cost as isize * (time as isize - deadline);
            }
        }
             */

            int ub = -changeOverLb - stockingCostLb;
            return ub;
            //return Integer.MAX_VALUE;
        }


        @Override
        public int relaxEdge(PSPState from, PSPState to, PSPState merged, Decision d, int cost) {
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

    static class PSPRanking implements StateRanking<PSPState> {
        @Override
        public int compare(PSPState s1, PSPState s2) {
            // the state with the smallest total demand is the best (not sure about this)
            int totS1 = Arrays.stream(s1.previousDemands).sum();
            int totS2 = Arrays.stream(s2.previousDemands).sum();
            return Integer.compare(totS1, totS2);
        }
    }

    static class PSPInstance {

        int nItems;
        int horizon;
        // dim = nItems
        int[] stockingCost; // cost of stocking item i

        // dim = nItems x nItems
        int[][] changeoverCost; // cost of changing from item i to item j

        // dim = nItems x (horizon+1)
        int[][] previousDemands; // previousDemands[i][t] = the largest time slot < t where a demand for item i occurs

        // dim = nItems x horizon
        int[][] remainingDemands; // remainingDemands[i][t] = the total demand for item i on [0..t]

        int optimal; // optimal objective value

        PSPInstance(String path) {

            InputReader reader = new InputReader(path);
            horizon = reader.getInt();
            nItems = reader.getInt();
            int nOrders = reader.getInt();

            changeoverCost = new int[nItems][nItems];
            for (int i = 0; i < nItems; i++) {
                for (int j = 0; j < nItems; j++) {
                    changeoverCost[i][j] = reader.getInt();
                }
            }

            stockingCost = new int[nItems];
            for (int i = 0; i < nItems; i++) {
                stockingCost[i] = reader.getInt();
            }

            int [][] demands = new int[nItems][horizon];
            for (int i = 0; i < nItems; i++) {
                for (int j = 0; j < horizon; j++) {
                    demands[i][j] = reader.getInt();
                }
            }

            optimal = reader.getInt();

            previousDemands = new int[nItems][horizon + 1];
            for (int i = 0; i < nItems; i++) {
                Arrays.fill(previousDemands[i], -1);
            }
            for (int t = 1; t <= horizon; t++) {
                for (int i = 0; i < nItems; i++) {
                    if (demands[i][t - 1] > 0) {
                        previousDemands[i][t] = t - 1;
                    } else {
                        previousDemands[i][t] = previousDemands[i][t - 1];
                    }
                }
            }
            remainingDemands = new int[nItems][horizon];
            for (int i = 0; i < nItems; i++) {
                for (int t = 0; t < horizon; t++) {
                    if (t == 0) {
                        remainingDemands[i][t] = demands[i][t];
                    } else {
                        remainingDemands[i][t] = remainingDemands[i][t - 1] + demands[i][t];
                    }
                }
            }
        }
    }

    public static void main(final String[] args) throws IOException {
        PSPInstance instance = new PSPInstance("data/PSP/various/pigment15b.txt");;
        PSP problem = new PSP(instance);
        final PSPRelax relax = new PSPRelax(instance);
        final PSPRanking ranking = new PSPRanking();
        final FixedWidth<PSPState> width = new FixedWidth<>(100);
        final VariableHeuristic<PSPState> varh = new DefaultVariableHeuristic();
        final Frontier<PSPState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] values = new int[problem.nbVars()];
                    for (Decision d : decisions) {
                        int t = (instance.horizon - d.var() - 1);
                        values[t] = d.val();
                    }
                    return values;
                })
                .get();

        System.out.println(String.format("Duration : %.3f", duration));
        System.out.println(String.format("Objective: %d", solver.bestValue().get()));
        System.out.println(String.format("Solution : %s", Arrays.toString(solution)));
        System.out.println(stats);

    }
}

