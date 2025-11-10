package org.ddolib.examples.setcover.setlayer;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.*;

public class SetCoverProblem implements Problem<SetCoverState> {
    final public int nElem;
    final public int nSet;
    final public List<Set<Integer>> sets;
    public int nbrElemRemoved;
    final public Optional<Double> optimal = Optional.empty();
    final public List<Double> weights;
    // Used to monitor how many times each type of branching is used
    public int countZeroOnly;
    public int countOneOnly;
    public int countZeroOne;
    final public List<Double> elemMinWeights; // for each elem, the minimal weight among the set containing the element


    public SetCoverProblem(int nElem, int nSet, List<Set<Integer>> sets, List<Double> weights) {
        this.nElem = nElem;
        this.nSet = nSet;
        this.sets = sets;
        this.weights = weights;
        this.nbrElemRemoved = nbrElemRemoved;
        this.elemMinWeights = new ArrayList<>(nElem);
        for (int i = 0; i < nElem; i++) {
            elemMinWeights.add(Double.MAX_VALUE);
        }
        for (int set = 0; set < nSet; set++) {
            for (Integer elem : sets.get(set)) {
                elemMinWeights.set(elem, Math.min(elemMinWeights.get(elem), weights.get(set)));
            }
        }
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
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
        return decision.val()*weights.get(decision.var());
    }
}
