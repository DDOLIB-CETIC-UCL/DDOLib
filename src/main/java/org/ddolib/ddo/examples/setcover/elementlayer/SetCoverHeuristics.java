package org.ddolib.ddo.examples.setcover.elementlayer;

import org.ddolib.ddo.heuristics.VariableHeuristic;

import java.util.*;

public class SetCoverHeuristics {

    public static final class MinCentrality implements VariableHeuristic<SetCoverState> {
        private final PriorityQueue<Integer> pq;

        /**
         * This heuristic orders the elements are ordered following their centrality.
         * Elements with a small centrality are harder to cover and thus have a stronger priority.
         * @param problem
         */
        public MinCentrality(SetCoverProblem problem) {
            pq = new PriorityQueue<>(Comparator.comparingInt(x -> problem.constraints.get(x).size()));
            for (int elem = 0; elem < problem.constraints.size(); elem++) {
                pq.add(elem);
            }
        }

        @Override
        public Integer nextVariable(Set<Integer> variables, Iterator<SetCoverState> states) {
            return pq.poll();
        }
    }


}
