package org.ddolib.ddo.core;

import java.util.Optional;
import java.util.Set;

/**
 * This defines the expected behavior of a solver: an object able to find a solution
 * that maximizes the objective value of some underlying optimization problem.
 */
public interface Solver {
    /**
     * Tries to maximize the objective value of the problem which is being solved
     *
     * @param verbosityLevel <ul>
     *                       <li>Level 0: nothing is printed.</li>
     *                       <li> Level 1: The iteration number, the size of the frontier and the current objective
     *                       value are printed. </li>
     *                       <li> Level 2: In addition, the root node of the current exploration is printed.</li>
     *                       </ul>
     * @param exportAsDot    Whether we want to export the first explored restricted and relaxed mdd. Tooltips are
     *                       configured to give additional information on nodes and edges.
     */
    SearchStatistics maximize(int verbosityLevel, boolean exportAsDot);

    /**
     * Tries to maximize the objective value of the problem which is being solved
     */
    SearchStatistics maximize();

    /**
     * @return the value of the best solution in this decision diagram if there is one
     */
    Optional<Double> bestValue();

    /**
     * @return the solution leading to the best solution in this decision diagram (if it exists)
     */
    Optional<Set<Decision>> bestSolution();
}
