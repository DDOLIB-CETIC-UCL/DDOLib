package org.ddolib.ddo.core;

import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;

/**
 * The set of parameters used to tweak the compilation of a MDD
 *
 * @param <T> The type used to model the state of your problem
 */
public final class CompilationInput<T> {
    /**
     * How is the mdd being compiled ?
     */
    final CompilationType compType;
    /**
     * A reference to the original problem we try to maximize
     */
    final Problem<T> problem;
    /**
     * The relaxation which we use to merge nodes in a relaxed dd
     */
    final Relaxation<T> relaxation;
    /**
     * The variable heuristic which is used to decide the variable to branch on next
     */
    final VariableHeuristic<T> var;
    /**
     * The state ranking heuristic to choose the nodes to keep and those to discard
     */
    final StateRanking<T> ranking;
    /**
     * The subproblem whose state space must be explored
     */
    final SubProblem<T> residual;
    /**
     * What is the maximum width of the mdd ?
     */
    final int maxWidth;
    /**
     * The best known lower bound at the time when the dd is being compiled
     */
    final int bestLB;

    /**
     * Creates the inputs to parameterize the compilation of an MDD
     */
    public CompilationInput(
            final CompilationType compType,
            final Problem<T> problem,
            final Relaxation<T> relaxation,
            final VariableHeuristic<T> var,
            final StateRanking<T> ranking,
            final SubProblem<T> residual,
            final int maxWidth,
            final int bestLB
    ) {
        this.compType = compType;
        this.problem = problem;
        this.relaxation = relaxation;
        this.var = var;
        this.ranking = ranking;
        this.residual = residual;
        this.maxWidth = maxWidth;
        this.bestLB = bestLB;
    }

    /**
     * @return how is the dd being compiled ?
     */
    public CompilationType getCompilationType() {
        return compType;
    }

    /**
     * @return the problem we try to maximize
     */
    public Problem<T> getProblem() {
        return problem;
    }

    /**
     * @return the relaxation of the problem
     */
    public Relaxation<T> getRelaxation() {
        return relaxation;
    }

    /**
     * @return an heuristic to pick the least promising nodes
     */
    public VariableHeuristic<T> getVariableHeuristic() {
        return var;
    }

    /**
     * @return an heuristic to pick the least promising nodes
     */
    public StateRanking<T> getStateRanking() {
        return ranking;
    }

    /**
     * @return the subproblem that will be compiled into a dd
     */
    public SubProblem<T> getResidual() {
        return residual;
    }

    /**
     * @return the maximum with allowed for any layer in the decision diagram
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * @return best known lower bound at the time when the dd is being compiled
     */
    public int getBestLB() {
        return bestLB;
    }

    @Override
    public String toString() {
        return String.format("Compilation: %s - Sub problem: %s - bestLB: %d", compType, residual, bestLB);
    }
}
