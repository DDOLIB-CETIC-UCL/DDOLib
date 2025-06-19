package org.ddolib.ddo.implem.solver;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.DominanceChecker;
import org.ddolib.ddo.implem.mdd.LinkedDecisionDiagram;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Solver that compile an unique exact mdd.
 * <p>
 * Note: By only using exact mdd, this solver can consume a lot of memory. It is advisable to use this solver to
 * test your model on small instances. See {@link SequentialSolver} or {@link ParallelSolver} for other use cases.
 *
 * @param <T> The type of states.
 * @param <K> The type of dominance keys.
 */
public final class ExactSolver<T, K> implements Solver {

    /**
     * The problem we want to maximize
     */
    private final Problem<T> problem;
    /**
     * A suitable relaxation for the problem we want to maximize
     */

    private final Relaxation<T> relax;
    /**
     * A heuristic to identify the most promising nodes
     */

    private final StateRanking<T> ranking;

    /**
     * A heuristic to choose the next variable to branch on when developing a DD
     */

    private final VariableHeuristic<T> varh;

    /**
     * Your implementation (just like the parallel version) will reuse the same
     * data structure to compile all mdds.
     * <p>
     * # Note:
     * This approach is recommended, however we do not force this design choice.
     * You might decide against reusing the same object over and over (even though
     * it has been designed to be reused). Should you decide to not reuse this
     * object, then you can simply ignore this field (and remove it altogether).
     */
    private final DecisionDiagram<T, K> mdd;
    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T, K> dominance;
    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;
    /**
     * Value of the best known lower bound.
     */
    private double bestLB;

    /**
     * Creates a new instance.
     *
     * @param problem The problem we want to maximize.
     * @param relax   A suitable relaxation for the problem we want to maximize
     * @param varh    A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking A heuristic to identify the most promising nodes.
     */
    public ExactSolver(final Problem<T> problem,
                       final Relaxation<T> relax,
                       final VariableHeuristic<T> varh,
                       final StateRanking<T> ranking,
                       final DominanceChecker<T, K> dominance) {
        this.problem = problem;
        this.relax = relax;
        this.ranking = ranking;
        this.varh = varh;
        this.dominance = dominance;
        this.mdd = new LinkedDecisionDiagram<>();
        this.bestLB = Integer.MIN_VALUE;
        this.bestSol = Optional.empty();
    }

    @Override
    public SearchStatistics maximize(int verbosityLevel) {
        SubProblem<T> root = new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Integer.MAX_VALUE,
                Collections.emptySet());

        CompilationInput<T, K> compilation = new CompilationInput<>(
                CompilationType.Exact,
                problem,
                relax,
                varh,
                ranking,
                root,
                Integer.MAX_VALUE,
                dominance,
                bestLB,
                CutSetType.LastExactLayer
        );
        mdd.compile(compilation);
        maybeUpdateBest(verbosityLevel);

        return new SearchStatistics(1, 1);
    }

    @Override
    public SearchStatistics maximize() {
        return maximize(0);
    }

    @Override
    public Optional<Double> bestValue() {
        if (bestSol.isPresent()) return Optional.of(bestLB);
        else return Optional.empty();
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    /**
     * This private method updates the best known node and lower bound in
     * case the best value of the current `mdd` expansion improves the current
     * bounds.
     */
    private void maybeUpdateBest(int verbosityLevel) {
        Optional<Double> ddval = mdd.bestValue();
        if (ddval.isPresent() && ddval.get() > bestLB) {
            bestLB = ddval.get();
            bestSol = mdd.bestSolution();
            if (verbosityLevel > 2) System.out.println("new best " + bestLB);
        }
    }
}
