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
     *                       <li> Level 3: In addition, the first restricted and relaxed explored mdd are exported
     *                       as .dot files in the output directory.</li>
     *                       </ul>
     */
    SearchStatistics maximize(int verbosityLevel);

    /**
     * Tries to maximize the objective value of the problem which is being solved
     */
    SearchStatistics maximize();

    /**
     * @return the value of the best solution in this decision diagram if there is one
     */
    Optional<Integer> bestValue();

    /**
     * @return the solution leading to the best solution in this decision diagram (if it exists)
     */
    Optional<Set<Decision>> bestSolution();
}
