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
import java.util.Collections.*;

public class SetCover {

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
    }

    public static class SetCoverProblem implements Problem<SetCoverState> {
        final int nElem;
        final int nSet;
        Set<Integer>[] sets;

        public SetCoverProblem(int nElem, int nSet, Set<Integer>[] sets) {
            this.nElem = nElem;
            this.nSet = nSet;
            this.sets = sets;
        }

        @Override
        public int nbVars() {
            return sets.length;
        }

        @Override
        public SetCoverState initialState() {
            Map<Integer, Integer> uncoveredElements = new HashMap<>();
            for(int i = 0; i < nElem; i++) {
                uncoveredElements.put(i, 0);
            }
            for (Set<Integer> set : sets) {
                for (Integer element : set) {
                    uncoveredElements.replace(element, uncoveredElements.get(element) + 1);
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
            // If the considered set is useless, it cannot be taken
            if (Collections.disjoint(state.uncoveredElements.keySet(), sets[var])) {
                return List.of(0).iterator();
            } else {
                // If the considered set is the last one that can cover a particular element,
                // it is forced to be selected
                for (Integer elem: state.uncoveredElements.keySet()) {
                    if (state.uncoveredElements.get(elem) == 1 && sets[var].contains(elem)) {
                        return List.of(1).iterator();
                    }
                }
                return Arrays.asList(1, 0).iterator();
            }
        }

        @Override
        public SetCoverState transition(SetCoverState state, Decision decision) {
            if (decision.val() == 1) {
                SetCoverState newState = state.clone();
                for (Integer element: sets[decision.var()]) {
                    newState.uncoveredElements.remove(element);
                }
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

    public static SetCoverProblem readInstance(final String fname) throws IOException {
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

            return new SetCoverProblem(context.nElem, context.nSet, context.sets);
        }
    }

    public static class SetCoverRanking implements StateRanking<SetCoverState> {
        @Override
        public int compare(final SetCoverState o1, final SetCoverState o2) {
            return o1.uncoveredElements.size() - o2.uncoveredElements.size();
        }
    }

    public static class SetCoverRelax implements Relaxation<SetCoverState> {

        @Override
        public SetCoverState mergeStates(Iterator<SetCoverState> states) {
            SetCoverState currState = states.next();
            SetCoverState newState = currState.clone();
            while (states.hasNext()) {
                currState = states.next();
                newState.uncoveredElements.keySet().retainAll(currState.uncoveredElements.keySet());
                if (newState.uncoveredElements.isEmpty()) break;
            }
            return newState;
        }

        @Override
        public int relaxEdge(SetCoverState from, SetCoverState to, SetCoverState merged, Decision d, int cost) {
            return cost;
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
        final String instance = "data/SetCover/tripode";
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
