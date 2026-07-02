/**
 * This package implements the acs, astar and ddo models for the Maximum Independent Set Problem (MISP).
 * Given a weighted graph 𝐺 = (𝑉,𝐸,𝑤) where 𝑉= {1,...,𝑛}
 * is a set of vertices, 𝐸 \subset 𝑉 ×𝑉 the set of edges connecting those vertices and
 * 𝑤 = {𝑤1,𝑤2,...,𝑤𝑛} is a set of weights s.t. 𝑤𝑖 is the weight of node 𝑖.
 * The problem consists in finding a subset of vertices in a graph such that
 * no edge exists in the graph that connects two of the selected nodes and
 * the sum of the weight of the selected nodes is maximal.
 * <ul>
 *     <li><a href="https://link.springer.com/book/10.1007/978-3-319-42849-9">
 *      David Bergman et al. Decision Diagrams for Optimization. Ed. by Barry O’Sullivan and
 *      Michael Wooldridge. Springer, 2016.</a> </li>
 *      <li><a href="https://utoronto.scholaris.ca/server/api/core/bitstreams/450b3ef4-ff33-42de-9d14-b0322aff11a3/content">
 *              David Bergman et al. “Discrete Optimization with Decision Diagrams”.
 *              In: INFORMS Journal on Computing 28.1 (2016), pp. 47–66.</a></li>
 * </ul>
 */
package org.ddolib.layered.examples.misp;
