package org.ddolib.modeling;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.verbosity.VerbosityLevel;

/**
 * Defines the structure of an optimization model solved using the
 * <b>Anytime Weighted A*  (AWA*)</b> algorithm within the
 * Decision Diagram Optimization (DDO) framework.
 *
 * <p>The Anytime Weighted A*  algorithm is derived from the A* algorithm. By adding a weight to
 * the heuristic function, it speeds up reaching feasible solution. It incrementally improves the
 * best found solution. The {@code AwAsatar} interface provides the problem definition
 * * and configuration elements required by the ACS solver.</p>
 *
 * @param <T> the type of states in the problem
 */
public interface AwAstarModel<T> extends Model<T> {

    /**
     * Returns the weight to add to the heuristic function. Must be &#62; 1
     *
     * @return the weight to add to the heuristic function (5 by default)
     */
    default double weight() {
        return 5.0;
    }

    /**
     * Returns a copy of this model but with another weight.
     *
     * @param w The weight to use.
     * @return a copy of this model but with another weight.
     */
    default AwAstarModel<T> setWeight(double w) {
        return new AwAstarModel<>() {
            @Override
            public Problem<T> problem() {
                return AwAstarModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return AwAstarModel.this.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return AwAstarModel.this.dominance();
            }

            @Override
            public VariableHeuristic<T> variableHeuristic() {
                return AwAstarModel.this.variableHeuristic();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return AwAstarModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return AwAstarModel.this.debugMode();
            }

            @Override
            public double weight() {
                return w;
            }
        };
    }

}
