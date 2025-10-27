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
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.mdd.DecisionDiagram;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.modeling.*;
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
 * A sequential implementation of a Branch-and-Bound solver based on
 * Multi-valued Decision Diagrams (MDDs).
 *
 * <p>
 * This solver follows the vanilla version of the algorithm introduced in lecture.
 * It explores subproblems sequentially (one at a time), combining restricted and
 * relaxed MDD compilations to progressively tighten the upper and lower bounds
 * of the optimization problem.
 * </p>
 *
 * <p>
 * The solver maintains a frontier (priority queue) of unexplored subproblems,
 * ordered by their lower bounds, and stops as soon as the optimality condition
 * is met (i.e., when the best known upper bound is equal to the minimum lower
 * bound in the frontier).
 * </p>
 *
 * <p>
 * <b>Key ideas:</b>
 * </p>
 * <ul>
 *   <li>Each subproblem corresponds to a partial decision sequence and a state.</li>
 *   <li>The solver compiles a restricted MDD to approximate the feasible region
 *       and update the current best solution (upper bound).</li>
 *   <li>It then compiles a relaxed MDD to estimate lower bounds and determine
 *       which subproblems should be explored next.</li>
 *   <li>Subproblems are pruned if their lower bounds exceed the current best
 *       solution (upper bound), or if they are dominated according to the provided
 *       {@link DominanceChecker}.</li>
 * </ul>
 *
 *
 * <p>
 * This sequential solver serves as a reference or baseline implementation.
 * </p>
 *
 * <p><b>Usage Notes:</b></p>
 * <ul>
 *   <li>This solver is designed for correctness and clarity, not scalability.</li>
 *   <li>It is best suited for debugging, small instances, and educational purposes.</li>
 *   <li>Set the verbosity level in {@link DdoModel} for detailed runtime logging.</li>
 * </ul>
 *
 * @param <T> The type representing a problem state.
 *
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
public final class SequentialSolver<T> implements Solver {
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
     * {@link org.ddolib.modeling.DebugLevel for details}
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
     * This is the cache used to prune the search tree
     */
    private Optional<SimpleCache<T>> cache;
    /**
     * Only the first restricted mdd can be exported to a .dot file
     */
    private boolean firstRestricted = true;
    /**
     * Only the first relaxed mdd can be exported to a .dot file
     */
    private boolean firstRelaxed = true;


    /**
     * Creates a fully qualified instance. The parameters of this solver are given via a
     * {@link DdoModel}
     *
     * @param model All the parameters needed to configure the solver.
     */
    public SequentialSolver(DdoModel<T> model) {
        this.problem = model.problem();
        this.relax = model.relaxation();
        this.varh = model.variableHeuristic();
        this.ranking = model.ranking();
        this.width = model.widthHeuristic();
        this.flb = model.lowerBound();
        this.dominance = model.dominance();
        this.cache = model.useCache() ? Optional.of(new SimpleCache<>()) : Optional.empty();
        this.frontier = model.frontier();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.verbosityLevel = model.verbosityLevel();
        this.verboseMode = new VerboseMode(verbosityLevel, 500L);
        this.exportAsDot = model.exportDot();
        this.debugLevel = model.debugMode();
    }

