package org.ddolib.ddo.examples.setcover.elementlayer;

import org.ddolib.ddo.heuristics.VariableHeuristic;

import java.util.*;

public class SetCoverHeuristics {

    public static final class MinCentrality implements VariableHeuristic<SetCoverState> {
        // private final PriorityQueue<Integer> pq;
        private final SetCoverProblem problem;
        private final Integer[] ordering;

        /**
         * This heuristic orders the elements are ordered following their centrality.
         * Elements with a small centrality are harder to cover and thus have a stronger priority.
         * @param problem
         */
        public MinCentrality(SetCoverProblem problem) {
            this.problem = problem;
            ordering = new Integer[problem.nElem];
            for (int i = 0; i < problem.nElem; i++) {
                ordering[i] = i;
            }
            Arrays.sort(ordering, Comparator.comparingInt(x -> this.problem.constraints.get(x).size()));
        }

        @Override
        public Integer nextVariable(Set<Integer> variables, Iterator<SetCoverState> states) {
            for (int elem: ordering) {
                if (variables.contains(elem)) {
                    System.out.println("Next element: " + elem);
                    return elem;
                }
            }
            return null;
        }
    }


}
