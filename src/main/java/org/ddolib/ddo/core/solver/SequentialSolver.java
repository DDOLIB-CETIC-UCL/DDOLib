package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solution;
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
    private final DdoModel<T> model;
    /**
     * This is the cache used to prune the search tree
     */
    private final Optional<SimpleCache<T>> cache;
    /**
     * Value of the best known upper bound.
     */
    private double bestUB;
    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;
    /**
     * Only the first restricted mdd can be exported to a .dot file
     */
    private boolean firstRestricted = true;
    /**
     * Only the first relaxed mdd can be exported to a .dot file
     */
    private boolean firstRelaxed = true;

    private DominanceChecker<T> dominance;

    /**
     * Creates a fully qualified instance. The parameters of this solver are given via a
     * {@link DdoModel}
     *
     * @param model All the parameters needed to configure the solver.
     */
    public SequentialSolver(DdoModel<T> model) {
        this.problem = model.problem();
        this.width = model.widthHeuristic();
        this.cache = model.useCache() ? Optional.of(new SimpleCache<>()) : Optional.empty();
        this.frontier = model.frontier();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.verbosityLevel = model.verbosityLevel();
        this.verboseMode = new VerboseMode(verbosityLevel, 500L);
        this.exportAsDot = model.exportDot();
        this.dominance = model.dominance();
        this.model = model;
    }

    public SequentialSolver(Problem<T> problem, WidthHeuristic<T> width, Frontier<T> frontier, VerbosityLevel verbosityLevel, VerboseMode verboseMode, boolean exportAsDot, DdoModel<T> model, Optional<SimpleCache<T>> cache, double bestUB, Optional<Set<Decision>> bestSol, boolean firstRestricted, boolean firstRelaxed, DominanceChecker<T> dominance) {
        this.problem = problem;
        this.width = width;
        this.frontier = frontier;
        this.verbosityLevel = verbosityLevel;
        this.verboseMode = verboseMode;
        this.exportAsDot = exportAsDot;
        this.model = model;
        this.cache = cache;
        this.bestUB = bestUB;
        this.bestSol = bestSol;
        this.firstRestricted = firstRestricted;
        this.firstRelaxed = firstRelaxed;
        this.dominance = dominance;
    }

    @Override
    public Solution minimize(Predicate<SearchStatistics> limit,
                             BiConsumer<int[], SearchStatistics> onSolution) {
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
            SearchStatistics stats = new SearchStatistics(SearchStatus.UNKNOWN, nbIter,
                    queueMaxSize, end - start, bestUB, gap());

            if (limit.test(stats)) {
                return new Solution(bestSolution(), stats);
            }


            verboseMode.currentSubProblem(nbIter, sub);

            if (nodeLB >= bestUB) {
                frontier.clear();
                end = System.currentTimeMillis();
                SearchStatistics s = new SearchStatistics(SearchStatus.OPTIMAL, nbIter,
                        queueMaxSize, end - start, bestUB, 0);
                return new Solution(bestSolution(), s);
            }

            int maxWidth = width.maximumWidth(sub.getState());
            CompilationConfig<T> compilation = configureCompilation(CompilationType.Restricted,
                    sub, maxWidth, model.exportDot() && this.firstRestricted);

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
            compilation = configureCompilation(CompilationType.Relaxed, sub, maxWidth,
                    model.exportDot() && this.firstRelaxed);

            DecisionDiagram<T> relaxedMdd = new LinkedDecisionDiagram<>(compilation);
            relaxedMdd.compile();
            if (compilation.compilationType == CompilationType.Relaxed && relaxedMdd.relaxedBestPathIsExact()
                    && frontier.cutSetType() == CutSetType.Frontier) {
                newbest = maybeUpdateBest(relaxedMdd, exportAsDot && firstRelaxed);
                if (newbest) {
                    stats = new SearchStatistics(SearchStatus.SAT, nbIter, queueMaxSize, System.currentTimeMillis() - start, bestUB, gap());
                    onSolution.accept(constructSolution(bestSol.get()), stats);
                }
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
                if (relaxedMdd.bestValue().isPresent() && relaxedMdd.bestValue().get() >= bestUB) {
                    continue;
                } else {
                    enqueueCutset(relaxedMdd);
                }
            }
        }
        long end = System.currentTimeMillis();
        SearchStatistics stats = new SearchStatistics(SearchStatus.OPTIMAL, nbIter, queueMaxSize,
                end - start, bestUB, 0);
        return new Solution(bestSolution(), stats);
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

    @Override
    public double gap() {
        if (frontier.isEmpty() || bestUB == Double.POSITIVE_INFINITY) {
            return 100.0;
        } else {
            double globalLB = frontier.bestInFrontier();
            return 100 * Math.abs(bestUB - globalLB) / Math.abs(bestUB);
        }
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

    /**
     * Initialize the parameters of a compilation.
     *
     * @param type        the type of the compilation (restricted or relaxed)
     * @param sub         the root of the current sub-problem
     * @param maxWidth    the max width of the diagram
     * @param exportAsDot whether the diagram has to be exported as .dot file.
     * @return the parameters of the compilation
     */
    private CompilationConfig<T> configureCompilation(CompilationType type, SubProblem<T> sub,
                                                      int maxWidth, boolean exportAsDot) {
        CompilationConfig<T> compilation = new CompilationConfig<>(model);
        compilation.compilationType = type;
        compilation.problem = model.problem();
        compilation.relaxation = model.relaxation();
        compilation.variableHeuristic = model.variableHeuristic();
        compilation.stateRanking = model.ranking();
        compilation.residual = sub;
        compilation.maxWidth = maxWidth;
        compilation.flb = model.lowerBound();
        compilation.dominance = this.dominance;
        compilation.cache = this.cache;
        compilation.bestUB = this.bestUB;
        compilation.cutSetType = frontier.cutSetType();
        compilation.exportAsDot = exportAsDot;
        compilation.debugLevel = model.debugMode();

        if (type == CompilationType.Relaxed) {
            compilation.reductionStrategy = model.relaxStrategy();
        } else if (type == CompilationType.Restricted) {
            compilation.reductionStrategy = model.restrictStrategy();
        }

        return compilation;
    }
}
