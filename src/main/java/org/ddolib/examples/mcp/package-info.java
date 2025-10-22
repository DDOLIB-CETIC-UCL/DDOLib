/**
 * This package implements the acs, astar and ddo models for the Maximum Cut Problem (MCP).
 * Given an undirected weighted graph 𝐺 = (𝑉,𝐸) in which the weight of
 * the edge (𝑖,𝑗) ∈ 𝐸 is denoted 𝑤_{i,j} the MCP consists in finding a bi-partition (𝑆,𝑇)
 * of the vertices of some given graph that maximizes the total weight of edges whose endpoints are in different partitions.
 * This problem is considered in the paper:
 * - David Bergman et al. Decision Diagrams for Optimization. Ed. by Barry O’Sullivan and Michael Wooldridge. Springer, 2016.
 * - David Bergman et al. “Discrete Optimization with Decision Diagrams”. In: INFORMS Journal on Computing 28.1 (2016), pp. 47–66.
 *
 */
package org.ddolib.examples.mcp;
