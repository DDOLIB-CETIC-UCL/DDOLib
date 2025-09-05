package org.ddolib.ddo.core.compilation;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;

import java.util.Optional;

/**
 * The set of parameters used to tweak the compilation of an MDD
 *
 * @param compilationType   How is the mdd being compiled.
 * @param problem           A reference to the original problem to solve.
 * @param relaxation        The relaxation used to merge nodes in a relaxed mdd.
 * @param variableHeuristic The variable heuristic used to decide the variable to branch on next.
 * @param stateRanking      The state ranking heuristic to choose the nodes to keep and those to discard.
 * @param residual          The subproblem whose state space must be explored.
 * @param maxWidth          The maximum width of the mdd.
 * @param fub               The heuristic defining a very rough estimation (upper bound) of the optimal value.
 * @param dominance         The dominance checker used to prune the search space.
 * @param bestLB            The best known lower bound at the time when the dd is being compiled.
 * @param cutSetType        The type of cut set used in the compilation.
 * @param exportAsDot       Whether the compiled diagram have to be exported to a dot file.
 * @param debugLevel        The debug level of the compilation to add additional checks (see
 *                          {@link org.ddolib.common.solver.SolverConfig for details}
 * @param <T>               The type used to model the state of your problem.
 * @param <K>               The type of the dominance key.
 */
public record CompilationInput<T, K>(CompilationType compilationType,
                                     Problem<T> problem,
                                     Relaxation<T> relaxation,
                                     VariableHeuristic<T> variableHeuristic,
                                     StateRanking<T> stateRanking,
                                     SubProblem<T> residual,
                                     int maxWidth,
                                     FastUpperBound<T> fub,
                                     DominanceChecker<T, K> dominance,
                                     Optional<SimpleCache<T>> cache,
                                     double bestLB,
                                     CutSetType cutSetType,
                                     boolean exportAsDot,
                                     int debugLevel) {

    /**
     * Returns a string representation of this record class.
     *
     * @return Returns a string representation of this record class.
     */
    @Override
    public String toString() {
        return String.format("Compilation: %s - Sub problem: %s - bestLB: %f", compilationType, residual, bestLB);
    }
}
