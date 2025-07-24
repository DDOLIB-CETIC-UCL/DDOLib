package org.ddolib.examples.ddo.setcover.elementlayer;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;

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
                    // System.out.println("Next element: " + elem);
                    return elem;
                }
            }
            return null;
        }
    }

    public static final class MinCentralityDynamic implements VariableHeuristic<SetCoverState> {
        // private final PriorityQueue<Integer> pq;
        private final SetCoverProblem problem;
        // private final Integer[] ordering;
        private final Integer[] occurences;

        /**
         * This heuristic orders the elements are ordered following their centrality.
         * Elements with a small centrality are harder to cover and thus have a stronger priority.
         * @param problem
         */
        public MinCentralityDynamic(SetCoverProblem problem) {
            this.problem = problem;
            this.occurences = new Integer[problem.nElem];
        }

        @Override
        public Integer nextVariable(Set<Integer> variables, Iterator<SetCoverState> states) {
            for(int elem = 0; elem < problem.nElem; elem++) {
                occurences[elem] = 0;
            }
            while (states.hasNext()) {
                SetCoverState state = states.next();
                for (int elem: state.uncoveredElements) {
                    occurences[elem]++;
                }
            }

            int minOccurences = Integer.MAX_VALUE;
            int nextVariable = -1;
            for (int elem: variables) {
                if (occurences[elem]*this.problem.constraints.get(elem).size() < minOccurences) {
                    minOccurences = occurences[elem]*this.problem.constraints.get(elem).size();
                    nextVariable = elem;
                }
            }

            return nextVariable;
        }
    }


}
