package org.ddolib.util.verbosity;

/**
 * Defines the different verbosity levels controlling the amount of information
 * printed during the execution of a solver.
 * <p>
 * The verbosity level determines how much runtime feedback is displayed
 * while solving an optimization problem. This can range from completely silent
 * operation to detailed tracing of solver progress and subproblem development.
 * </p>
 *
 * <p>
 * Typical usage involves selecting an appropriate verbosity level in the model
 * configuration (see {@link org.ddolib.modeling.Model#verbosityLevel()}), depending
 * on whether the focus is on performance, debugging, or visualization of the search process.
 * </p>
 */
public enum VerbosityLevel {
    /**
     * No output is produced.
     * <p>
     * This mode is intended for fully silent execution, where performance
     * measurements or logs are not required.
     * </p>
     */
    SILENT,
    /**
     * Displays important progress updates.
     * <p>
     * In this mode, the solver prints a message each time a new best objective
     * value is found during the search.
     * </p>
     */
    NORMAL,
    /**
     * Displays detailed runtime information for debugging or analysis purposes.
     * <p>
     * In this mode, the solver outputs:
     * </p>
     * <ul>
     *     <li>A message whenever a new best objective is found.</li>
     *     <li>Periodic statistics about the search frontier (approximately every 0.5 seconds).</li>
     *     <li>Information about each developed subproblem as the search progresses.</li>
     * </ul>
     * This mode provides the highest level of detail and is useful for
     * performance analysis and algorithmic tuning.
     */
    LARGE,

    /**
     * Same that {@code LARGE} but save the logs into {@code logs.txt} files.
     */
    EXPORT,
}
