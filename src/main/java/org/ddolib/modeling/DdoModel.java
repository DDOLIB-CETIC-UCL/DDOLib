package org.ddolib.modeling;

import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
/**
 * Defines the interface for a Dynamic Decision Diagram Optimization (DDO) model.
 * <p>
 * A {@code DdoModel} extends the generic {@link Model} interface by providing
 * methods related to relaxation, ranking, width control, and frontier management.
 * It serves as the foundation for implementing algorithms that construct and explore
 * decision diagrams dynamically (e.g., exact or approximate methods).
 * </p>
 *
 * <p>This interface provides several default behaviors, which can be overridden
 * to customize model evaluation, search strategy, or diagram generation.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Provide the problem relaxation used for node evaluation ({@link #relaxation()}).</li>
 *   <li>Define the ranking strategy between states ({@link #ranking()}).</li>
 *   <li>Control the diagram’s width through heuristics ({@link #widthHeuristic()}).</li>
 *   <li>Specify how to maintain and update the frontier ({@link #frontier()}).</li>
 *   <li>Indicate optional behaviors like caching ({@link #useCache()}) or exporting the structure ({@link #exportDot()}).</li>
 * </ul>
 * @param <T> the state type
 */
public interface DdoModel<T> extends Model<T> {
    /**
     * Returns the relaxation of the model used to evaluate the nodes or layers
     * of the decision diagram.
     * <p>
     * The relaxation defines a simplified or approximate version of the problem,
     * which helps bound the objective function or guide the search.
     * </p>
     *
     * @return the {@link Relaxation} object associated with this model
     */
    Relaxation<T> relaxation();
    /**
     * Returns the ranking function used to order states within a layer.
     * <p>
     * The default implementation imposes no specific order (always returns 0),
     * meaning all states are considered equal in priority.
     * Override this method to implement problem-specific ranking heuristics.
     * </p>
     *
     * @return a {@link StateRanking} comparator between states
     */
    default StateRanking<T> ranking() {
        return (o1, o2) -> 0;
    }
    /**
     * Returns the width heuristic controlling the maximum number of nodes per layer.
     * <p>
     * The default implementation uses a fixed width of 10.
     * </p>
     *
     * @return a {@link WidthHeuristic} instance controlling diagram width
     */
    default WidthHeuristic<T> widthHeuristic() {
        return new FixedWidth<>(10);
    }
    /**
     * Returns the frontier management strategy used to store and expand the current
     * layer of the decision diagram.
     * <p>
     * The default implementation creates a {@link SimpleFrontier} based on
     * {@link #ranking()} and uses {@link CutSetType#LastExactLayer} as the cut set.
     * </p>
     *
     * @return a {@link Frontier} instance defining the search frontier
     */
    default Frontier<T> frontier() {
        return new SimpleFrontier<>(ranking(), CutSetType.LastExactLayer);
    }
    /**
     * Indicates whether caching should be used during the diagram construction.
     * <p>
     * Caching stores intermediate results to avoid redundant computations.
     * By default, caching is disabled.
     * </p>
     *
     * @return {@code true} if caching is enabled, {@code false} otherwise
     */
    default boolean useCache() {
        return false;
    }
    /**
     * Indicates whether the generated decision diagram should be exported
     * to a DOT file (Graphviz format).
     * <p>
     * This feature allows visualization of the structure of the generated
     * decision diagram for debugging or analysis purposes.
     * By default, export is disabled.
     * </p>
     *
     * @return {@code true} if DOT export is enabled, {@code false} otherwise
     */
    default boolean exportDot() {
        return false;
    }

    /**
     * Strategy to select which nodes should be merged together on a relaxed DD.
     */
    default ReductionStrategy<T> relaxStrategy() {
        return new CostBased<>((o1, o2) -> 0);
    }

    /**
     * Strategy to select which nodes should be dropped on a restricted DD.
     */
    default ReductionStrategy<T> restrictStrategy() {
        return new CostBased<>((o1, o2) -> 0);
    }

}
