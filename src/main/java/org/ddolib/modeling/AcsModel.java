package org.ddolib.modeling;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.verbosity.VerbosityLevel;

/**
 * Defines the structure of an optimization model solved using the
 * <b>Anytime Column Search (ACS)</b> algorithm within the
 * Decision Diagram Optimization (DDO) framework.
 *
 * <p>
 * The Anytime Column Search is an iterative approach that incrementally
 * builds and refines decision diagrams (MDDs) to improve solution quality
 * over time. The {@code AcsModel} interface provides the problem definition
 * and configuration elements required by the ACS solver.
 * </p>
 *
 * @param <T> the type representing the state of the problem
 * @see Model
 * @see Solvers
 */

public interface AcsModel<T> extends Model<T> {
    /**
     * Returns the default column width used for formatted output during
     * the Anytime Column Search process.
     * <p>
     * This parameter is primarily used for visual alignment of
     * intermediate results or search statistics in console or file outputs.
     * </p>
     *
     * @return the default column width, in characters (default is {@code 5})
     */

    default int columnWidth() {
        return 5;
    }

    /**
     * Returns a copy of this model but with another column width.
     *
     * @param width The column width to use.
     * @return A copy of this model but with another column width.
     */
    default AcsModel<T> setColumnWidth(int width) {
        return new AcsModel<T>() {
            @Override
            public Problem<T> problem() {
                return AcsModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return AcsModel.this.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return AcsModel.this.dominance();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return AcsModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return AcsModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return AcsModel.this.debugMode();
            }

            @Override
            public int columnWidth() {
                return width;
            }
        };
    }

    @Override
    default AcsModel<T> disableDominance() {
        return new AcsModel<>() {
            @Override
            public Problem<T> problem() {
                return AcsModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return AcsModel.this.lowerBound();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return AcsModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return AcsModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return AcsModel.this.debugMode();
            }

            @Override
            public int columnWidth() {
                return AcsModel.this.columnWidth();
            }
        };
    }

    @Override
    default AcsModel<T> disableLowerBound() {
        return new AcsModel<T>() {
            @Override
            public Problem<T> problem() {
                return AcsModel.this.problem();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return AcsModel.this.dominance();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return AcsModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return AcsModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return AcsModel.this.debugMode();
            }

            @Override
            public int columnWidth() {
                return AcsModel.this.columnWidth();
            }
        };
    }
}
