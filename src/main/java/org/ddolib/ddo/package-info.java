/**
 * The classes in this package compose the framework you will be using to
 * complete your assignment on the Branch-and-Bound with MDD algorithm.
 * There are more classes that you strictly need, but it gives you an idea
 * of the kind of things you might want to try and experiment with.
 * <p>
 * The framework is structured as follows:
 * <ul>
 *
 *  <li>
 *      The {@code modeling} package contains interfaces and abstract classes to extend for
 *      implementing your optimization problem.
 *  </li>
 *
 *  <li>
 *      The {@code core} package comprises all the necessary abstractions (interfaces,
 *      data types,...) that you will want to manipulate when solving a problem
 *      with BaB + MDD.
 * <p>
 *      You should really think of the content of the core package as the base
 *      vocabulary you need to master to explain someone else the BaB+DD algo.
 *  </li>
 *
 *  <li>
 *      The {@code algo} package provides purely algorithmic components, including various solver and
 *      heuristics that can customize the MDD behavior.
 *  </li>
 *
 *  <li>
 *      The {@code api} package provides a user-friendly api, e.g. factory for solvers with
 *      default values.
 *  </li>
 *
 * </ul>
 */
package org.ddolib.ddo;
