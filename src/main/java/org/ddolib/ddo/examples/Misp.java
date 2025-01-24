package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;
import org.ddolib.ddo.core.Relaxation;
import org.ddolib.ddo.heuristics.StateRanking;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

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
                return List.of(0).iterator();
            } else {
                return List.of(1, 0).iterator();
            }
        }

        @Override
        public BitSet transition(BitSet state, Decision decision) {
            var res = (BitSet) state.clone();
            if (decision.val() == 1) {
                res.set(decision.var(), false);
                res.andNot(neighbors[decision.var()]);
            }

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

}
