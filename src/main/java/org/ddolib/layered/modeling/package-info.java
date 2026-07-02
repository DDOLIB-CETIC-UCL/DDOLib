/**
 * This package contains the interfaces and abstract classes that must be implemented as problem
 * specific classes to model a problem with the layered API (fixed, known number of variables).
 * It also contains default implementations and the per-algorithm {@code *Model} bundles
 * ({@link org.ddolib.layered.modeling.DdoModel}, {@link org.ddolib.layered.modeling.AcsModel},
 * {@link org.ddolib.layered.modeling.AwAstarModel}, {@link org.ddolib.layered.modeling.ExactModel},
 * {@link org.ddolib.layered.modeling.LnsModel}) as well as the {@link org.ddolib.layered.modeling.Solvers}
 * facade used to run any of them.
 */
package org.ddolib.layered.modeling;
