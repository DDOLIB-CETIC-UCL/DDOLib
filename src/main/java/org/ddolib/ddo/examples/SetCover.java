package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SetCover {

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
                for (int elem: problem.sets[set]) {
                    superposition += covered[elem];
                    if (superposition > maxSuperposition) {
                        maxSuperposition = superposition;
                        selected = set;
                    }
                }
            }

            for (int set: problem.sets[selected]) {
                covered[set]++;
            }
            System.out.println("Selected set: " + problem.sets[selected]);

            return selected;
        }
    }

    public static final class MaxCentralityHeuristic implements VariableHeuristic<SetCover.SetCoverState> {
        private final SetCoverProblem problem;
        private final int[] centralitiesSum; // contains, for each set, the sum of the centrality of each covered elem

        public MaxCentralityHeuristic(SetCoverProblem problem) {
            this.problem = problem;
            centralitiesSum = new int[problem.nSet];

            // Compute the centrality of each element
            int[] centrality = new int[problem.nElem];
            for (int set = 0; set < problem.nSet; set++) {
                for (int elem: problem.sets[set]) {
                    centrality[elem]++;
                }
            }

            // Compute the sums of the centralities
            for (int set = 0; set < problem.nSet; set++) {
                for (int elem: problem.sets[set]) {
                    centralitiesSum[set] += centrality[elem];
                }
            }
        }

        @Override
        public Integer nextVariable(Set<Integer> variables, Iterator<SetCover.SetCoverState> states) {
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

    public static final class MinBandwithHeuristic implements VariableHeuristic<SetCover.SetCoverState> {

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
        public Integer nextVariable(Set<Integer> variables, Iterator<SetCover.SetCoverState> states) {
            nbCall++;
            if (lastSelected == -1) {
                int maxSize = -1;
                for (Integer var: variables) {
                    if (problem.sets[var].size() > maxSize) {
                        maxSize = problem.sets[var].size();
                        lastSelected = var;
                    }
                }
                // lastSelected = variables.iterator().next();
                for (int elem: problem.sets[lastSelected]) {
                    firstTimeEncountered[elem] = nbCall;
                    lastTimeEncountered[elem] = nbCall;
                }
                return lastSelected;
            }
            int maxSimilarity = -1;
            int selected = -1;
            // selected = variables.iterator().next();
            for (Integer set: variables) {
                int similarity = intersectionSize(problem.sets[lastSelected], problem.sets[set]);
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    selected = set;
                }
            }
            lastSelected = selected;
            System.out.println("Selected set: " + selected);
            System.out.println(problem.sets[selected]);
            System.out.println("Similarity: " + maxSimilarity);

            for (int elem: problem.sets[selected]) {
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

    public static class SetCoverState {
        // Set<Integer> uncoveredElements;
        Map<Integer, Integer> uncoveredElements;

        public SetCoverState(Map<Integer, Integer> uncoveredElements) {
            this.uncoveredElements = uncoveredElements;
        }

        @Override
        protected SetCoverState clone() {
            return new SetCoverState(new HashMap<>(uncoveredElements));
        }

        @Override
        public String toString() {
            return uncoveredElements.keySet().toString();
        }

        @Override
        public boolean equals(Object o) {
            assert o instanceof SetCoverState;
            return uncoveredElements.keySet().equals(((SetCoverState) o).uncoveredElements.keySet());
        }

        public int size() {
            return uncoveredElements.size();
        }
    }

    public static class SetCoverProblem implements Problem<SetCoverState> {
        final int nElem;
        final int nSet;
        final Set<Integer>[] sets;
        int nbrElemRemoved;
        public int countZeroOnly;
        public int countOneOnly;
        public int countZeroOne;

        public SetCoverProblem(int nElem, int nSet, Set<Integer>[] sets) {
            this(nElem, nSet, sets, 0);
        }

        public SetCoverProblem(int nElem, int nSet, Set<Integer>[] sets, int nbrElemRemoved) {
            this.nElem = nElem;
            this.nSet = nSet;
            this.sets = sets;
            this.nbrElemRemoved = nbrElemRemoved;

        }

        public void setNbrElemRemoved(int nbrElemRemoved) {
            this.nbrElemRemoved = nbrElemRemoved;
        }

        @Override
        public int nbVars() {
            return sets.length;
        }

        private Set<Integer> getMostCoveredElements(int nbrElemToQuery) {
            int[] coverage = new int[nElem];
            for (Set<Integer> set: sets) {
                for (Integer elem: set) {
                    coverage[elem]++;
                }
            }

            PriorityQueue<Integer> pq = new PriorityQueue<>((o1, o2) -> Integer.compare(coverage[o2], coverage[o1]));
            for (int elem = 0; elem < nElem; elem++) {
                pq.add(elem);
            }
            Set<Integer> mostCoveredElements = new HashSet<>();
            for (int i = 0; i < nbrElemRemoved; i++) {
                mostCoveredElements.add(pq.poll());
            }
            return mostCoveredElements;
        }

        @Override
        public SetCoverState initialState() {
            Set<Integer> unkeptElements = getMostCoveredElements(this.nbrElemRemoved);
            Map<Integer, Integer> uncoveredElements = new HashMap<>();
            countZeroOnly = 0;
            countOneOnly = 0;
            countZeroOne = 0;
            for(int i = 0; i < nElem; i++) {
                if (!unkeptElements.contains(i)) {
                    uncoveredElements.put(i, 0);
                }
            }
            for (Set<Integer> set : sets) {
                // System.out.println(set);
                for (Integer element : set) {
                    if (!unkeptElements.contains(element)) {
                        uncoveredElements.replace(element, uncoveredElements.get(element) + 1);
                    }
                }
            }
            return new SetCoverState(uncoveredElements);
        }

        @Override
        public int initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(SetCoverState state, int var) {
            // System.out.println("var: " + sets[var]);
            // If the considered set is useless, it cannot be taken
            if (Collections.disjoint(state.uncoveredElements.keySet(), sets[var])) {
                countZeroOnly++;
                return List.of(0).iterator();
            } else {
                // If the considered set is the last one that can cover a particular element,
                // it is forced to be selected
                for (Integer elem: state.uncoveredElements.keySet()) {
                    if (state.uncoveredElements.get(elem) == 1 && sets[var].contains(elem)) {
                        countOneOnly++;
                        return List.of(1).iterator();
                    }
                }
                countZeroOne++;
                return Arrays.asList(1, 0).iterator();
            }
        }

        @Override
        public SetCoverState transition(SetCoverState state, Decision decision) {
            if (decision.val() == 1) {
                /*System.out.println("Computing transition");
                System.out.println("Initial State: " + state);
                System.out.println("Set: " + sets[decision.var()]);*/
                SetCoverState newState = state.clone();
                for (Integer element: sets[decision.var()]) {
                    newState.uncoveredElements.remove(element);
                }
                // System.out.println("New State: " + newState);
                return newState;
            } else {
                SetCoverState newState = state.clone();
                for (Integer element: sets[decision.var()]) {
                    if (newState.uncoveredElements.containsKey(element)) {
                        newState.uncoveredElements.replace(element, newState.uncoveredElements.get(element) - 1);
                    }
                }
                return newState;
            }
        }

        @Override
        public int transitionCost(SetCoverState state, Decision decision) {
            return -decision.val();
        }
    }

    public static class SetCoverRanking implements StateRanking<SetCoverState> {

        @Override
        public int compare(final SetCoverState o1, final SetCoverState o2) {
            return Integer.compare(o1.size(), o2.size());
        }
    }

    public static class SetCoverRelax implements Relaxation<SetCoverState> {

        @Override
        public SetCoverState mergeStates(Iterator<SetCoverState> states) {
            // System.out.println("**************Merging**************");
            SetCoverState currState = states.next();
            // System.out.println(currState);
            SetCoverState newState = currState.clone();
            int nbrMerged = 1;
            List<Integer> statesSizes = new ArrayList<>();
            statesSizes.add(currState.uncoveredElements.size());
            while (states.hasNext()) {
                currState = states.next();
                // System.out.println(currState);
                newState.uncoveredElements.keySet().retainAll(currState.uncoveredElements.keySet());
                // statesSizes.add(currState.uncoveredElements.size());
                nbrMerged++;
                // if (newState.uncoveredElements.isEmpty()) break;
            }
            /*System.out.println("New state: " + newState.uncoveredElements.size());
            System.out.printf("Merged %d states%n", nbrMerged);
            statesSizes.sort(Integer::compareTo);
            System.out.println("Min size: "+ statesSizes.getFirst());
            System.out.println("Median size: " + statesSizes.get(statesSizes.size()/2));*/
            return newState;
        }

        @Override
        public int relaxEdge(SetCoverState from, SetCoverState to, SetCoverState merged, Decision d, int cost) {
            return cost;
        }

    }

    public static SetCoverProblem readInstance(final String fname) throws IOException {
        return readInstance(fname, 0);
    }

    public static SetCoverProblem readInstance(final String fname, int nbrElemRemoved) throws IOException {
        final File f = new File(fname);
        try (final BufferedReader br = new BufferedReader(new FileReader(f))) {
            final PinReadContext context = new PinReadContext();

            br.lines().forEachOrdered((String s) -> {
                if (context.isFirst) {
                    context.isFirst = false;

                    String[] tokens = s.split("\\s");
                    context.nElem = Integer.parseInt(tokens[0]);
                    context.nSet = Integer.parseInt(tokens[1]);

                    context.sets = new Set[context.nSet];
                } else {
                    if (context.count< context.nSet) {
                        String[] tokens = s.split("\\s");
                        context.sets[context.count] = new HashSet<>(tokens.length);
                        for (String token : tokens) {
                            context.sets[context.count].add(Integer.parseInt(token));
                        }
                        context.count++;
                    }
                }
            });

            nbrElemRemoved = Math.min(context.nElem,  nbrElemRemoved);

            return new SetCoverProblem(context.nElem, context.nSet, context.sets, nbrElemRemoved);
        }
    }

    private static class PinReadContext {
        boolean isFirst = true;
        int nElem = 0;
        int nSet = 0;
        Set<Integer>[] sets = new Set[0];
        int count = 0;
    }

    public static void main(String[] args) throws IOException {
        final String instance = "data/SetCover/1id_problem/tripode";
        final SetCoverProblem problem = readInstance(instance);
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(1000000);
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;


        int[] solution = solver.bestSolution().map(decisions -> {
            System.out.println("Solution Found");
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
