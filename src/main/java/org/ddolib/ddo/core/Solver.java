package org.ddolib.ddo.core;

import java.util.Optional;
import java.util.Set;

/**
 * This defines the expected behavior of a solver: an object able to find a solution
 * that maximizes the objective value of some underlying optimization problem. 
 */
public interface Solver {
    /** Tries to maximize the objective value of the problem which is being solved */
    void maximize(int verbose);
    void maximize();
    /** @return the value of the best solution in this decision diagram if there is one */
    Optional<Integer> bestValue();
    /** @return the solution leading to the best solution in this decision diagram (if it exists) */
    Optional<Set<Decision>> bestSolution();
}
