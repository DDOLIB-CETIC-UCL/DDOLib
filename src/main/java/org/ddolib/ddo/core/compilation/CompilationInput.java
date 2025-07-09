package org.ddolib.ddo.core.compilation;

import org.ddolib.ddo.algo.heuristics.VariableHeuristic;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.implem.dominance.DominanceChecker;
import org.ddolib.ddo.modeling.Problem;
import org.ddolib.ddo.modeling.Relaxation;
import org.ddolib.ddo.modeling.StateRanking;

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
 * @param dominance         The dominance checker used to prune the search space.
 * @param bestLB            The best known lower bound at the time when the dd is being compiled.
 * @param cutSetType        The type of cut set used in the compilation.
 * @param exportAsDot       Whether the compiled diagram have to be exported to a dot file.
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
                                     DominanceChecker<T, K> dominance,
                                     double bestLB,
                                     CutSetType cutSetType,
                                     boolean exportAsDot) {

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
