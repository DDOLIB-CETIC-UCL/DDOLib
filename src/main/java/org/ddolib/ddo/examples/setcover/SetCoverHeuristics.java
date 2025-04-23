package org.ddolib.ddo.examples.setcover;

import org.ddolib.ddo.heuristics.VariableHeuristic;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class SetCoverHeuristics {

    public static final class MostDifferent implements VariableHeuristic<SetCoverState> {
        private final SetCoverProblem problem;
        private int lastSelected = -1;

        public MostDifferent(SetCoverProblem problem) {
            this.problem = problem;
        }

        @Override
        public Integer nextVariable(Set<Integer> variables, Iterator<SetCoverState> states) {
            int selected = -1;
            if (lastSelected == -1) {
                for (int i: variables) {
                    if (selected == -1 || problem.sets.get(i).size() > problem.sets.get(selected).size())
                        selected = i;
                }
            } else {

            }

            return selected;
        }

    }

    public static final class MostCovered implements VariableHeuristic<SetCoverState> {
        private final SetCoverProblem problem;
        private final int[] covered;
        private final int[] firstTimeEncountered;
        private final int[] lastTimeEncountered;

        public MostCovered(SetCoverProblem problem) {
            this.problem = problem;
            covered = new int[problem.nElem];
            this.firstTimeEncountered = new int[problem.nElem];
            this.lastTimeEncountered = new int[problem.nElem];
            for (int i = 0; i < problem.nElem; i++) {
                firstTimeEncountered[i] = -1;
                lastTimeEncountered[i] = -1;
            }
        }


        @Override
        public Integer nextVariable(Set<Integer> variables, Iterator<SetCoverState> states) {
            // int depth = problem.nSet - variables.size();
            int selected = -1;
            int maxSuperposition = -1;
            for (int set: variables) {
                int superposition = 0;
                for (int elem: problem.sets.get(set)) {
                    superposition += covered[elem];
                    if (superposition > maxSuperposition) {
                        maxSuperposition = superposition;
                        selected = set;
                    }
                }
            }

            for (int set: problem.sets.get(selected)) {
                covered[set]++;
            }
            System.out.println("Selected set: " + problem.sets.get(selected));

            return selected;
        }
    }

    public static final class MaxCentralityHeuristic implements VariableHeuristic<SetCoverState> {
        private final SetCoverProblem problem;
        private final int[] centralitiesSum; // contains, for each set, the sum of the centrality of each covered elem

        public MaxCentralityHeuristic(SetCoverProblem problem) {
            this.problem = problem;
            centralitiesSum = new int[problem.nSet];

            // Compute the centrality of each element
            int[] centrality = new int[problem.nElem];
            for (int set = 0; set < problem.nSet; set++) {
                for (int elem: problem.sets.get(set)) {
                    centrality[elem]++;
                }
            }

            // Compute the sums of the centralities
            for (int set = 0; set < problem.nSet; set++) {
                for (int elem: problem.sets.get(set)) {
                    centralitiesSum[set] += centrality[elem];
                }
            }
        }

        @Override
        public Integer nextVariable(Set<Integer> variables, Iterator<SetCoverState> states) {
            int maxCentrality = -1;
            int selection = -1;
            for (int set:variables) {
                if (centralitiesSum[set] > maxCentrality) {
                    maxCentrality = centralitiesSum[set];
                    selection = set;
                }
            }
            return selection;
        }
    }

    public static final class MinBandwithHeuristic implements VariableHeuristic<SetCoverState> {

        private final SetCoverProblem problem;
        private final int[] firstTimeEncountered;
        private final int[] lastTimeEncountered;
        private int nbCall;

        public MinBandwithHeuristic(SetCoverProblem problem) {
            this.problem = problem;
            this.firstTimeEncountered = new int[problem.nElem];
            this.lastTimeEncountered = new int[problem.nElem];
            for (int i = 0; i < problem.nElem; i++) {
                firstTimeEncountered[i] = -1;
                lastTimeEncountered[i] = -1;
            }
            nbCall = 0;
        }

        int lastSelected = -1;

        private int intersectionSize(Set<Integer> a, Set<Integer> b) {
            Set<Integer> smaller;
            Set<Integer> larger;
            if (a.size() < b.size()) {
                smaller = a;
                larger = b;
            } else {
                smaller = b;
                larger = a;
            }

            int intersectionSize = 0;
            for (Integer elem: smaller) {
                if (larger.contains(elem)) intersectionSize++;
            }

            return intersectionSize;
        }

        @Override
        public Integer nextVariable(Set<Integer> variables, Iterator<SetCoverState> states) {
            nbCall++;
            if (lastSelected == -1) {
                int maxSize = -1;
                for (Integer var: variables) {
                    if (problem.sets.get(var).size() > maxSize) {
                        maxSize = problem.sets.get(var).size();
                        lastSelected = var;
                    }
                }
                // lastSelected = variables.iterator().next();
                for (int elem: problem.sets.get(lastSelected)) {
                    firstTimeEncountered[elem] = nbCall;
                    lastTimeEncountered[elem] = nbCall;
                }
                return lastSelected;
            }
            int maxSimilarity = -1;
            int selected = -1;
            // selected = variables.iterator().next();
            for (Integer set: variables) {
                int similarity = intersectionSize(problem.sets.get(lastSelected), problem.sets.get(set));
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    selected = set;
                }
            }
            lastSelected = selected;
            System.out.println("Selected set: " + selected);
            System.out.println(problem.sets.get(selected));
            System.out.println("Similarity: " + maxSimilarity);

            for (int elem: problem.sets.get(selected)) {
                if (firstTimeEncountered[elem] == -1) firstTimeEncountered[elem] = nbCall;
                lastTimeEncountered[elem] = nbCall;
            }

            System.out.print("Bandwith: ");
            for (int i = 0; i < problem.nElem; i++) {
                System.out.printf("%d:%d, ", i, lastTimeEncountered[i] - firstTimeEncountered[i]);
            }

            int[] bandwiths = new int[problem.nElem];
            for (int i = 0; i < problem.nElem; i++) {
                bandwiths[i] = lastTimeEncountered[i] - firstTimeEncountered[i];
            }
            Arrays.sort(bandwiths);
            System.out.println();
            System.out.println("Avg: " + Arrays.stream(bandwiths).average().getAsDouble());
            System.out.println("Min: " + bandwiths[0]);
            System.out.println("Max: " + bandwiths[bandwiths.length - 1]);
            System.out.println("Median: " + bandwiths[bandwiths.length / 2]);

            return selected;
        }
    }


}
