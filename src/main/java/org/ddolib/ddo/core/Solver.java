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
     *@param verbosityLevel
     *            0: no verbosities
     *            1: statistics about the front every tenth of a second (or so)
     *            2: statistics about the front at every sub-problem
     *            3: 2 + the detail of the sub-problem at hand
     * @return statistics about the search
     */
    SearchStatistics maximize(int verbosityLevel);
    /** Tries to maximize the objective value of the problem which is being solved */
    SearchStatistics maximize();
    /** @return the value of the best solution in this decision diagram if there is one */
    Optional<Double> bestValue();
    /** @return the solution leading to the best solution in this decision diagram (if it exists) */
    Optional<Set<Decision>> bestSolution();
}
