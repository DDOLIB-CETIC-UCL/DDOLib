/**
 * This package contains the implementation of the Maximum Independent Set Problem (MISP)
 * ***** The Maximum Independent Set Problem (MISP) *****
 * Given a weighted graph 𝐺 = (𝑉,𝐸,𝑤) where 𝑉= {1,...,𝑛}
 * is a set of vertices, 𝐸 \subset 𝑉 ×𝑉 the set of edges connecting those vertices and
 * 𝑤 = {𝑤1,𝑤2,...,𝑤𝑛} is a set of weights s.t. 𝑤𝑖 is the weight of node 𝑖.
 * The problem consists in finding a subset of vertices in a graph such that
 * no edge exists in the graph that connects two of the selected nodes and
 * the sum of the weight of the selected nodes is maximal.
 * This problem is considered in the paper:
 * - David Bergman et al. Decision Diagrams for Optimization. Ed. by Barry O’Sullivan and Michael Wooldridge. Springer, 2016.
 * - David Bergman et al. “Discrete Optimization with Decision Diagrams”. In: INFORMS Journal on Computing 28.1 (2016), pp. 47–66.
 * /**
 * <p>
 * <p>
 * /**
 * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.misp.MispMain"} in your terminal to execute
 * default instance. <br>
 * <p>
 * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.misp.MispMain -Dexec.args="<your file>
 * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.
 */
package org.ddolib.examples.misp;
