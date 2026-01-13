package org.ddolib.ddo.core.mdd;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * Defines the abstraction of a reusable <b>Decision Diagram (DD)</b> used to model and solve
 * combinatorial optimization problems.
 * <p>
 * A decision diagram is a layered graph-based representation of the solution space of a problem.
 * Depending on how it is compiled, the DD can be:
 * </p>
 * <ul>
 *   <li><b>Exact</b> — representing all feasible solutions of the original problem (no relaxation);</li>
 *   <li><b>Relaxed</b> — merging nodes to reduce the diagram width, potentially omitting some constraints
 *       while preserving valid lower or upper bounds on the optimal value.</li>
 * </ul>
 *
 * <p>This interface defines the basic operations available on a compiled DD, including:
 * triggering compilation, accessing the best value or solution, iterating over nodes in the
 * cutset, and exporting the structure for visualization.</p>
 *
 * @param <T> the type representing the problem state at a given layer in the diagram
 */
public interface DecisionDiagram<T> {
    /**
     * Triggers the compilation of the decision diagram according to the configuration
     * parameters provided by the user (e.g., width, relaxation, variable heuristic, etc.).
     * <p>
     * The compilation builds the layered graph structure that encodes the feasible (or
     * relaxed) state space of the problem. Depending on the configuration, this process
     * may yield either an <em>exact</em> or a <em>relaxed</em> DD.
     * </p>
     */
    void compile();

    /**
     * Indicates whether the compiled decision diagram is exact.
     * <p>
     * An exact DD represents the full solution space of the original problem, meaning
     * that the best path in the DD corresponds exactly to the true optimal solution.
     * </p>
     *
     * @return {@code true} if the compiled DD is exact; {@code false} if it is relaxed
     */
    boolean isExact();

    /**
     * Returns the value of the best solution represented in this decision diagram, if it exists.
     * <p>
     * For a relaxed DD, this corresponds to a bound (lower or upper depending on the problem
     * formulation). For an exact DD, it is the exact optimal value.
     * </p>
     *
     * @return an {@link Optional} containing the best objective value, or an empty
     *         {@link Optional} if no solution is available
     */
    Optional<Double> bestValue();

    /**
     * Returns the sequence of decisions leading to the best solution represented in this DD.
     * <p>
     * The returned solution corresponds to the path in the diagram that achieves the best
     * (lowest or highest) objective value. If the DD is relaxed, the returned set may not
     * correspond to a fully feasible solution.
     * </p>
     *
     * @return an {@link Optional} containing the set of {@link Decision} objects defining
     *         the best solution, or an empty {@link Optional} if none exists
     */
    Optional<Set<Decision>> bestSolution();

    /**
     * Provides an iterator over the nodes belonging to the <em>exact cutset</em> of the diagram.
     * <p>
     * The exact cutset is the set of subproblems (nodes) that exactly represent the state space
     * at a specific layer, usually the last layer before relaxation or completion. It can be used
     * to guide recomputation, heuristic extensions, or incremental compilation.
     * </p>
     *
     * @return an {@link Iterator} over the {@link SubProblem} elements forming the exact cutset
     */
    Iterator<SubProblem<T>> exactCutset();

    /**
     * Checks whether the best path found in the relaxed decision diagram is exact.
     * <p>
     * This can occur when the relaxation did not alter the structure along the optimal path,
     * meaning the computed best value is equal to the true optimum even though the DD is relaxed.
     * </p>
     *
     * @return {@code true} if the relaxed best path corresponds to an exact feasible solution;
     *         {@code false} otherwise
     */
    boolean relaxedBestPathIsExact();

    /**
     * Exports the compiled decision diagram as a Graphviz-compatible
     * <a href="https://graphviz.org/doc/info/lang.html">DOT</a> formatted string.
     * <p>
     * This method is typically used for visualization or debugging purposes.
     * The resulting DOT string can be rendered using Graphviz tools (e.g., {@code dot},
     * {@code neato}, or web-based renderers).
     * </p>
     *
     * @return a {@link String} containing the DOT representation of the compiled decision diagram
     */
    String exportAsDot();
}
