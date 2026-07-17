package org.ddolib.layered.modeling;

import org.ddolib.common.frontier.CutSetType;
import org.ddolib.common.frontier.Frontier;
import org.ddolib.common.frontier.SimpleFrontier;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.layered.solving.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.layered.solving.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.layered.solving.ddo.core.heuristics.cluster.StateDistance;
import org.ddolib.layered.solving.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.common.util.verbosity.VerbosityLevel;

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
 *
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
     *
     * @return the {@link ReductionStrategy} used to merge nodes in a relaxed decision diagram
     */
    default ReductionStrategy<T> relaxStrategy() {
        return new CostBased<>((o1, o2) -> 0);
    }

    /**
     * Strategy to select which nodes should be dropped on a restricted DD.
     *
     * @return the {@link ReductionStrategy} used to drop nodes in a restricted decision diagram
     */
    default ReductionStrategy<T> restrictStrategy() {
        return new CostBased<>((o1, o2) -> 0);
    }

    /**
     * Returns the distance measure used to compare states, notably to select the neighborhood
     * explored by a large neighborhood search.
     *
     * @return the {@link StateDistance} used to compare states
     */
    default StateDistance<T> stateDistance() {
        return (o1, o2) -> 0;
    }

    /**
     * Indicates whether Large Neighborhood Search (LNS) should be used to improve the solution
     * found by this model.
     * <p>
     * By default, LNS is disabled.
     * </p>
     *
     * @return {@code true} if LNS is enabled, {@code false} otherwise
     */
    default boolean useLNS() {
        return false;
    }


    /**
     * Returns a copy of this model but with a fixed width.
     *
     * @param width the maximum width of the diagram
     * @return a copy of this model but with a fixed width
     */
    default DdoModel<T> fixWidth(int width) {
        return new DdoModel<>() {
            @Override
            public Problem<T> problem() {
                return DdoModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return DdoModel.this.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return DdoModel.this.dominance();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return DdoModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return DdoModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return DdoModel.this.debugMode();
            }

            @Override
            public Relaxation<T> relaxation() {
                return DdoModel.this.relaxation();
            }

            @Override
            public StateRanking<T> ranking() {
                return DdoModel.this.ranking();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return new FixedWidth<>(width);
            }

            @Override
            public Frontier<T> frontier() {
                return DdoModel.this.frontier();
            }

            @Override
            public boolean useCache() {
                return DdoModel.this.useCache();
            }

            @Override
            public boolean exportDot() {
                return DdoModel.this.exportDot();
            }

            @Override
            public boolean useLNS() {
                return DdoModel.this.useLNS();
            }
        };
    }

    /**
     * Returns a copy of this model by changing the {@link CutSetType}.
     *
     * @param type the new cutset type
     * @return a copy of this model by changing the {@link CutSetType}
     */
    default DdoModel<T> setCutSetType(CutSetType type) {
        return new DdoModel<>() {
            @Override
            public Problem<T> problem() {
                return DdoModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return DdoModel.this.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return DdoModel.this.dominance();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return DdoModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return DdoModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return DdoModel.this.debugMode();
            }

            @Override
            public Relaxation<T> relaxation() {
                return DdoModel.this.relaxation();
            }

            @Override
            public StateRanking<T> ranking() {
                return DdoModel.this.ranking();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return DdoModel.this.widthHeuristic();
            }

            @Override
            public Frontier<T> frontier() {
                return new SimpleFrontier<>(ranking(), type);
            }

            @Override
            public boolean useCache() {
                return DdoModel.this.useCache();
            }

            @Override
            public boolean exportDot() {
                return DdoModel.this.exportDot();
            }

            @Override
            public boolean useLNS() {
                return DdoModel.this.useLNS();
            }
        };
    }

    /**
     * Returns a copy of this model by enabling or disabling the cache.
     *
     * @param b whether the cache must be used
     * @return a copy of this model by enabling or disabling the cache
     */
    default DdoModel<T> useCache(boolean b) {
        return new DdoModel<T>() {
            @Override
            public Problem<T> problem() {
                return DdoModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return DdoModel.this.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return DdoModel.this.dominance();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return DdoModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return DdoModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return DdoModel.this.debugMode();
            }

            @Override
            public Relaxation<T> relaxation() {
                return DdoModel.this.relaxation();
            }

            @Override
            public StateRanking<T> ranking() {
                return DdoModel.this.ranking();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return DdoModel.this.widthHeuristic();
            }

            @Override
            public Frontier<T> frontier() {
                return DdoModel.this.frontier();
            }

            @Override
            public boolean useCache() {
                return b;
            }

            @Override
            public boolean exportDot() {
                return DdoModel.this.exportDot();
            }

            @Override
            public boolean useLNS() {
                return DdoModel.this.useLNS();
            }
        };
    }

    @Override
    default DdoModel<T> disableDominance() {
        return new DdoModel<>() {
            @Override
            public Problem<T> problem() {
                return DdoModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return DdoModel.this.lowerBound();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return DdoModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return DdoModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return DdoModel.this.debugMode();
            }

            @Override
            public Relaxation<T> relaxation() {
                return DdoModel.this.relaxation();
            }

            @Override
            public StateRanking<T> ranking() {
                return DdoModel.this.ranking();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return DdoModel.this.widthHeuristic();
            }

            @Override
            public Frontier<T> frontier() {
                return DdoModel.this.frontier();
            }

            @Override
            public boolean useCache() {
                return DdoModel.this.useCache();
            }

            @Override
            public boolean exportDot() {
                return DdoModel.this.exportDot();
            }

            @Override
            public boolean useLNS() {
                return DdoModel.this.useLNS();
            }
        };
    }

    @Override
    default DdoModel<T> disableLowerBound() {
        return new DdoModel<>() {
            @Override
            public Problem<T> problem() {
                return DdoModel.this.problem();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return DdoModel.this.dominance();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return DdoModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return DdoModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return DdoModel.this.debugMode();
            }

            @Override
            public Relaxation<T> relaxation() {
                return DdoModel.this.relaxation();
            }

            @Override
            public StateRanking<T> ranking() {
                return DdoModel.this.ranking();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return DdoModel.this.widthHeuristic();
            }

            @Override
            public Frontier<T> frontier() {
                return DdoModel.this.frontier();
            }

            @Override
            public boolean useCache() {
                return DdoModel.this.useCache();
            }

            @Override
            public boolean exportDot() {
                return DdoModel.this.exportDot();
            }

            @Override
            public DdoModel<T> fixWidth(int width) {
                return DdoModel.this.fixWidth(width);
            }

            @Override
            public boolean useLNS() {
                return DdoModel.this.useLNS();
            }
        };
    }

    /**
     * Convert this model into a model for the exact solver.
     *
     * @return a model usable by an {@link ExactModel}
     */
    default ExactModel<T> toExactModel() {
        return new ExactModel<T>() {
            @Override
            public Problem<T> problem() {
                return DdoModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return DdoModel.this.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return DdoModel.this.dominance();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return DdoModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return DdoModel.this.debugMode();
            }

            @Override
            public boolean exportDot() {
                return DdoModel.this.exportDot();
            }

            @Override
            public ExactModel<T> toExactModel() {
                return DdoModel.this.toExactModel();
            }
        };
    }

}
