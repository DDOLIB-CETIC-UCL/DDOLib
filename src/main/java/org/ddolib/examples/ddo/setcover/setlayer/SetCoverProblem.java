package org.ddolib.examples.ddo.setcover.setlayer;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.*;

public class SetCoverProblem implements Problem<SetCoverState> {
    final public int nElem;
    final public int nSet;
    final public List<Set<Integer>> sets;
    public int nbrElemRemoved;

    // Used to monitor how many times each type of branching is used
    public int countZeroOnly;
    public int countOneOnly;
    public int countZeroOne;

    public SetCoverProblem(int nElem, int nSet, List<Set<Integer>> sets) {
        this(nElem, nSet, sets, 0);
    }

    public SetCoverProblem(int nElem, int nSet, List<Set<Integer>> sets, int nbrElemRemoved) {
        this.nElem = nElem;
        this.nSet = nSet;
        this.sets = sets;
        this.nbrElemRemoved = nbrElemRemoved;

    }

    @Override
    public double optimal() {
        return 0.0;
    }

    /**
     *
     * @param nbrElemRemoved
     */
    public void setNbrElemRemoved(int nbrElemRemoved) {
        this.nbrElemRemoved = nbrElemRemoved;
    }

    @Override
    public int nbVars() {
        return sets.size();
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
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(SetCoverState state, int var) {
        // System.out.println("var: " + sets[var]);
        // If the considered set is useless, it cannot be taken

        if (Collections.disjoint(state.uncoveredElements.keySet(), sets.get(var))) {
            countZeroOnly++;
            return List.of(0).iterator();
        } else {
            // If the considered set is the last one that can cover a particular element,
            // it is forced to be selected
            for (Integer elem: state.uncoveredElements.keySet()) {
                if (state.uncoveredElements.get(elem) == 1 && sets.get(var).contains(elem)) {
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
            for (Integer element: sets.get(decision.var())) {
                newState.uncoveredElements.remove(element);
            }
            // System.out.println("New State: " + newState);
            return newState;
        } else {
            SetCoverState newState = state.clone();
            for (Integer element: sets.get(decision.var())) {
                if (newState.uncoveredElements.containsKey(element)) {
                    newState.uncoveredElements.replace(element, newState.uncoveredElements.get(element) - 1);
                }
            }
            return newState;
        }
    }

    @Override
    public double transitionCost(SetCoverState state, Decision decision) {
        return -decision.val();
    }
}
