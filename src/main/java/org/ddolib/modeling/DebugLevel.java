package org.ddolib.modeling;

/**
 * Defines the different debug level.
 *
 */
public enum DebugLevel {

    /**
     * No debug.
     */
    OFF,
    /**
     * Cheks mandatory properties of components of your model (equality of states, definition of
     * the lower bound)
     */
    ON,
    /**
     * ON + add extra tools to help to debug (export failing diagrams  as dot for ddo, check
     * consistency for of the lower bound for A*)
     */
    EXTENDED
}
