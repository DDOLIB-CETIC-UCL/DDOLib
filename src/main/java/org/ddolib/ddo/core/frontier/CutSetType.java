package org.ddolib.ddo.core.frontier;

/**
 * Cutset type for the decision diagram compilation.
 */
public enum CutSetType {
    LastExactLayer,
    Frontier,
    None // used in RelaxationSolver and RestrictionSolver
}
