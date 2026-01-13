/**
 * This package implements the acs, astar and ddo models for the Maximum 2-Satisfiability Problem (MAX2SAT) Problem.
 * Given a logic formula in CNF whose clauses (of the formula comprises at most two literals)
 * have each been assigned a weight, the MAX2SAT problem consists in finding a variable assignment
 * that maximizes the total weight of the satisfied clauses.
 * This problem is considered in the papers:
 * <ul>
 *     <li><a href="https://link.springer.com/book/10.1007/978-3-319-42849-9">
 *      David Bergman et al. Decision Diagrams for Optimization. Ed. by Barry O’Sullivan and
 *      Michael Wooldridge. Springer, 2016.</a> </li>
 *      <li><a href="https://utoronto.scholaris.ca/server/api/core/bitstreams/450b3ef4-ff33-42de-9d14-b0322aff11a3/content">
 *              David Bergman et al. “Discrete Optimization with Decision Diagrams”.
 *              In: INFORMS Journal on Computing 28.1 (2016), pp. 47–66.</a></li>
 * </ul>
 */
package org.ddolib.examples.max2sat;