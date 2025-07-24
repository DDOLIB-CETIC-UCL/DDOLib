package org.ddolib.ddo.core.solver;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.compilation.CompilationInput;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.mdd.DecisionDiagram;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.Random;

/**
Solver that solve only a relaxed MDD from the root node
 */
public final class RelaxationSolver<T, K> implements Solver {
    /**
     * The problem we want to maximize
     */
    private final Problem<T> problem;
    /** A suitable relaxation for the problem we want to maximize */
    private final Relaxation<T> relax;
    /**
     * A heuristic to identify the most promising nodes
     */
    private final StateRanking<T> ranking;
    /**
     * A heuristic to choose the maximum width of the DD you compile
     */
    private final WidthHeuristic<T> width;
    /**
     * A heuristic to choose the next variable to branch on when developing a DD
     */
    private final VariableHeuristic<T> varh;
    private final ClusterStrat relaxStrat;

    /**
     * The heuristic defining a very rough estimation (upper bound) of the optimal value.
     */
    private final FastUpperBound<T> fub;

    /**
     * Set of nodes that must still be explored before
     * the problem can be considered 'solved'.
     * <p>
     * # Note:
     * This fringe orders the nodes by upper bound (so the highest ub is going
     * to pop first). So, it is guaranteed that the upper bound of the first
     * node being popped is an upper bound on the value reachable by exploring
     * any of the nodes remaining on the fringe. As a consequence, the
     * exploration can be stopped as soon as a node with an ub &#8804; current best
     * lower bound is popped.
     */
    private final Frontier<T> frontier;
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

    private final StateDistance<T> distance;
    private final StateCoordinates<T> coord;

    private int bestUB;
    private long startTime;
    public double timeForBest;
    private final Random rnd;

    /**
     * Value of the best known lower bound.
     */
    private double bestLB;
    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T, K> dominance;

    /**
     * Only the first restricted mdd can be exported to a .dot file
     */
    private boolean firstRestricted = true;
    /**
     * Only the first relaxed mdd can be exported to a .dot file
     */
    private boolean firstRelaxed = true;

    /** Creates a fully qualified instance */
    public RelaxationSolver(
            final Problem<T> problem,
            final Relaxation<T> relax,
            final VariableHeuristic<T> varh,
            final StateRanking<T> ranking,
            final WidthHeuristic<T> width,
            final Frontier<T> frontier,
            final FastUpperBound<T> fub,
            final DominanceChecker<T, K> dominance,
            final ClusterStrat relaxStrat,
            final StateDistance<T> distance,
            final StateCoordinates<T> coord,
            final int seed)
    {
        this.relaxStrat = relaxStrat;
        this.problem = problem;
        this.relax   = relax;
        this.varh    = varh;
        this.ranking = ranking;
        this.distance = distance;
        this.fub = fub;
        this.coord = coord;
        this.width   = width;
        this.frontier= frontier;
        this.mdd     = new LinkedDecisionDiagram<>();
        this.bestLB  = Integer.MIN_VALUE;
        this.bestSol = Optional.empty();
        this.dominance = dominance;
        rnd = new Random(seed);
    }

    @Override
    public SearchStatistics maximize() {
        return maximize(0, false);
    }

    @Override
    public SearchStatistics maximize(int verbosityLevel, boolean exportAsDot) {
        long start = System.currentTimeMillis();
        SubProblem<T> sub = root();
        int maxWidth = width.maximumWidth(sub.getState());

        CompilationInput<T, K> compilation = new CompilationInput<>(
                CompilationType.Relaxed,
                problem,
                relax,
                varh,
                ranking,
                sub,
                maxWidth,
                fub,
                dominance,
                bestLB,
                frontier.cutSetType(),
                exportAsDot,
                relaxStrat,
                null,
                distance,
                coord,
                rnd
        );
        mdd.compile(compilation);
        maybeUpdateBest(verbosityLevel, exportAsDot);
        long end = System.currentTimeMillis();
        return new SearchStatistics(1, 0, end-start);
    }

    public boolean isExact() {
        return mdd.isExact();
    }

    @Override
    public Optional<Double> bestValue() {
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
    private void maybeUpdateBest(int verbosityLevel, boolean exportAsDot) {
        Optional<Double> ddval = mdd.bestValue();
        if (ddval.isPresent() && ddval.get() > bestLB) {
            bestLB = ddval.get();
            bestSol = mdd.bestSolution();
            if (verbosityLevel >= 1) System.out.println("new best: " + bestLB);
        } else if (exportAsDot) {
            mdd.exportAsDot(); // to be sure to update the color of the edges.
        }
    }
}
