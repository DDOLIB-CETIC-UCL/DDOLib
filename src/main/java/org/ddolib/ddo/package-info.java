/**
 * The classes in this package compose the framework you will be using to
 * complete your assignment on the Branch-and-Bound with MDD algorithm.
 * There are more classes that you strictly need, but it gives you an idea
 * of the kind of things you might want to try and experiment with.
 * 
 * The framework is structured as follows: 
 * - the `core` package comprises all the necessary abstractions (interfaces,
 *   data types,..) that you will want to manipulate when solving a problem
 *   with BaB + MDD. 
 * 
 *   # Note: 
 *   For most these are **pure** abstractions, which means that this
 *   package mostly consists of interfaces that must be instantiated through
 *   a concrete class.
 *   
 *   You should really think of the content of the core package as the base
 *   vocabulary you need to master to explain someone else the BaB+DD algo.
 * 
 * - the `heuristic` package comprises the abstractions of the various
 *   strategies that can (in some cases must) be implemented in order to 
 *   work with a BaB+DD solver, but are not strictly necessary if you were
 *   to simply explain the approach to someone else.
 * 
 * - the `implem` package -- as the indicated by the name, provides the 
 *   implementation of classes which you will want to use in order to 
 *   instantiate the abstractions described in the other two packages.
 */
package org.ddolib.ddo;
