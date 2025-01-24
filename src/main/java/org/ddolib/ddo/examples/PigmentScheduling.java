package org.ddolib.ddo.examples;



import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.ddolib.ddo.io.InputReader;

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
 * 0 ≤ p < H where H is the time horizon.
 */
public class PigmentScheduling {

    public static final int IDLE = -1; // represent the idle state of the machine i.e. no production

    static class PSPState {
        int time; // The current time slot
        int next; //  The item that was produced at time t+1, -1 means that we don't know the item that is being produced next
        int [] previousDemands; // The time at which the previous demand for each item had been filled

        public PSPState(int time, int next, int[] previousDemands) {
            this.time = time;
            this.next = next;
            this.previousDemands = previousDemands;
        }

        @Override
        protected PSPState clone() {
            return new PSPState(time, next, Arrays.copyOf(previousDemands, previousDemands.length));
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
                prevDemands[i] = instance.previousDemands[i][instance.horizon]; // h-1 ?
            }
            return new PSPState(0, IDLE, prevDemands);
        }

        @Override
        public Iterator<Integer> domain(PSPState state, int var) {
            int t = var;
            IntStream items = IntStream.range(0, instance.nItems);
            IntStream dom = items.filter(i -> state.previousDemands[i] >= t);
            int remDemands = items.filter(i -> state.previousDemands[i] >= 0).map(i -> instance.remainingDemands[i][state.previousDemands[i]]).sum();

            if (remDemands > t + 1) {
                return Collections.emptyIterator();
            }
            if (remDemands < t + 1) {
                return IntStream.concat(dom, IntStream.of(IDLE)).iterator();
            } else {
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
                int t = decision.var();
                int duration = state.previousDemands[item] - t;
                int stocking = instance.stockingCost[item] * duration;
                int changeover = state.next != -1 ? instance.changeoverCost[state.next][item] : 0;
                // stocking cost + changeover cost
                return -(changeover + stocking);
            }
        }
    }

    public static class PSPRelax implements Relaxation<PSPState> {
        @Override
        public PSPState mergeStates(final Iterator<PSPState> states) {
            return null;
        }

        @Override
        public int relaxEdge(PSPState from, PSPState to, PSPState merged, Decision d, int cost) {
            return cost;
        }
    }

    static class PSPRanking implements StateRanking<PSPState> {
        @Override
        public int compare(PSPState s1, PSPState s2) {
            // the state with the smallest total demand is the best (not sure about this)
            int totS1 = Arrays.stream(s1.previousDemands).sum();
            int totS2 = Arrays.stream(s2.previousDemands).sum();
            return Integer.compare(totS1,totS2);
        }
    }

    static class PSPInstance {

        int nItems;
        int horizon;
        // dim = nItems
        int[] stockingCost; // cost of stocking item i

        // dim = nItems x nItems
        int[][] changeoverCost; // cost of changing from item i to item j

        // dim = nItems x horizon
        int[][] previousDemands; // previousDemands[i][t] = the time slot at which the previous demand for item i occurs

        // dim = nItems x horizon
        int[][] remainingDemands; // remainingDemands[i][t] = the total demand for item i from time 0 to t included

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

            previousDemands = new int[nItems][horizon + 1];
            for (int i = 0; i < nItems; i++) {
                Arrays.fill(previousDemands[i], -1);
            }
            for (int t = 1; t <= horizon; t++) {
                for (int i = 0; i < nItems; i++) {
                    if (demands[i][t-1] > 0) {
                        previousDemands[i][t] = t - 1;
                    } else {
                        previousDemands[i][t] = previousDemands[i][t-1];
                    }
                }
            }
            remainingDemands = new int[nItems][horizon];
            for (int i = 0; i < nItems; i++) {
                for (int t = 0; t < horizon; t++) {
                    if (t == 0) {
                        remainingDemands[i][t] = demands[i][t];
                    } else {
                        remainingDemands[i][t] = remainingDemands[i][t-1] + demands[i][t];
                    }
                }
            }

        }

    }

    public static void main(final String[] args) throws IOException {
        PSPInstance instance = new PSPInstance("data/PSP/instancesWith2items/1");;
        PSP problem = new PSP(instance);
        final PSPRelax relax = new PSPRelax();
        final PSPRanking ranking = new PSPRanking();
        final FixedWidth<PSPState> width = new FixedWidth<>(250);
        final VariableHeuristic<PSPState> varh = new DefaultVariableHeuristic();
        final Frontier<PSPState> frontier = new SimpleFrontier<>(ranking);

        final Solver solver = new ParallelSolver<PSPState>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] values = new int[problem.nbVars()];
                    for (Decision d : decisions) {
                        values[d.var()] = d.val();
                    }
                    return values;
                })
                .get();

        System.out.println(String.format("Duration : %.3f", duration));
        System.out.println(String.format("Objective: %d", solver.bestValue().get()));
        System.out.println(String.format("Solution : %s", Arrays.toString(solution)));
    }
}

