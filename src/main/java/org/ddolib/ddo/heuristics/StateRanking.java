package org.ddolib.ddo.heuristics;

import java.util.Comparator;

/** 
 * A state ranking is used to order the states and decides the ones that are kept
 * and the ones that are merged/deleted when a relaxation/restriction occurs. 
 * 
 * In this context, a state ranking is nothing but an ordering on the states 
 * which is defined in the form of a comparator. The solvers and MDD should
 * interpret compare(a, b) > 0 as a should have a higher chance of being kept
 * intact while b should have a higher chance of being merged.
 */
public interface StateRanking<T> extends Comparator<T> {}
