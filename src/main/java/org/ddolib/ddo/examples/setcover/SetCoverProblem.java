package org.ddolib.ddo.examples.setcover;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;

public class SetCoverProblem implements Problem<SetCoverState> {
    final public int nElem;
    final public int nSet;
    final public Set<Integer>[] sets;
    public int nbrElemRemoved;
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
