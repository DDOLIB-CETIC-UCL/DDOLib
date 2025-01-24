package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;

import java.util.*;

public final class Misp {

    public static class MispProblem implements Problem<BitSet> {

        final BitSet remainingNodes;
        final BitSet[] neighbors;
        final int[] weight;

        /**
         * @param remainingNodes The remaining node that can be selected in the current independent set. Considered
         *                       as the state of the MDD.
         * @param neighbors      For each node {@code i}, {@code neighbors[i]} returns the adjacency list of {@code i}.
         * @param weight         For each node {@code i}, {@code weight[i]} returns the weight associated to {@code i}
         *                       in the problem instance.
         */
        public MispProblem(BitSet remainingNodes, BitSet[] neighbors, int[] weight) {
            this.remainingNodes = remainingNodes;
            this.neighbors = neighbors;
            this.weight = weight;
        }

        @Override
        public String toString() {
            StringBuilder weighStr = new StringBuilder();
            for (int i = 0; i < weight.length; i++) {
                weighStr.append(String.format("\t%d : %d%n", i, weight[i]));
            }

            StringBuilder neighStr = new StringBuilder();
            for (int i = 0; i < neighbors.length; i++) {
                neighStr.append(String.format("\t%d : %s%n", i, neighbors[i]));
            }
            System.out.println(Arrays.toString(neighbors));

            return String.format("Remaining nodes: %s%nWeight: %n%s%nNeighbors: %n%s%n", remainingNodes.toString(),
                    weighStr, neighStr);
        }

        @Override
        public int nbVars() {
            return weight.length;
        }

        @Override
        public BitSet initialState() {
            return remainingNodes;
        }

        @Override
        public int initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(BitSet state, int var) {
            if (state.get(var)) {
                return List.of(0, 1).iterator();
            } else {
                return List.of(0).iterator();
            }
        }

        @Override
        public BitSet transition(BitSet state, Decision decision) {
            var res = (BitSet) state.clone();
            if (decision.val() == 1) res.andNot(neighbors[decision.var()]);

            return res;
        }

        @Override
        public int transitionCost(BitSet state, Decision decision) {
            return weight[decision.var()] * decision.val();
        }
    }

    public static class MispRelax implements Relaxation<BitSet> {

        private final MispProblem problem;

        public MispRelax(MispProblem problem) {
            this.problem = problem;
        }

        @Override
        public BitSet mergeStates(Iterator<BitSet> states) {
            var merged = new BitSet(problem.nbVars());
            while (states.hasNext()) {
                final BitSet state = states.next();
                merged.or(state);
            }
            return merged;
        }

        @Override
        public int relaxEdge(BitSet from, BitSet to, BitSet merged, Decision d, int cost) {
            return cost;
        }

    }

    public static class MispRanking implements StateRanking<BitSet> {

        @Override
        public int compare(BitSet o1, BitSet o2) {
            return Integer.compare(o1.length(), o2.length());
        }
    }

    public static MispProblem cycleGraph(int n) {
        BitSet state = new BitSet(n);
        state.set(0, n, true);
        int[] weight = new int[n];
        BitSet[] neighbor = new BitSet[n];
        for (int i = 0; i < n; i++) {
            weight[i] = 1;
            neighbor[i] = new BitSet(n);
        }

        for (int i = 0; i < n; i++) {
            if (i != 0) neighbor[i].set(i - 1);
            if (i != n - 1) neighbor[i].set(i + 1);
        }
        neighbor[0].set(n - 1);
        neighbor[n - 1].set(0);

        return new MispProblem(state, neighbor, weight);
    }

    public static void main(String[] args) {
        final MispProblem problem = cycleGraph(1000);
        final MispRelax relax = new MispRelax(problem);
        final MispRanking ranking = new MispRanking();
        final FixedWidth<BitSet> width = new FixedWidth<>(250);
        final VariableHeuristic<BitSet> varh = new DefaultVariableHeuristic<BitSet>();

        final Frontier<BitSet> frontier = new SimpleFrontier<>(ranking);

        final Solver solver = new ParallelSolver<BitSet>(
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

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }

}
