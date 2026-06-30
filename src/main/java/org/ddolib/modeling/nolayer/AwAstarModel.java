package org.ddolib.modeling.nolayer;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.verbosity.VerbosityLevel;

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
        return new AwAstarModel<T>() {
            @Override
            public Problem<T> problem() {
                return AwAstarModel.this.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return AwAstarModel.this.lowerBound();
            }

            @Override
            public NoLayerDominanceChecker<T> dominance() {
                return AwAstarModel.this.dominance();
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
