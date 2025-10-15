/**
 * ################ Minimum Sum Completion Time (MSCT) #####################
 * The problem is to sequence n jobs such that:
 * - each job is scheduled after its release time
 * - the sum of completion time is minimized
 * This DP model is from :
 * 	J. Christopher Beck, Ryo Kuroiwa, Jimmy H. M. Lee, Peter J. Stuckey, Allen Z. Zhong:
 * Transition Dominance in Domain-Independent Dynamic Programming. CP 2025: 5:1-5:23
 * In this model a state is represented by:
 * - the set of remaining jobs
 * - the current time (the end time of last sequenced job)
 */
package org.ddolib.examples.msct;
