package org.ddolib.ddo.core.mdd;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * Obviously, this abstraction encapsulates the behavior of a reusable decision diagram.
 * The latter can be compiled either as a relaxed DD or as a restricted DD.
 *
 * @param <T> the type of state.
 */
public interface DecisionDiagram<T> {
    /**
     * Triggers the compilation of the decision diagram according to the parameters given
     * in the input.
     *
     */
    void compile();

    /**
     * @return true iff the diagram resulting from the compilation is an exact dd
     */
    boolean isExact();

    /**
     * @return the value of the best solution in this decision diagram if there is one
     */
    Optional<Double> bestValue();

    /**
     * @return the solution leading to the best solution in this decision diagram (if it exists)
     */
    Optional<Set<Decision>> bestSolution();

    /**
     * @return an iterator to the nodes of the exact cutset of the problem
     */
    Iterator<SubProblem<T>> exactCutset();

    /**
     * @return true iff the relaxed best path of the DD is exact
     */
    boolean relaxedBestPathIsExact();

    /**
     * Export the compiled MDD in .dot file format.
     *
     * @return A .dot formatted string of the compiled mdd.
     */
    String exportAsDot();
}
