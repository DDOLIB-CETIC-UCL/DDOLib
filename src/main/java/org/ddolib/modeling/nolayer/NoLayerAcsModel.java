package org.ddolib.modeling.nolayer;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.verbosity.VerbosityLevel;

/**
 * Defines the structure of an optimization model solved using the
 * <b>Anytime Column Search (ACS)</b> algorithm within the
 * Decision Diagram Optimization (DDO) framework using the NoLayer API.
 *
 * @param <T> the type representing the state of the problem
 */
public interface NoLayerAcsModel<T> extends NoLayerModel<T> {

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
    default NoLayerAcsModel<T> setColumnWidth(int width) {
        return new NoLayerAcsModel<T>() {
            @Override
            public NoLayerProblem<T> problem() {
                return NoLayerAcsModel.this.problem();
            }

            @Override
            public NoLayerFastLowerBound<T> lowerBound() {
                return NoLayerAcsModel.this.lowerBound();
            }

            @Override
            public NoLayerDominanceChecker<T> dominance() {
                return NoLayerAcsModel.this.dominance();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return NoLayerAcsModel.this.verbosityLevel();
            }

            @Override
            public DebugLevel debugMode() {
                return NoLayerAcsModel.this.debugMode();
            }

            @Override
            public double upperBound() {
                return NoLayerAcsModel.this.upperBound();
            }

            @Override
            public int columnWidth() {
                return width;
            }
        };
    }
}
