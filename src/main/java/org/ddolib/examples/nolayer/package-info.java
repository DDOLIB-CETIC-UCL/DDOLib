/**
 * This package contains models formulated using the unlayered (NoLayer)
 * modeling API.
 * <p>
 * In the unlayered modeling API, the problem is not defined by a fixed number
 * of variables.
 * Instead of exploring layer by layer up to a strict bound, the solver
 * transitions from
 * state to state until a problem-specific
 * {@link org.ddolib.modeling.nolayer.Problem#isGoal(Object)}
 * goal test is met. This offers more flexibility for models that naturally do
 * not have a uniform
 * path length, such as shortest paths problem in a graph.
 * </p>
 */
package org.ddolib.examples.nolayer;
