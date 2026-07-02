/**
 * This package contains models formulated using the layered modeling API.
 * <p>
 * In the layered modeling API, a fixed number of variables (or layers) must be specified.
 * The solver explores the state space layer by layer, building paths from the root layer down
 * to a terminal layer (where the length of the path corresponds strictly to the pre-defined
 * number of variables). The solver expects a {@link org.ddolib.layered.modeling.Model} which internally uses a
 * {@link org.ddolib.layered.modeling.Problem} defining the number of variables.
 * </p>
 */
package org.ddolib.layered.examples;
