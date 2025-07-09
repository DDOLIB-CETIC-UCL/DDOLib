package org.ddolib.ddo.algo.solver;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.profiling.SearchStatistics;

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
     * @param verbosityLevel 0: no verbosity
     *                       1: display newBest whenever there is a newBest
     *                       2: 1 + statistics about the front every half a second (or so)
     *                       3: 2 + every developed sub-problem
     *                       4: 3 + details about the developed state
     * @param exportAsDot    whether we want to export the first explored restricted and relaxed mdd.
     *                       Tooltips are configured to give additional information on nodes and edges.
     * @return statistics about the search
     */
    SearchStatistics maximize(int verbosityLevel, boolean exportAsDot);

    /**
     * Tries to maximize the objective value of the problem which is being solved. The verbosity is set to 0 and the
     * export to .dot file is disabled.
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
