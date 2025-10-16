package org.ddolib.modeling;

/**
 * Defines verbosity level
 */
public enum VerbosityLevel {
    /**
     * Nothing is printed.
     */
    SILENT,
    /**
     * Displays new best objective whenever there is a new one.
     */
    NORMAL,
    /**
     * Displays:
     * <ul>
     *     <li>New best objective whenever there is a new one.</li>
     *     <li>Statistics about the front every half second.</li>
     *     <li>Every developed sub-problem.</li>
     * </ul>
     */
    LARGE
}