    @Override
    public SearchStatistics minimize(Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        long start = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        frontier.push(root());
        cache.ifPresent(c -> c.initialize(problem));

        while (!frontier.isEmpty()) {
            nbIter++;
            verboseMode.detailedSearchState(nbIter, frontier.size(), bestUB,
                    frontier.bestInFrontier(), gap());

            queueMaxSize = Math.max(queueMaxSize, frontier.size());
            // 1. RESTRICTION
            SubProblem<T> sub = frontier.pop();
            double nodeLB = sub.getLowerBound();

            long end = System.currentTimeMillis();
            SearchStatistics stats = new SearchStatistics(SearchStatus.UNKNOWN, nbIter, queueMaxSize, end - start, bestUB, gap());
            if (bestUB != Double.POSITIVE_INFINITY)
                stats = new SearchStatistics(SearchStatus.SAT, nbIter, queueMaxSize, end - start, bestUB, gap());

            if (limit.test(stats)) {
                return stats;
            }


            verboseMode.currentSubProblem(nbIter, sub);

            if (nodeLB >= bestUB) {
                frontier.clear();
                end = System.currentTimeMillis();
                return new SearchStatistics(SearchStatus.OPTIMAL, nbIter, queueMaxSize, end - start, bestUB, 0);
            }

            int maxWidth = width.maximumWidth(sub.getState());
            CompilationConfig<T> compilation = new CompilationConfig<>();
            compilation.compilationType = CompilationType.Restricted;
            compilation.problem = this.problem;
            compilation.relaxation = this.relax;
            compilation.variableHeuristic = this.varh;
            compilation.stateRanking = this.ranking;
            compilation.residual = sub;
            compilation.maxWidth = maxWidth;
            compilation.flb = flb;
            compilation.dominance = this.dominance;
            compilation.cache = this.cache;
            compilation.bestUB = this.bestUB;
            compilation.cutSetType = frontier.cutSetType();
            compilation.exportAsDot = this.exportAsDot && this.firstRestricted;
            compilation.debugLevel = this.debugLevel;

            DecisionDiagram<T> restrictedMdd = new LinkedDecisionDiagram<>(compilation);

            restrictedMdd.compile();
            String problemName = problem.getClass().getSimpleName().replace("Problem", "");
            boolean newbest = maybeUpdateBest(restrictedMdd, exportAsDot && firstRestricted);
            if (newbest) {
                stats = new SearchStatistics(SearchStatus.SAT, nbIter, queueMaxSize, System.currentTimeMillis() - start, bestUB, gap());
                onSolution.accept(constructSolution(bestSol.get()), stats);
            }
            if (exportAsDot && firstRestricted) {
                exportDot(restrictedMdd.exportAsDot(),
                        Paths.get("output", problemName + "_restricted.dot").toString());
            }
            firstRestricted = false;


            if (restrictedMdd.isExact()) {
                continue;
            }

            // 2. RELAXATION
            compilation.compilationType = CompilationType.Relaxed;
            compilation.bestUB = this.bestUB;
            compilation.exportAsDot = this.exportAsDot && this.firstRelaxed;
            DecisionDiagram<T> relaxedMdd = new LinkedDecisionDiagram<>(compilation);

            relaxedMdd.compile();
            if (compilation.compilationType == CompilationType.Relaxed && relaxedMdd.relaxedBestPathIsExact()
                    && frontier.cutSetType() == CutSetType.Frontier) {
                maybeUpdateBest(relaxedMdd, exportAsDot && firstRelaxed);
            }
            if (exportAsDot && firstRelaxed) {
                if (!relaxedMdd.isExact())
                    relaxedMdd.bestSolution();
                exportDot(relaxedMdd.exportAsDot(),
                        Paths.get("output", problemName + "_relaxed.dot").toString());
            }
            firstRelaxed = false;
            if (relaxedMdd.isExact()) {
                newbest = maybeUpdateBest(relaxedMdd, false);
                if (newbest) {
                    stats = new SearchStatistics(SearchStatus.SAT, nbIter, queueMaxSize, System.currentTimeMillis() - start, bestUB, gap());
                    onSolution.accept(constructSolution(bestSol.get()), stats);
                }
            } else {
                enqueueCutset(relaxedMdd);
            }
        }
        long end = System.currentTimeMillis();
        return new SearchStatistics(SearchStatus.OPTIMAL, nbIter, queueMaxSize, end - start, bestUB, 0);
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
    private boolean maybeUpdateBest(DecisionDiagram<T> currentMdd, boolean exportDot) {
        Optional<Double> ddval = currentMdd.bestValue();
        if (ddval.isPresent() && ddval.get() < bestUB) {
            bestUB = ddval.get();
            bestSol = currentMdd.bestSolution();
            verboseMode.newBest(bestUB);
            return true;
        } else if (exportDot) {
            currentMdd.exportAsDot(); // to be sure to update the color of the edges.
        }
        return false;
    }

    /**
     * If necessary, tightens the bound of nodes in the cutset of `mdd` and
     * then add the relevant nodes to the shared fringe.
     */
    private void enqueueCutset(DecisionDiagram<T> currentMdd) {
        Iterator<SubProblem<T>> cutset = currentMdd.exactCutset();
        while (cutset.hasNext()) {
            SubProblem<T> cutsetNode = cutset.next();
            if (cutsetNode.getLowerBound() < bestUB) {
                frontier.push(cutsetNode);
            }
        }
    }

    private void exportDot(String dot, String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(dot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double gap() {
        if (frontier.isEmpty()) {
            return 0.0;
        } else {
            double bestInFrontier = frontier.bestInFrontier();
            return Math.abs(100 * (bestUB - bestInFrontier) / bestUB);
        }
    }
}
