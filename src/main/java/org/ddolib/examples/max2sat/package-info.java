/**
 * ******* Maximum 2-Satisfiability Problem (MAX2SAT) *******
 * Given a logic formula in CNF whose clauses (of the formula comprises at most two literals)
 * have each been assigned a weight, the MAX2SAT problem consists in finding a variable assignment
 * that maximizes the total weight of the satisfied clauses.
 * This problem is considered in the paper:
 *      - David Bergman et al. Decision Diagrams for Optimization. Ed. by Barry O’Sullivan and Michael Wooldridge. Springer, 2016.
 *      - David Bergman et al. “Discrete Optimization with Decision Diagrams”. In: INFORMS Journal on Computing 28.1 (2016), pp. 47–66.
 */

/**
 * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.examples.ddo.max2sat.Max2Sat"} in your terminal to execute
 * default instance. <br>
 * <p>
 * Run {@code mvn exec:java -Dexec.mainClass="oorg.ddolib.ddo.examples.max2sat.Max2Sat -Dexec.args="<your file>
 * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.
 */
package org.ddolib.examples.max2sat;