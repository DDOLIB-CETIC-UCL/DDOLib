package org.ddolib.examples.alp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Arrays;
import java.util.Iterator;
/**
 * Relaxation operator for {@link ALPState} in the Aircraft Landing Problem (ALP).
 * <p>
 * This class defines how multiple states are merged into a single relaxed state
 * when building a relaxed decision diagram. The relaxation helps to limit the size
 * of the decision diagram while maintaining bounds on the optimal solution.
 * </p>
 * <p>
 * The merged state is computed by:
 * </p>
 * <ul>
 *     <li>For each aircraft class, taking the minimum number of remaining aircraft
 *         among all merged states.</li>
 *     <li>For each runway, taking the minimum previous landing time among all merged states.</li>
 * </ul>
 * This ensures that the merged state is a valid under-approximation of the original states.
 *
 * @see ALPState
 * @see Relaxation
 */
public class ALPRelax implements Relaxation<ALPState> {
    /** The ALP problem instance. */
    ALPProblem problem;
    /**
     * Constructs a relaxation operator for the given ALP problem.
     *
     * @param problem the ALP problem instance
     */
    public ALPRelax(ALPProblem problem) {
        this.problem = problem;
    }
    /**
     * Merges multiple states into a single relaxed state.
     * <p>
     * The merged state takes the minimum remaining aircraft count per class
     * and the minimum previous landing time per runway.
     * </p>
     *
     * @param states an iterator over the states to merge
     * @return the merged relaxed state
     */
    @Override
    public ALPState mergeStates(Iterator<ALPState> states) {
        int[] remainingAircraft = new int[problem.nbClasses];
        Arrays.fill(remainingAircraft, Integer.MAX_VALUE);
        RunwayState[] runwayStates = new RunwayState[problem.nbRunways];
        Arrays.fill(runwayStates, new RunwayState(ALPProblem.DUMMY, Integer.MAX_VALUE));

        // Set the remaining nb of aircraft (for each class) of the merged state as the minimal value of each merged states.
        // Set the previous time of each runway of the merged state as the minimal value of each merged states.
        while (states.hasNext()) {
            ALPState s = states.next();
            for (int i = 0; i < remainingAircraft.length; i++) {
                remainingAircraft[i] = Math.min(remainingAircraft[i], s.remainingAircraftOfClass[i]);
            }
            for (int i = 0; i < runwayStates.length; i++) {
                runwayStates[i].prevTime = Math.min(s.runwayStates[i].prevTime, runwayStates[i].prevTime);
            }
        }

        return new ALPState(remainingAircraft, runwayStates);
    }
    /**
     * Returns the relaxed cost of a transition (edge) between states.
     * <p>
     * In this default implementation, the edge cost remains unchanged.
     * </p>
     *
     * @param from the source state
     * @param to the destination state
     * @param merged the merged state
     * @param d the decision leading to this transition
     * @param cost the original cost of the transition
     * @return the relaxed cost of the edge
     */
    @Override
    public double relaxEdge(ALPState from, ALPState to, ALPState merged, Decision d, double cost) {
        return cost;
    }
}
