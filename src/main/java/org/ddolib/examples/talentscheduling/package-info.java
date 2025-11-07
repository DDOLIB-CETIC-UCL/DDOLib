/**
 * This package implements the acs, astar and ddo models for the talent scheduling problem (tsp).
 * The talent scheduling problem (tsp) is define by the given of a set
 * S= {s1, s2, . . . , sn} of n scenes and A = {a1,a2, . . . , am} a
 * set of m actors. All scenes are assumed to be shot on a given location.
 * Each scene sj ∈ S requires a subset a(sj) ⊆ A of actors and has a duration d(sj)
 * that commonly consists of one or several days. Each actor ai
 * is required by a subset s(ai)⊆S of scenes. We denote by H the permutation
 * set of the n scenes and define ei(π) (respectively, li(π)) as the
 * earliest day (respectively, the latest day) in which actor i is required
 * to be present on location in the permutation π ∈ H. Each actor ai ∈
 * A has a daily wage c(ai) and is paid for each day from ei(π) to li(π)
 * regardless of whether he (or she) is required in the scenes. The objective of
 * the talent scheduling problem is to find a shooting sequence
 * (i.e.,a permutation π ∈ H) of all scenes that minimizes the total paid
 * wages.
 */
package org.ddolib.examples.talentscheduling;
