package org.ddolib.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static java.lang.Integer.*;
import static java.lang.Math.abs;

/**
 * Implements a <b>relaxation</b> for the Maximum 2-Satisfiability (MAX2SAT) problem.
 * <p>
 * This class provides a mechanism to merge multiple {@link Max2SatState} instances into a single
 * relaxed state, which can be used in relaxation-based search algorithms such as DDO or A*.
 * </p>
 *
 * <p>
 * The merge strategy aims to be optimistic without overestimating: for each variable, if all states
 * have the same sign of net benefit, the smallest magnitude is kept; otherwise, the net benefit is set to 0.
 * This approach ensures that the merged state does not overestimate the achievable benefit, providing a
 * valid lower bound for maximization.
 * </p>
 *
 * <p>
 * The {@link #relaxEdge(Max2SatState, Max2SatState, Max2SatState, Decision, double)} method adjusts the
 * transition cost to account for losses of net benefit in the merged state, ensuring an over-approximation
 * of the optimal solution.
 * </p>
 *
 * @see Max2SatProblem
 * @see Max2SatState
 * @see Relaxation
 */
public class Max2SatRelax implements Relaxation<Max2SatState> {

    /** The MAX2SAT problem instance being relaxed. */
    private final Max2SatProblem problem;

    /**
     * Constructs a relaxation for the given MAX2SAT problem.
     *
     * @param problem the MAX2SAT problem instance
     */
    public Max2SatRelax(Max2SatProblem problem) {
        this.problem = problem;
    }
    /**
     * Merges multiple {@link Max2SatState} instances into a single relaxed state.
     * <p>
     * For each variable, if all the net benefits in the states have the same sign, the smallest
     * magnitude is kept; otherwise, the net benefit is set to 0. The depth of the merged state
     * corresponds to the depth of the last processed state.
     * </p>
     *
     * @param states an iterator over the states to merge
     * @return a new {@link Max2SatState} representing the merged relaxed state
     */
    @Override
    public Max2SatState mergeStates(Iterator<Max2SatState> states) {
        //Being to optimistic can lead to bad decision. For the merged state we keep the net
        // benefit near to 0.
        ArrayList<Integer> merged = new ArrayList<>(Collections.nCopies(problem.nbVars(), 0));
        int depth = problem.nbVars();
        while (states.hasNext()) {
            Max2SatState current = states.next();
            depth = current.depth();
            for (int i = 0; i < current.netBenefit().size(); i++) {
                Integer mergedI = merged.get(i);
                Integer currentI = current.netBenefit().get(i);
                // If all the net benefits have the same sign, we keep the smallest one in absolute value.
                if (signum(mergedI) == 1 && signum(currentI) == 1) {
                    merged.set(i, min(mergedI, currentI));
                } else if (signum(mergedI) == -1 && signum(currentI) == -1) {
                    merged.set(i, max(mergedI, currentI));
                } else {
                    // Otherwise, we set the benefit to 0.
                    merged.set(i, 0);
                }
            }
        }
        return new Max2SatState(merged, depth);
    }
    /**
     * Adjusts the cost of transitioning from one state to another in the relaxed model.
     * <p>
     * The method compensates for potential losses in net benefits when using a merged state,
     * ensuring that the transition cost maintains an over-approximation of the optimal solution.
     * </p>
     *
     * @param from   the original state before the transition
     * @param to     the original state after the transition
     * @param merged the merged relaxed state
     * @param d      the decision taken
     * @param cost   the original transition cost
     * @return the adjusted transition cost in the relaxed model
     */
    @Override
    public double relaxEdge(Max2SatState from, Max2SatState to, Max2SatState merged, Decision d,
                            double cost) {
        // The net benefits in merged state are smaller than the net benefit in exact states.
        // To offset the losses of benefit and guarantee an over-approximation of the optimal
        // solution, we add the losses to the transition cost.
        double toReturn = -cost;
        for (int i = d.var() + 1; i < problem.nbVars(); i++) {
            toReturn += abs(to.netBenefit().get(i)) - abs(merged.netBenefit().get(i));
        }
        return -toReturn;
    }


}
