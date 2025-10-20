package org.ddolib.modeling;

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
}
