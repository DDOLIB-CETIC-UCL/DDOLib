package org.ddolib.ddo.core;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * Obviously, this abstraction encapsulates the behavior of a reusable decision diagram.
 * The latter can be compiled either as a relaxed DD or as a restricted DD.
 *
 * @param <T> the type of state.
 * @param <K> the type of key
 */
public interface DecisionDiagram<T, K> {
    /**
     * Triggers the compilation of the decision diagram according to the parameters given
     * in the input.
     *
     * @param input this corresponds to the set of parameters in the input section of the
     *              algorithm pseudocode
     */
    void compile(final CompilationInput<T, K> input);

    /**
     * @return true iff the diagram resulting from the compilation is an exact dd
     */
    boolean isExact();
    /** @return the value of the best solution in this decision diagram if there is one */
    Optional<Double> bestValue();
    /** @return the solution leading to the best solution in this decision diagram (if it exists) */
    Optional<Set<Decision>> bestSolution();

    /**
     * @return an iterator to the nodes of the exact cutset of the problem
     */
    Iterator<SubProblem<T>> exactCutset();

    /**@return true iff the relaxed best path of the DD is exact */
    boolean relaxedBestPathIsExact();

    /**
     * Export the compiled MDD in .dot file format.
     *
     * @return A .dot formatted string of the compiled mdd.
     */
    String exportAsDot();
}
