/**
 * This package implements the acs, astar and ddo models for the Minimum Sum Completion Time (MSCT).
 * The problem is to sequence n jobs such that:
 * - each job is scheduled after its release time
 * - the sum of completion time is minimized
 * This DP model is from :
 * <a href="https://drops.dagstuhl.de/storage/00lipics/lipics-vol340-cp2025/LIPIcs.CP.2025.5/LIPIcs.CP.2025.5.pdf">
 * J. Christopher Beck, Ryo Kuroiwa, Jimmy H. M. Lee, Peter J. Stuckey, Allen Z. Zhong:
 * Transition Dominance in Domain-Independent Dynamic Programming. CP 2025: 5:1-5:23</a>
 * In this model a state is represented by:
 * - the set of remaining jobs
 * - the current time (the end time of last sequenced job)
 */
package org.ddolib.examples.msct;
