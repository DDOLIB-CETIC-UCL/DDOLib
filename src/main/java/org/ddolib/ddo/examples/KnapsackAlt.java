package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.RelaxationSolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class KnapsackAlt {

    public static class KnapsackState {
        int capacity;
        int profit;

        public KnapsackState(int capacity, int profit) {
            this.capacity = capacity;
            this.profit = profit;
        }

        @Override
        protected KnapsackState clone() {
            return new KnapsackState(capacity, profit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(capacity);
        }

        @Override
        public boolean equals(Object o) {
            assert o instanceof KnapsackState;
            return ((KnapsackState) o).capacity == capacity;
        }

        @Override
        public String toString() {
            return "KnapsackState [capacity=" + capacity + ", cost=" + profit + "]";
        }

    }

    public static class KnapsackProblem implements Problem<KnapsackState> {

        final int capa;
        final int[] profit;
        final int[] weight;
        public final Integer optimal;

        public KnapsackProblem(final int capa, final int[] profit, final int[] weight, final Integer optimal) {
            this.capa = capa;
            this.profit = profit;
            this.weight = weight;
            this.optimal = optimal;
        }

        @Override
        public int nbVars() {
            return profit.length;
        }

        @Override
        public KnapsackState initialState() {
            return new KnapsackState(capa, 0);
        }

        @Override
        public int initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(KnapsackState state, int var) {
            if (state.capacity >= weight[var]) {
                return Arrays.asList(1, 0).iterator();
            } else {
                return List.of(0).iterator();
            }
        }

        @Override
        public KnapsackState transition(KnapsackState state, Decision decision) {
            if (decision.val() == 1) {
                return new KnapsackState(state.capacity - weight[decision.var()], profit[decision.var()]);
            } else {
                return state;
            }
        }

        @Override
        public int transitionCost(KnapsackState state, Decision decision) {
            return profit[decision.var()] * decision.val();
        }
    }

    public static class KnapsackRelax implements Relaxation<KnapsackState> {
        private final KnapsackProblem problem;

        public KnapsackRelax(KnapsackProblem problem) {
            this.problem = problem;
        }


        @Override
        public KnapsackState mergeStates(Iterator<KnapsackState> states) {
            int capa = 0;
            int profit = 0;
            while (states.hasNext()) {
                final KnapsackState state = states.next();
                capa = Math.max(capa, state.capacity);
                profit = Math.max(profit, state.profit);
            }
            return new KnapsackState(capa, profit);
        }

        @Override
        public int relaxEdge(KnapsackState from, KnapsackState to, KnapsackState merged, Decision d, int cost) {
            return cost;
        }

        @Override
        public int fastUpperBound(KnapsackState state, Set<Integer> variables) {
            double[] ratio = new double[problem.nbVars()];
            int capacity = state.capacity;
            for (int v : variables) {
                ratio[v] = ((double) capacity / problem.profit[v]);
            }

            class RatioComparator implements Comparator<Integer> {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return Double.compare(ratio[o1], ratio[o2]);
                }
            }

            Integer[] sorted = variables.toArray(new Integer[0]);
            Arrays.sort(sorted, new RatioComparator().reversed());

            int maxProfit = 0;
            Iterator<Integer> itemIterator = Arrays.stream(sorted).iterator();
            while (capacity > 0 && itemIterator.hasNext()) {
                int item = itemIterator.next();
                if (capacity >= problem.weight[item]) {
                    maxProfit += problem.profit[item];
                    capacity -= problem.weight[item];
                } else {
                    double itemProfit = ratio[item] * problem.weight[item];
                    maxProfit += (int) Math.floor(itemProfit);
                    capacity = 0;
                }
            }

            return maxProfit;
        }

    }

    public static class KnapsackDistance implements StateDistance<KnapsackState> {
        @Override
        public double distance(KnapsackState stateA, KnapsackState stateB) {
            return Math.sqrt(Math.pow(stateA.capacity - stateB.capacity, 2) + Math.pow(stateA.profit - stateB.profit, 2));
        }
    }

    public static class KnapsackCoordinates implements StateCoordinates<KnapsackState> {
        @Override
        public double[] getCoordinates(KnapsackState state) {
            return new double[] {state.capacity, state.profit};
        }
    }

    public static class KnapsackRanking implements StateRanking<KnapsackState> {
        @Override
        public int compare(final KnapsackState o1, final KnapsackState o2) {
            return o1.capacity - o2.capacity;
        }
    }

    public static KnapsackProblem readInstance(final String fname) throws IOException {
        final File f = new File(fname);
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            final PinReadContext context = new PinReadContext();

            bf.lines().forEachOrdered((String s) -> {
                if (context.isFirst) {
                    context.isFirst = false;
                    String[] tokens = s.split("\\s");
                    context.n = Integer.parseInt(tokens[0]);
                    context.capa = Integer.parseInt(tokens[1]);

                    if (tokens.length == 3) {
                        context.optimal = Integer.parseInt(tokens[2]);
                    }

                    context.profit = new int[context.n];
                    context.weight = new int[context.n];
                } else {
                    if (context.count < context.n) {
                        String[] tokens = s.split("\\s");
                        context.profit[context.count] = Integer.parseInt(tokens[0]);
                        context.weight[context.count] = Integer.parseInt(tokens[1]);

                        context.count++;
                    }
                }
            });

            return new KnapsackProblem(context.capa, context.profit, context.weight, context.optimal);
        }
    }

    private static class PinReadContext {
        boolean isFirst = true;
        int n = 0;
        int count = 0;
        int capa = 0;
        int[] profit = new int[0];
        int[] weight = new int[0];
        Integer optimal = null;
    }

    public static void main(final String[] args) throws IOException {
        final String instance = "data/Knapsack/Nafar_2024/KP_1.txt";
        final KnapsackProblem problem = readInstance(instance);
        final KnapsackRelax relax = new KnapsackRelax(problem);
        final KnapsackRanking ranking = new KnapsackRanking();
        final FixedWidth<KnapsackState> width = new FixedWidth<>(100);
        final VariableHeuristic<KnapsackState> varh = new DefaultVariableHeuristic<KnapsackState>();
        final StateDistance<KnapsackState> distance = new KnapsackDistance();
        final StateCoordinates<KnapsackState> coord = new KnapsackCoordinates();
        final int seed = 54646;
        final RelaxationType relaxationType = RelaxationType.GHP;

        final Frontier<KnapsackState> frontier = new SimpleFrontier<>(ranking);
        /*final Solver solver = new ParallelSolver<Integer>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);*/

        Solver solver = new RelaxationSolver<>(
                relaxationType,
                problem,
                relax,
                varh,
                ranking,
                distance,
                coord,
                width,
                frontier,
                seed
        );


        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;


        int[] solution = solver.bestSolution().map(decisions -> {
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            return values;
        }).get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }

}
