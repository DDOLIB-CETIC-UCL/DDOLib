package org.ddolib.examples.ddo.setcover.elementlayer;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;


import java.util.*;

public class SetCoverProblem implements Problem<SetCoverState> {
    final public int nElem;
    final public int nSet;
    final public List<Set<Integer>> constraints; // for each element, the collection of sets that contain this element


    public SetCoverProblem(int nElem, int nSet, List<Set<Integer>> constraints) {
        this.nElem = nElem;
        this.nSet = nSet;
        this.constraints = constraints;
    }

    @Override
    public int nbVars() {
        return nElem;
    }


    /**
     * The initial state contains all element in the universe
     * @return the initial state
     */
    @Override
    public SetCoverState initialState() {
        Set<Integer> uncoveredElements = new HashSet<>();
        for (int i = 0; i < nElem; i++) {
            uncoveredElements.add(i);
        }
        return new SetCoverState(uncoveredElements);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    /**
     * If the considered element in var is uncovered in the given state,
     * then the domain contains all the sets that covers this element.
     * Else, no set is added to the solution
     * @param state the state from which the transitions should be applicable
     * @param var the variable whose domain in being queried
     * @return
     */
    @Override
    public Iterator<Integer> domain(SetCoverState state, int var) {
        if (state.uncoveredElements.contains(var)) { // the element is uncovered
            return constraints.get(var).iterator();
        } else { // the element is already covered
            return List.of(-1).iterator();
        }
    }

    /**
     * If a set has been added to the solution, then all the elements
     * that it contains are removed from the uncovered elements.
     * Else the state is unchanged.
     * @param state the state from which the transition originates
     * @param decision the decision which is applied to `state`.
     * @return
     */
    @Override
    public SetCoverState transition(SetCoverState state, Decision decision) {
        if (decision.val() != -1) { // a set is added to the solution
            SetCoverState newState = state.clone();
            for (int element: state.uncoveredElements) {
                if (constraints.get(element).contains(decision.val())) {
                    newState.uncoveredElements.remove(element);
                }
            }
            return newState;
        } else { // no set is added to the solution
            return state.clone();
        }
    }

    @Override
    public double transitionCost(SetCoverState state, Decision decision) {
        if (decision.val() != -1) { // a set is added to the solution
            return -1;
        } else { // no set is added to the solution
            return 0;
        }
    }

    /**
     * @param index the index of the required set
     * @return a Set containing the elements covered by the required set
     */
    public Set<Integer> getSetDefinition(int index) {
        Set<Integer> definition = new HashSet<>();
        for (int elem = 0; elem < nElem; elem++) {
            if (constraints.get(elem).contains(index)) {
                definition.add(elem);
            }
        }

        return definition;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("nElem: ").append(nElem).append("\n");
        builder.append("nSet: ").append(nSet).append("\n");
        builder.append("constraints: ").append(constraints).append("\n");
        return builder.toString();
    }
}
