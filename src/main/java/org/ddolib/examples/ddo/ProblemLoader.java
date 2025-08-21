package org.ddolib.examples.ddo;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;

/**
 *
 * @param problem the loaded problem
 * @param relax the relaxation operator defined for this problem
 * @param ranking the ranking defined for this problem
 * @param width the maximal width heuristic defined for this problem
 * @param varh the variable heuristic defined for this problem
 * @param fub the fast upper bound defined for this problem
 * @param dominance the dominance checker defined for this problem
 * @param distance the distance function defined for this problem
 * @param coordinates the coordinates function defined for this problem
 * @param <T> the type of state
 * @param <K> the type of key for the dominance checker
 */
public record ProblemLoader<T, K> (Problem<T> problem,
                                   Relaxation<T> relax,
                                   StateRanking<T> ranking,
                                   FixedWidth<T> width,
                                   VariableHeuristic<T> varh,
                                   FastUpperBound<T> fub,
                                   DominanceChecker<T, K> dominance,
                                   StateDistance<T> distance,
                                   StateCoordinates<T> coordinates) {}
