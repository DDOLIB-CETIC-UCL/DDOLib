/**
 * This package contains the classes and interfaces defining the heuristics used to discard or
 * merge nodes within a layer of a layered decision diagram during restriction or relaxation.
 * {@link org.ddolib.layered.solving.ddo.core.heuristics.cluster.ReductionStrategy} is the common
 * interface, implemented by the classic cost-based strategy
 * ({@link org.ddolib.layered.solving.ddo.core.heuristics.cluster.CostBased}), a random strategy
 * ({@link org.ddolib.layered.solving.ddo.core.heuristics.cluster.RandomBased}), a distance-based
 * clustering approach ({@link org.ddolib.layered.solving.ddo.core.heuristics.cluster.GHP}, using
 * {@link org.ddolib.layered.solving.ddo.core.heuristics.cluster.StateDistance}), and a hybridization
 * of the cost-based and clustering strategies
 * ({@link org.ddolib.layered.solving.ddo.core.heuristics.cluster.Hybrid}).
 */
package org.ddolib.layered.solving.ddo.core.heuristics.cluster;
