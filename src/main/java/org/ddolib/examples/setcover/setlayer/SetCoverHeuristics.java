package org.ddolib.examples.setcover.setlayer;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;

import java.util.*;

public class SetCoverHeuristics {

    /**
     * In this heuristic, first element are sorted such as elements with small centrality
     * have a stronger priority to be covered.
     * Then the set are ordered such as the one covering element with small centralities are considered first,
     * but also such as sets that covers elements that were in the previous sets are considered first.
     * The idea is to quickly "close" elements, i.e. quickly reach sets that are the last one to cover an element
     */
    public static final class FocusClosingElements implements VariableHeuristic<SetCoverState> {
        private final SetCoverProblem problem;
        private List<Integer> ordering;
        // private Iterator<Integer> orderingIterator;

        private List<Set<Integer>> computeSymptoms() {
            List<Set<Integer>> symptoms = new ArrayList<>(problem.nElem);
            for (int i = 0; i < problem.nElem; i++) {
                symptoms.add(new HashSet<>());
            }

            for (int set = 0; set < problem.nSet; set++) {
                for (int elem: problem.sets.get(set)) {
                    symptoms.get(elem).add(set);
                }
            }
            return symptoms;
        }

        public FocusClosingElements(SetCoverProblem problem) {
            this.problem = problem;

            List<Set<Integer>> symptoms = computeSymptoms();

            ordering = new ArrayList<>(problem.nSet);
            int[] centralities = new int[problem.nElem];
            boolean[] isSetAdded = new boolean[problem.nSet];
            int priorityElement = 0;
            centralities[0] = symptoms.get(0).size();
            for (int elem = 1; elem < problem.nElem; elem++) {
                centralities[elem] = symptoms.get(elem).size();
                if (symptoms.get(elem).size() < symptoms.get(priorityElement).size()) {
                    priorityElement = elem;
                }
            }

            while (ordering.size() < problem.nSet && priorityElement != -1) {
                for (int set: symptoms.get(priorityElement)) {
                    if (isSetAdded[set]) {
                        continue;
                    }
                    ordering.add(set);
                    isSetAdded[set] = true;

                    for (int elem: problem.sets.get(set)) {
                        centralities[elem]--;
                    }
                }

                priorityElement = -1;
                int minCentrality = Integer.MAX_VALUE;
                for (int elem = 0; elem < problem.nElem; elem++) {
                    if (centralities[elem] < minCentrality && centralities[elem] > 0) {
                        priorityElement = elem;
                        minCentrality = centralities[elem];
                    }
                }
            }
            // orderingIterator = ordering.iterator();
        }

        @Override
        public Integer nextVariable(Set<Integer> variables, Iterator<SetCoverState> states) {
            for (int set: ordering) {
                if (variables.contains(set)) {
                    return set;
                }
            }
            return null;
        }
    }

    /**
     * Heuristic where the next set is selected is the one that reduces the most the smallestState
     */
    public static final class FocusMostSmallState implements VariableHeuristic<SetCoverState> {
        private final SetCoverProblem problem;

        public FocusMostSmallState(SetCoverProblem problem) {
            this.problem = problem;
        }

        @Override
        public Integer nextVariable(Set<Integer> variables, Iterator<SetCoverState> states) {

            // Select the smallest state
            SetCoverState consideredState = states.next();
            while (states.hasNext()) {
                SetCoverState nextState = states.next();
                if (nextState.uncoveredElements.size() < consideredState.uncoveredElements.size() && !nextState.uncoveredElements.isEmpty()) {
                    consideredState = nextState;
                }
            }

            // Select the
            int maxIntersection = -1;
            int selectedVar = -1;
            for (Integer i : variables) {
                int intersection = consideredState.intersectionSize(problem.sets.get(i));
                if (intersection > maxIntersection) {
                    maxIntersection = intersection;
                    selectedVar = i;
                }
            }
            // System.out.println(selectedVar);

            return selectedVar;
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
            // System.out.println("Selected set: " + problem.sets.get(selected));

            return selected;
        }
    }

    // TODO : test MinCentralityHeuristic
    public static final class MinCentralityHeuristic implements VariableHeuristic<SetCoverState> {
        private final SetCoverProblem problem;
        private final int[] centralitiesSum; // contains, for each set, the sum of the centrality of each covered elem

        public MinCentralityHeuristic(SetCoverProblem problem) {
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
            int minCentrality = Integer.MAX_VALUE;
            int selection = -1;
            for (int set:variables) {
                if (centralitiesSum[set] < minCentrality) {
                    minCentrality = centralitiesSum[set];
                    selection = set;
                }
            }
            // System.out.println(selection);
            return selection;
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

            System.out.print("Bandwidth: ");
            for (int i = 0; i < problem.nElem; i++) {
                System.out.printf("%d:%d, ", i, lastTimeEncountered[i] - firstTimeEncountered[i]);
            }

            int[] bandwidths = new int[problem.nElem];
            for (int i = 0; i < problem.nElem; i++) {
                bandwidths[i] = lastTimeEncountered[i] - firstTimeEncountered[i];
            }
            Arrays.sort(bandwidths);
            System.out.println();
            System.out.println("Avg: " + Arrays.stream(bandwidths).average().getAsDouble());
            System.out.println("Min: " + bandwidths[0]);
            System.out.println("Max: " + bandwidths[bandwidths.length - 1]);
            System.out.println("Median: " + bandwidths[bandwidths.length / 2]);

            return selected;
        }
    }


}
