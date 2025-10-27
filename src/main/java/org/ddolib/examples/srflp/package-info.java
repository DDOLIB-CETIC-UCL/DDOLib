/**
 * This package implements the acs, astar and ddo models for the Single Row Facility Layout Problem (SRFLP).
 * For more details and this problem and its model see
 * <a href= https://drops.dagstuhl.de/storage/00lipics/lipics-vol235-cp2022/LIPIcs.CP.2022.14/LIPIcs.CP.2022.14.pdf>
 * Coppé, V., Gillard, X., and Schaus, P. (2022).
 * Solving the constrained single-row facility layout problem with decision diagrams.
 * In 28th International Conference on Principles and Practice of Constraint Programming (CP 2022) (pp. 14-1).
 * Schloss Dagstuhl–Leibniz-Zentrum für Informatik.
 * </a>
 * <p>
 * In this model:
 * <ul>
 *     <li>Each variable/layer represent the position of the next department to be placed.</li>
 *     <li>The domain of each variable is the set of the remaining not placed department.</li>
 * </ul>
 */
package org.ddolib.examples.srflp;