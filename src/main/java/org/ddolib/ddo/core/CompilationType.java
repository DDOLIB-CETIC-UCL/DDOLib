package org.ddolib.ddo.core;

/**
 * How are we to compile the decision diagram ? 
 */
public enum CompilationType {
    /** If you want to use a pure DP resolution of the problem */
    Exact,
    /** If you want to compile a restricted DD which yields a lower bound on the objective */
    Restricted,
    /** If you want to compile a relaxed DD which yields an upper bound on the objective */
    Relaxed,
}
