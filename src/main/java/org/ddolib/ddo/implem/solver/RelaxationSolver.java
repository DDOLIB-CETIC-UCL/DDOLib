package org.ddolib.ddo.implem.solver;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.heuristics.WidthHeuristic;
import org.ddolib.ddo.implem.mdd.LinkedDecisionDiagram;

import java.util.*;

/**
Solver that solve only a relaxed MDD from the root node
 */
public final class RelaxationSolver<T> implements Solver {
    private final RelaxationType relaxType;
    /** The problem we want to maximize */
    private final Problem<T> problem;
    /** A suitable relaxation for the problem we want to maximize */
    private final Relaxation<T> relax;
    /** A heuristic to identify the most promising nodes */
    private final StateRanking<T> ranking;
    /** A heuristic to choose the maximum width of the DD you compile */
    private final WidthHeuristic<T> width;
    /** A heuristic to choose the next variable to branch on when developing a DD */
    private final VariableHeuristic<T> varh;

    /**
     * This is the fringe: the set of nodes that must still be explored before
     * the problem can be considered 'solved'.
     *
     * # Note:
     * This fringe orders the nodes by upper bound (so the highest ub is going
     * to pop first). So, it is guaranteed that the upper bound of the first
     * node being popped is an upper bound on the value reachable by exploring
     * any of the nodes remaining on the fringe. As a consequence, the
     * exploration can be stopped as soon as a node with an ub <= current best
     * lower bound is popped.
     */
    private final Frontier<T> frontier;
    /**
     * Your implementation (just like the parallel version) will reuse the same
     * data structure to compile all mdds.
     *
     * # Note:
     * This approach is recommended, however we do not force this design choice.
     * You might decide against reusing the same object over and over (even though
     * it has been designed to be reused). Should you decide to not reuse this
     * object, then you can simply ignore this field (and remove it altogether).
     */
    private final DecisionDiagram<T> mdd;

    /** This is the value of the best known lower bound. */
    private int bestLB;

    /** If set, this keeps the info about the best solution so far. */
    private Optional<Set<Decision>> bestSol;

    /** Creates a fully qualified instance */
    public RelaxationSolver(
            final RelaxationType relaxType,
            final Problem<T> problem,
            final Relaxation<T> relax,
            final VariableHeuristic<T> varh,
            final StateRanking<T> ranking,
            final WidthHeuristic<T> width,
            final Frontier<T> frontier)
    {
        this.relaxType = relaxType;
        this.problem = problem;
        this.relax   = relax;
        this.varh    = varh;
        this.ranking = ranking;
        this.width   = width;
        this.frontier= frontier;
        this.mdd     = new LinkedDecisionDiagram<>();
        this.bestLB  = Integer.MIN_VALUE;
        this.bestSol = Optional.empty();
    }

    /** Creates a fully qualified instance */
    public RelaxationSolver(
            final Problem<T> problem,
            final Relaxation<T> relax,
            final VariableHeuristic<T> varh,
            final StateRanking<T> ranking,
            final WidthHeuristic<T> width,
            final Frontier<T> frontier)
    {
        this(RelaxationType.Cost, problem, relax, varh, ranking, width, frontier);
    }

    @Override
    public SearchStatistics maximize() {

        SubProblem<T> sub = root();
        int maxWidth = width.maximumWidth(sub.getState());

        CompilationInput<T> compilation = new CompilationInput<>(
                relaxType,
                CompilationType.Relaxed,
                problem,
                relax,
                varh,
                ranking,
                sub,
                maxWidth,
                //
                bestLB
        );
        mdd.compile(compilation);
        maybeUpdateBest();
        return new SearchStatistics(1, 0);
    }

    @Override
    public Optional<Integer> bestValue() {
        if (bestSol.isPresent()) {
            return Optional.of(bestLB);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    /** @return the root subproblem */
    private SubProblem<T> root() {
        return new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Integer.MAX_VALUE,
                Collections.emptySet());
    }

    /**
     * This private method updates the best known node and lower bound in
     * case the best value of the current `mdd` expansion improves the current
     * bounds.
     */
    private void maybeUpdateBest() {
        Optional<Integer> ddval = mdd.bestValue();
        if (ddval.isPresent() && ddval.get() > bestLB) {
            System.out.println("New Best Value: " + ddval.get());
            bestLB = ddval.get();
            bestSol = mdd.bestSolution();
        }
    }
}
