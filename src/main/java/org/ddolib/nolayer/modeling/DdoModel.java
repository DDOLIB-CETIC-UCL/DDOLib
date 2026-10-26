package org.ddolib.nolayer.modeling;

import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.common.util.verbosity.VerbosityLevel;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.nolayer.solver.Solution;

/**
 * Defines the interface for an unlayered Dynamic Decision Diagram Optimization (DDO) model.
 *
 * @param <T> the state type
 */
public interface DdoModel<T> extends Model<T> {
    /**
     * Returns the relaxation of the model used to merge states in the decision diagram.
     *
     * @return the {@link Relaxation} object associated with this model
     */
    Relaxation<T> relaxation();

    /**
     * Returns the ranking function used to order states when width must be reduced. Higher priority
     * states are kept exact, lower priority states are dropped or merged.
     *
     * @return a {@link StateRanking} comparator between states
     */
    default StateRanking<T> ranking() {
        return (o1, o2) -> 0;
    }

    /**
     * Returns the width heuristic controlling the maximum number of states in the frontier.
     *
     * @return a {@link WidthHeuristic} instance controlling diagram width
     */
    default WidthHeuristic<T> widthHeuristic() {
        return new FixedWidth<>(10);
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
     * Indicates whether caching should be used during the diagram construction.
     *
     * @return {@code true} if caching is enabled, {@code false} otherwise
     */
    default boolean useCache() {
        return false;
    }

    /**
     * Returns a dominance checker to prune inferior states during compilation.
     *
     * @return a {@link NoLayerDominanceChecker} instance
     */
    default NoLayerDominanceChecker<T> dominance() {
        return new DefaultNoLayerDominanceChecker<>();
    }

    /**
     * Indicates whether the generated decision diagram should be exported to a DOT file (Graphviz
     * format).
     *
     * @return {@code true} if DOT export is enabled, {@code false} otherwise
     */
    default boolean exportDot() {
        return false;
    }

    /**
     * Returns a copy of this model but with the given initial solution.
     *
     * @param solution the initial solution (see {@link #initialSolution()})
     * @return a copy of this model but with the given initial solution
     */
    @Override
    default DdoModel<T> setInitialSolution(Solution solution) {
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
            public NoLayerDominanceChecker<T> dominance() {
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
            public ReductionStrategy<T> relaxStrategy() {
                return DdoModel.this.relaxStrategy();
            }

            @Override
            public ReductionStrategy<T> restrictStrategy() {
                return DdoModel.this.restrictStrategy();
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
            public Solution initialSolution() {
                return solution;
            }
        };
    }
}
