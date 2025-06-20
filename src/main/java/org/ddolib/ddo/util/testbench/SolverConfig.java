package org.ddolib.ddo.util.testbench;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Relaxation;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.heuristics.WidthHeuristic;
import org.ddolib.ddo.implem.dominance.DominanceChecker;

/**
 * Class containing the input of {@link org.ddolib.ddo.implem.solver.SequentialSolver}.
 *
 * @param relax     A suitable relaxation for the problem we want to maximize.
 * @param varh      A heuristic to choose the next variable to branch on when developing a DD.
 * @param ranking   A heuristic to identify the most promising nodes.
 * @param width     A heuristic to choose the maximum width of the DD you compile.
 * @param frontier  The set of nodes that must still be explored before
 *                  the problem can be considered 'solved'.
 *                  <p>
 *                  # Note:
 *                  This fringe orders the nodes by upper bound (so the highest ub is going
 *                  to pop first). So, it is guaranteed that the upper bound of the first
 *                  node being popped is an upper bound on the value reachable by exploring
 *                  any of the nodes remaining on the fringe. As a consequence, the
 *                  exploration can be stopped as soon as a node with an ub &#8804; current best
 *                  lower bound is popped.
 * @param dominance The dominance object that will be used to prune the search space.
 * @param <T>       The type of the states.
 * @param <K>       The type of the dominance keys.
 */
public record SolverConfig<T, K>(
        Relaxation<T> relax,
        VariableHeuristic<T> varh,
        StateRanking<T> ranking,
        WidthHeuristic<T> width,
        Frontier<T> frontier,
        DominanceChecker<T, K> dominance) {
}
