/**
 * This package contains the interfaces and abstract classes that must be implemented as problem
 * specific classes to model a problem with the no-layer API (no fixed number of variables,
 * termination detected via {@link org.ddolib.nolayer.modeling.Problem#isTarget(Object)}).
 * It also contains default implementations and the per-algorithm {@code *Model} bundles
 * ({@link org.ddolib.nolayer.modeling.DdoModel}, {@link org.ddolib.nolayer.modeling.AcsModel},
 * {@link org.ddolib.nolayer.modeling.AwAstarModel}) as well as the
 * {@link org.ddolib.nolayer.modeling.Solvers} facade used to run any of them.
 */
package org.ddolib.nolayer.modeling;
