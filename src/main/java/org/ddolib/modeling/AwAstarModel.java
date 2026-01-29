package org.ddolib.modeling;

/**
 * Defines the structure of an optimization model solved using the
 * <b>Anytime Weighted A*  (AWA*)</b> algorithm within the
 * Decision Diagram Optimization (DDO) framework.
 *
 * <p>The Anytime Weighted A*  algorithm is derived from the A* algorithm. By adding a weight to
 * the heuristic function, it speeds up reaching feasible solution. It incrementally improves the
 * best found solution. The {@code AwAsatar} interface provides the problem definition
 * * and configuration elements required by the ACS solver.</p>
 *
 * @param <T> the type of states in the problem
 */
public interface AwAstarModel<T> extends Model<T> {

    /**
     * Returns the weight to add to the heuristic function. Must be &#62; 1
     *
     * @return the weight to add to the heuristic function (5 by default)
     */
    default double weight() {
        return 5.0;
    }

}
