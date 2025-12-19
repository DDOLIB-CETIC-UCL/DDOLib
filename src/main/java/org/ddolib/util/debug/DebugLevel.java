package org.ddolib.util.debug;

/**
 * Defines the different levels of debugging information and validation
 * that can be enabled during the execution of the solver.
 * <p>
 * The {@code DebugLevel} controls how strictly the framework validates
 * internal consistency, model correctness, and debugging artifacts
 * generated during search. Higher levels include all checks from
 * lower levels, plus additional diagnostics and export tools.
 * </p>
 *
 * <p>
 * This configuration is typically set through the model definition
 * (see {@link org.ddolib.modeling.Model#debugMode()}), allowing
 * users to choose between performance-oriented runs and
 * detailed debugging sessions.
 * </p>
 */
public enum DebugLevel {

    /**
     * Disables all debugging features.
     * <p>
     * This mode is intended for production runs or benchmarking,
     * where performance is prioritized and no additional checks
     * or debug information are generated.
     * </p>
     */
    OFF,
    /**
     * Enables basic debugging checks.
     * <p>
     * This mode verifies fundamental properties of the model components,
     * such as:
     * </p>
     * <ul>
     *     <li>Equality and correctness of state representations.</li>
     *     <li>Proper definition and consistency of lower bounds.</li>
     * </ul>
     * These checks help detect common modeling or implementation errors
     * early in the solving process.
     */
    ON,
    /**
     * Enables extended debugging and diagnostic tools.
     * <p>
     * Includes all checks from {@link #ON}, and adds:
     * </p>
     * <ul>
     *     <li>Export of failing or inconsistent decision diagrams
     *     (as <code>.dot</code> files) to assist with visualization and analysis.</li>
     *     <li>Additional consistency verification of lower-bound
     *     computations (particularly useful for A*-based algorithms).</li>
     * </ul>
     * This mode is recommended when investigating unexpected solver
     * behavior or validating complex model implementations.
     *
     */
    EXTENDED
}
