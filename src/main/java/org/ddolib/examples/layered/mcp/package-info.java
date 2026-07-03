/**
 * This package implements the acs, astar and ddo models for the Maximum Cut Problem (MCP).
 * Given an undirected weighted graph 𝐺 = (𝑉,𝐸) in which the weight of
 * the edge (𝑖,𝑗) ∈ 𝐸 is denoted 𝑤_{i,j} the MCP consists in finding a bi-partition (𝑆,𝑇)
 * of the vertices of some given graph that maximizes the total weight of edges whose endpoints are in different partitions.
 * This problem is considered in the papers:
 * <ul>
 *     <li><a href="https://link.springer.com/book/10.1007/978-3-319-42849-9">
 *      David Bergman et al. Decision Diagrams for Optimization. Ed. by Barry O’Sullivan and
 *      Michael Wooldridge. Springer, 2016.</a> </li>
 *      <li><a href="https://utoronto.scholaris.ca/server/api/core/bitstreams/450b3ef4-ff33-42de-9d14-b0322aff11a3/content">
 *              David Bergman et al. “Discrete Optimization with Decision Diagrams”.
 *              In: INFORMS Journal on Computing 28.1 (2016), pp. 47–66.</a></li>
 * </ul>
 *
 */
package org.ddolib.examples.layered.mcp;
