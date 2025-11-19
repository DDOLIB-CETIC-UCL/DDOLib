package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.compilation.CompilationConfig;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.mdd.DecisionDiagram;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.modeling.*;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.verbosity.VerboseMode;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * A solver that compile one relaxed MDD from the root node.
 *
 * @param <T> The type representing a problem state.
 * @see ExactSolver
 * @see DdoModel
 * @see Problem
 * @see Relaxation
 * @see StateRanking
 * @see VariableHeuristic
 * @see WidthHeuristic
 * @see FastLowerBound
 * @see DominanceChecker
 */
public final class RelaxationSolver<T> implements Solver {
    /**
     * The problem we want to minimize
     */
    private final Problem<T> problem;
    /**
     * A suitable relaxation for the problem we want to minimize
     */
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

    /**
     * Set of nodes that must still be explored before
     * the problem can be considered 'solved'.
     * <p>
     * # Note:
     * This fringe orders the nodes by lower bound (so the lowest lower bound is going
     * to pop first). So, it is guaranteed that the lower-bound of the first
     * node being popped is a lower bound on the value reachable by exploring
     * any of the nodes remaining on the fringe. As a consequence, the
     * exploration can be stopped as soon as a node with an lb &#8804; current best
     * lower bound is popped.
     */
    private final Frontier<T> frontier;
    /**
     * The heuristic defining a very rough estimation (lower bound) of the optimal value.
     */
    private final FastLowerBound<T> flb;
    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T> dominance;


    /**
     * <ul>
     *     <li>0: no verbosity</li>
     *     <li>1: display newBest whenever there is a newBest</li>
     *     <li>2: 1 + statistics about the front every half a second (or so)</li>
     *     <li>3: 2 + every developed sub-problem</li>
     *     <li>4: 3 + details about the developed state</li>
     * </ul>
     * <p>
     * <p>
     * 3: 2 + every developed sub-problem
     * 4: 3 + details about the developed state
     */
    private final VerbosityLevel verbosityLevel;

    private final VerboseMode verboseMode;
    /**
     * Whether we want to export the first explored restricted and relaxed mdd.
     */
    private final boolean exportAsDot;
    /**
     * The debug level of the compilation to add additional checks (see
     * {@link DebugLevel for details}
     */
    private final DebugLevel debugLevel;
    /**
     * Value of the best known upper bound.
     */
    private double bestUB;
    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    /**
     * Strategy to select which nodes should be merged together on a relaxed DD.
     */
    private final ReductionStrategy<T> relaxStrategy;

    /**
     * Creates a fully qualified instance. The parameters of this solver are given via a
     * {@link DdoModel}
     *
     * @param model All the parameters needed to configure the solver.
     */
    public RelaxationSolver(DdoModel<T> model) {
        this.problem = model.problem();
        this.relax = model.relaxation();
        this.varh = model.variableHeuristic();
        this.ranking = model.ranking();
        this.width = model.widthHeuristic();
        this.flb = model.lowerBound();
        this.dominance = model.dominance();
        this.frontier = model.frontier();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.verbosityLevel = model.verbosityLevel();
        this.verboseMode = new VerboseMode(verbosityLevel, 500L);
        this.exportAsDot = model.exportDot();
        this.debugLevel = model.debugMode();
        this.relaxStrategy = model.relaxStrategy();
    }

    @Override
    public SearchStatistics minimize(Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        long start = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        nbIter++;
        verboseMode.detailedSearchState(nbIter, frontier.size(), bestUB,
                frontier.bestInFrontier(), gap());

        SubProblem<T> sub = root();
        int maxWidth = width.maximumWidth(sub.getState());
        CompilationConfig<T> compilation = new CompilationConfig<>();
        compilation.compilationType = CompilationType.Relaxed;
        compilation.problem = this.problem;
        compilation.relaxation = this.relax;
        compilation.variableHeuristic = this.varh;
        compilation.stateRanking = this.ranking;
        compilation.residual = sub;
        compilation.maxWidth = maxWidth;
        compilation.flb = flb;
        compilation.dominance = this.dominance;
        compilation.bestUB = this.bestUB;
        compilation.cutSetType = frontier.cutSetType();
        compilation.exportAsDot = this.exportAsDot;
        compilation.debugLevel = this.debugLevel;
        compilation.reductionStrategy = relaxStrategy;

        DecisionDiagram<T> relaxedMdd = new LinkedDecisionDiagram<>(compilation);
        relaxedMdd.compile();
        maybeUpdateBest(relaxedMdd, exportAsDot);

        return new SearchStatistics(SearchStatus.SAT, nbIter, queueMaxSize, System.currentTimeMillis() - start, bestUB, gap());
    }

    @Override
    public Optional<Double> bestValue() {
        if (bestSol.isPresent()) {
            return Optional.of(bestUB);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    /**
     * @return the root subproblem
     */
    private SubProblem<T> root() {
        return new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Double.NEGATIVE_INFINITY,
                Collections.emptySet());
    }

    /**
     * Updates the best known node and upper bound in
     * case the best value of the current `mdd` expansion improves the current
     * bounds.
     */
    private void maybeUpdateBest(DecisionDiagram<T> currentMdd, boolean exportDot) {
        Optional<Double> ddval = currentMdd.bestValue();
        if (ddval.isPresent() && ddval.get() < bestUB) {
            bestUB = ddval.get();
            bestSol = currentMdd.bestSolution();
            verboseMode.newBest(bestUB);
        } else if (exportDot) {
            currentMdd.exportAsDot(); // to be sure to update the color of the edges.
        }
    }

    private double gap() {
        if (frontier.isEmpty()) {
            return 100.0;
        } else {
            double bestInFrontier = frontier.bestInFrontier();
            return Math.abs(100 * (Math.abs(bestUB) - Math.abs(bestInFrontier)) / bestUB);
        }
    }
}
