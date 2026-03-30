package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.*;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.compilation.CompilationConfig;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
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
public final class RestrictionSolver<T> {
    /**
     * The problem we want to minimize
     */
    private final Problem<T> problem;
    /**
     * A heuristic to choose the maximum width of the DD you compile
     */
    private final WidthHeuristic<T> width;


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
     * Value of the best known upper bound.
     */
    private double bestUB;
    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    private final DdoModel<T> model;

    /**
     * Creates a fully qualified instance. The parameters of this solver are given via a
     * {@link DdoModel}
     *
     * @param model All the parameters needed to configure the solver.
     */
    public RestrictionSolver(DdoModel<T> model) {
        this.problem = model.problem();
        this.width = model.widthHeuristic();
        this.frontier = model.frontier();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.verbosityLevel = model.verbosityLevel();
        this.verboseMode = new VerboseMode(verbosityLevel, 500L);
        this.exportAsDot = model.exportDot();
        this.dominance = model.dominance();
        this.model = model;
    }

    public Solution minimize(Predicate<SearchStatistics> limit,
                                             BiConsumer<int[], SearchStatistics> onSolution) {
        long start = System.currentTimeMillis();

        SubProblem<T> sub = root();
        int maxWidth = width.maximumWidth(sub.getState());
        CompilationConfig<T> compilation = configureCompilation(sub, maxWidth, model.exportDot());

        LinkedDecisionDiagram<T> restrictedMdd = new LinkedDecisionDiagram<>(compilation);
        restrictedMdd.compile();
        maybeUpdateBest(restrictedMdd, exportAsDot);

        if (!restrictedMdd.bestValue().isPresent()) {
            System.out.println(restrictedMdd.bestSolution());
            System.exit(0);
        }

        long end = System.currentTimeMillis();
        SearchStatistics stats = new SearchStatistics(SearchStatus.OPTIMAL, 0, 0,
                end - start, bestUB, 0);
        return new Solution(bestSolution(), stats);
        // return new SearchStatistics(SearchStatus.SAT, nbIter, queueMaxSize, System.currentTimeMillis() - start, bestUB, gap());
    }

    public Optional<Double> bestValue() {
        if (bestSol.isPresent()) {
            return Optional.of(bestUB);
        } else {
            return Optional.empty();
        }
    }

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

    /**
     * Initialize the parameters of a compilation.
     *
     * @param sub         the root of the current sub-problem
     * @param maxWidth    the max width of the diagram
     * @param exportAsDot whether the diagram has to be exported as .dot file.
     * @return the parameters of the compilation
     */
    private CompilationConfig<T> configureCompilation(SubProblem<T> sub,
                                                      int maxWidth, boolean exportAsDot) {
        CompilationConfig<T> compilation = new CompilationConfig<>(model);
        compilation.compilationType = CompilationType.Restricted;
        compilation.problem = model.problem();
        compilation.relaxation = model.relaxation();
        compilation.variableHeuristic = model.variableHeuristic();
        compilation.stateRanking = model.ranking();
        compilation.residual = sub;
        compilation.maxWidth = maxWidth;
        compilation.flb = model.lowerBound();
        compilation.dominance = this.dominance;
        compilation.bestUB = this.bestUB;
        compilation.cutSetType = frontier.cutSetType();
        compilation.exportAsDot = exportAsDot;
        compilation.debugLevel = model.debugMode();
        compilation.reductionStrategy = model.restrictStrategy();

        return compilation;
    }
}
