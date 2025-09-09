package org.ddolib.modeling;

import org.ddolib.ddo.core.Decision;

import java.util.Iterator;
import java.util.Optional;

/**
 * This is the definition of the problem one tries to optimize. It basically
 * consists of a problem's formulation in terms of the labeled transition
 * system semantics of a dynamic programme.
 *
 * @param <T> the type of state
 */
public interface Problem<T> {
    /**
     * @return the number of variables in the problem
     */
    int nbVars();

    /**
     * @return the intial state of the problem
     */
    T initialState();

    /**
     * @return the problem's initial value
     */
    double initialValue();

    /**
     * @param state the state from which the transitions should be applicable
     * @param var   the variable whose domain in being queried
     * @return all values in the domain of `var` if a decision is made about the given variable
     */
    Iterator<Integer> domain(final T state, final int var);

    /**
     * Applies the problem transition function from one state to the next
     * going through a given decision. (Computes the next state)
     *
     * @param state    the state from which the transition originates
     * @param decision the decision which is applied to `state`.
     */
    T transition(final T state, final Decision decision);

    /**
     * Computes the impact on the objective value of making the given
     * decision in the specified state.
     *
     * @param state    the state from which the transition originates.
     * @param decision the decision which is applied to `state`.
     */
    double transitionCost(final T state, final Decision decision);

    /**
     * Returns the expected optimal value if known.
     * <p>
     * <b>WARNING:</b> It must return the expected value returned by the solver. Be cautious if you are working with a
     * minimization problem and negative number.
     *
     * @return the expected optimal value if known.
     */
    default Optional<Double> optimalValue() {
        return Optional.empty();
    }

    default void setLB(){};
}
