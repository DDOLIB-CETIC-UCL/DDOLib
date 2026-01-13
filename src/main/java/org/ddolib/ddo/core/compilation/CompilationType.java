package org.ddolib.ddo.core.compilation;

/**
 * Specifies the type of compilation to use when constructing a decision diagram (DD) for a problem.
 * <p>
 * The compilation type determines how the decision diagram is generated and whether it represents an exact solution,
 * an upper bound, or a lower bound of the objective function.
 * </p>
 */
public enum CompilationType {
    /**
     * Compile an exact decision diagram.
     * <p>
     * This approach corresponds to a pure dynamic programming (DP) resolution of the problem,
     * generating a full decision diagram that represents all feasible solutions exactly.
     * </p>
     */
    Exact,
    /**
     * Compile a restricted decision diagram.
     * <p>
     * This approach generates a decision diagram that is limited in width or depth,
     * providing an upper bound on the objective function rather than the exact solution.
     * </p>
     */
    Restricted,
    /**
     * Compile a relaxed decision diagram.
     * <p>
     * This approach generates a decision diagram that merges nodes to reduce complexity,
     * yielding a lower bound on the objective function rather than the exact solution.
     * </p>
     */
    Relaxed,
}
