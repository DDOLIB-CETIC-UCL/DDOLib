package org.ddolib.modeling;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.verbosity.VerbosityLevel;
/**
 * Interface representing a model for Large Neighborhood Search (LNS) problems.
 *
 * <p>This interface extends {@link Model} and provides default implementations
 * and configuration options specifically for LNS-based search algorithms.</p>
 *
 * <p>Key responsibilities of an {@code LnsModel} include:</p>
 * <ul>
 *     <li>Providing the underlying {@link Problem} instance.</li>
 *     <li>Specifying a {@link FastLowerBound} for efficient lower bound estimation.</li>
 *     <li>Optionally providing a {@link DominanceChecker} to prune dominated states.</li>
 *     <li>Providing heuristics such as {@link StateRanking} and {@link WidthHeuristic} to guide search.</li>
 *     <li>Supporting optional configuration of LNS parameters like initial solution, destruction probability, and width.</li>
 * </ul>
 *
 * <p>Default implementations:</p>
 * <ul>
 *     <li>{@link #ranking()} returns a trivial ranking (no preference).</li>
 *     <li>{@link #widthHeuristic()} defaults to a {@link FixedWidth} of 10.</li>
 *     <li>{@link #exportDot()} returns {@code false} (no DOT export by default).</li>
 *     <li>{@link #restrictStrategy()} defaults to a {@link CostBased} reduction with zero comparator.</li>
 *     <li>{@link #stateDistance()} returns 0 between any two states.</li>
 *     <li>{@link #initialSolution()} returns {@code null} (no initial solution by default).</li>
 *     <li>{@link #probability()} returns 0.2 as default destruction probability.</li>
 *     <li>{@link #useLNS()} returns {@code true} by default.</li>
 * </ul>
 *
 * <p>Configuration methods allow creating modified copies of the model with custom parameters:</p>
 * <ul>
 *     <li>{@link #fixWidth(int)} returns a new {@code LnsModel} with a fixed search width.</li>
 *     <li>{@link #setInitialSolution(int[])} returns a new {@code LnsModel} using a given initial solution.</li>
 *     <li>{@link #setProbability(double)} returns a new {@code LnsModel} with a specified destruction probability.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * LnsModel model = new MyLnsModel()
 *     .fixWidth(20)
 *     .setInitialSolution(mySolution)
 *     .setProbability(0.3);
 * </pre>
 *
 * <p>This interface is intended for use with {@link Solvers#minimizeLns(LnsModel, java.util.function.Predicate, java.util.function.BiConsumer)}
 * or similar LNS solvers.</p>
 *
 * @param <T> the type of state used in the problem
 */
public interface LnsModel<T> extends Model<T> {

    /**
     * Returns the state ranking heuristic used to guide the search.
     * Default implementation returns a neutral ranking (all states equal).
     *
     * @return the state ranking
     */
    default StateRanking<T> ranking() {
        return (o1, o2) -> 0;
    }
    /**
     * Returns the width heuristic used for tree exploration.
     * Default is {@link FixedWidth} with width 10.
     *
     * @return the width heuristic
     */
    default WidthHeuristic<T> widthHeuristic() {
        return new FixedWidth<>(10);
    }
    /**
     * Indicates whether to export the search tree to DOT format.
     * Default is {@code false}.
     *
     * @return {@code true} if DOT export is enabled, {@code false} otherwise
     */
    default boolean exportDot() {
        return false;
    }
    /**
     * Returns the strategy used to restrict the search neighborhood.
     *
     * @return the reduction strategy
     */
    default ReductionStrategy<T> restrictStrategy() {
        return new CostBased<>((o1, o2) -> 0);
    }
    /**
     * Returns a measure of distance between two states.
     * Default returns 0 for all states.
     *
     * @return the state distance
     */
    default StateDistance<T> stateDistance() {
        return (o1, o2) -> 0;
    }
    /**
     * Returns the initial solution to start the search from.
     * Default is {@code null} (no initial solution).
     *
     * @return the initial solution as an array of variable assignments
     */
    default int[] initialSolution() {
        return null;
    }
    /**
     * Returns the probability used to destruct parts of the solution in LNS.
     * Default is 0.2.
     *
     * @return destruction probability
     */
    default double probability() {return 0.2;}
    /**
     * Indicates whether LNS should be used.
     * Default is {@code true}.
     *
     * @return {@code true} if LNS is enabled, {@code false} otherwise
     */
    default boolean useLNS() {return true;}

    /**
     * Returns a copy of this model with a fixed search width.
     *
     * @param width the width to fix
     * @return a new {@code LnsModel} with the specified width
     */
    default LnsModel<T> fixWidth(int width) {
        return new LnsModel<>() {
            @Override
            public Problem<T> problem() {
                return LnsModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return LnsModel.this.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return LnsModel.this.dominance();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return LnsModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return LnsModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return LnsModel.this.debugMode();
            }

            @Override
            public StateRanking<T> ranking() {
                return LnsModel.this.ranking();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return new FixedWidth<>(width);
            }

            @Override
            public boolean exportDot() {
                return LnsModel.this.exportDot();
            }

            @Override
            public boolean useLNS() {
                return LnsModel.this.useLNS();
            }
        };
    }

    /**
     * Returns a copy of this model with a specified initial solution.
     *
     * @param solution the initial solution
     * @return a new {@code LnsModel} using the given initial solution
     */
    default LnsModel<T> setInitialSolution(int[] solution) {
        return new LnsModel<>() {
            @Override
            public Problem<T> problem() {
                return LnsModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return LnsModel.this.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return LnsModel.this.dominance();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return LnsModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return LnsModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return LnsModel.this.debugMode();
            }

            @Override
            public StateRanking<T> ranking() {
                return LnsModel.this.ranking();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return LnsModel.this.widthHeuristic();
            }

            @Override
            public boolean exportDot() {
                return LnsModel.this.exportDot();
            }

            @Override
            public int[] initialSolution() {return  solution;}

            @Override
            public boolean useLNS() {
                return LnsModel.this.useLNS();
            }
        };
    }
    /**
     * Returns a copy of this model with a specified destruction probability.
     *
     * @param proba the probability to use in LNS
     * @return a new {@code LnsModel} with the specified probability
     */
    default LnsModel<T> setProbability(double proba) {
        return new LnsModel<>() {
            @Override
            public Problem<T> problem() {
                return LnsModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return LnsModel.this.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return LnsModel.this.dominance();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return LnsModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return LnsModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return LnsModel.this.debugMode();
            }

            @Override
            public StateRanking<T> ranking() {
                return LnsModel.this.ranking();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return LnsModel.this.widthHeuristic();
            }

            @Override
            public boolean exportDot() {
                return LnsModel.this.exportDot();
            }

            @Override
            public int[] initialSolution() {return  LnsModel.this.initialSolution();}

            @Override
            public double probability() {return proba;}

            @Override
            public boolean useLNS() {
                return LnsModel.this.useLNS();
            }
        };
    }
}
