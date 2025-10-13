package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
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
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

/**
 * From the lecture, you should have a good grasp on what a branch-and-bound
 * with mdd solver does even though you haven't looked into concrete code
 * yet.
 * <p>
 * One of the tasks from this assignment is for you to implement the vanilla
 * algorithm (sequentially) as it has been explained during the lecture.
 * <p>
 * To help you, we provide you with a well documented framework that defines
 * and implements all the abstractions you will need in order to implement
 * a generic solver. Additionally, and because the BaB-MDD framework parallelizes
 * *VERY* well, we provide you with a parallel implementation of the algorithm
 * (@see ParallelSolver). Digging into that code, understanding it, and stripping
 * away all the parallel-related concerns should finalize to give you a thorough
 * understanding of the sequential algo.
 * <p>
 * # Note
 * ONCE YOU HAVE A CLEAR IDEA OF HOW THE CODE WORKS, THIS TASK SHOULD BE EXTREMELY
 * EASY TO COMPLETE.
 *
 * @param <T> The type of states.
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
     * Add a time limit for the search, by default it is set to infinity
     */
    private final int timeLimit;
    /**
     * Add a gap limit for the search, by default it is set to zero
     */
    private final double gapLimit;
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
     * {@link SolverConfig}<br><br>
     *
     * <b>Mandatory parameters:</b>
     * <ul>
     *     <li>An implementation of {@link Problem}</li>
     *     <li>An implementation of {@link Relaxation}</li>
     *     <li>An implementation of {@link StateRanking}</li>
     *     <li>An implementation of {@link VariableHeuristic}</li>
     *     <li>An implementation of {@link WidthHeuristic}</li>
     *     <li>An implementation of {@link Frontier}</li>
     * </ul>
     * <br>
     * <b>Optional parameters: </b>
     * <ul>
     *     <li>An implementation of {@link FastLowerBound}</li>
     *     <li>An implementation of {@link DominanceChecker}</li>
     *     <li>A time limit</li>
     *     <li>A gap limit</li>
     *     <li>A verbosity level</li>
     *     <li>A boolean to export some mdd as .dot file</li>
     *     <li>A debug level:
     *          <ul>
     *               <li>0: no additional tests (default)</li>
     *               <li>1: checks if the upper bound is well-defined and if the hash code
     *               of the states are coherent</li>
     *               <li>2: 1 + export diagram with failure in {@code output/failure.dot}</li>
     *             </ul>
     *     </li>
     * </ul>
     *
     * @param config All the parameters needed to configure the solver.
     */
    public SequentialSolver(SolverConfig<T> config) {
        this.problem = config.problem;
        this.relax = config.relax;
        this.varh = config.varh;
        this.ranking = config.ranking;
        this.width = config.width;
        this.flb = config.flb;
        this.dominance = config.dominance;
        this.cache = config.cache == null ? Optional.empty() : Optional.of(config.cache);
        this.frontier = config.frontier;
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.timeLimit = config.timeLimit;
        this.gapLimit = config.gapLimit;
        this.verbosityLevel = config.verbosityLevel;
        this.exportAsDot = config.exportAsDot;
        this.debugLevel = config.debugLevel;
    }

    @Override
    public SearchStatistics minimize() {
        return minimize((Predicate<SearchStatistics>) null);
    }


    @Override
    public SearchStatistics minimize(Predicate<SearchStatistics> limit) {
        long start = System.currentTimeMillis();
        int printInterval = 500; //ms; half a second
        long nextPrint = start + printInterval;
        int nbIter = 0;
        int queueMaxSize = 0;
        frontier.push(root());
        cache.ifPresent(c -> c.initialize(problem));

        while (!frontier.isEmpty()) {
            nbIter++;
            if (verbosityLevel == VerbosityLevel.LARGE) {
                long now = System.currentTimeMillis();
                if (now >= nextPrint) {
                    double bestInFrontier = frontier.bestInFrontier();

                    System.out.printf("it:%d  frontierSize:%d bestObj:%g bestInFrontier:%g gap:%.1f%%%n",
                            nbIter, frontier.size(), bestUB, bestInFrontier, gap());

                    nextPrint = now + printInterval;
                }
            }

            queueMaxSize = Math.max(queueMaxSize, frontier.size());
            // 1. RESTRICTION
            SubProblem<T> sub = frontier.pop();
            double nodeLB = sub.getLowerBound();

            long end = System.currentTimeMillis();
            int[] sol = new int[problem.nbVars()];
            Optional<Double> solVal = Optional.empty();
            SearchStatistics statistics;
            if (limit != null) {
                if (bestSol.isEmpty()) {
                    Arrays.fill(sol, -1);
                    statistics = new SearchStatistics(nbIter, queueMaxSize, end - start, SearchStatistics.SearchStatus.UNKNOWN, Double.MAX_VALUE, solVal, sol, solVal);
                } else {
                    if (bestSol.get().size() < problem.nbVars()) {
                        solVal = bestValue();
                        statistics = new SearchStatistics(nbIter, queueMaxSize, end - start, SearchStatistics.SearchStatus.UNSAT, gap(), solVal, sol, solVal);
                    } else {
                        sol = constructSolution(bestSol.get().size());
                        solVal = bestValue();
                        if (gap() == 0.0)
                            statistics = new SearchStatistics(nbIter, queueMaxSize, end - start, SearchStatistics.SearchStatus.OPTIMAL, gap(), solVal, sol, solVal);
                        else
                            statistics = new SearchStatistics(nbIter, queueMaxSize, end - start, SearchStatistics.SearchStatus.SAT, gap(), solVal, sol, solVal);

                    }
                }
                if (limit.test(statistics)) {
                    return statistics;
                }
            }


            if (verbosityLevel == VerbosityLevel.LARGE) {
                System.out.println("it:" + nbIter + "\t" + sub);
                System.out.println("\t" + sub.getState());
            }

            if (nodeLB >= bestUB) {
                double gap = gap();
                frontier.clear();
                end = System.currentTimeMillis();
                sol = constructSolution(bestSol.get().size());
                solVal = Optional.of(bestUB);
                return new SearchStatistics(nbIter, queueMaxSize, end - start, currentSearchStatus(gap), gap, solVal, sol, solVal);
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
            maybeUpdateBest(restrictedMdd, exportAsDot && firstRestricted);
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
                    relaxedMdd.bestSolution(); // to update the best edges' color
                exportDot(relaxedMdd.exportAsDot(),
                        Paths.get("output", problemName + "_relaxed.dot").toString());
            }
            firstRelaxed = false;
            if (relaxedMdd.isExact()) {
                maybeUpdateBest(relaxedMdd, false);
            } else {
                enqueueCutset(relaxedMdd);
            }
        }
        long end = System.currentTimeMillis();
        int[] sol = constructSolution(problem.nbVars());
        return new SearchStatistics(nbIter, queueMaxSize, end - start,
                SearchStatistics.SearchStatus.OPTIMAL, 0.0,
                cache.map(SimpleCache::stats).orElse("noCache"), bestValue(), sol, bestValue());
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
            if (verbosityLevel != VerbosityLevel.SILENT) System.out.println("new best: " + bestUB);
        } else if (exportDot) {
            currentMdd.exportAsDot(); // to be sure to update the color of the edges.
        }
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

    private SearchStatistics.SearchStatus currentSearchStatus(double gap) {
        if (bestSol.isEmpty()) {
            if (bestUB == Double.POSITIVE_INFINITY) {
                return SearchStatistics.SearchStatus.UNKNOWN;
            } else {
                return SearchStatistics.SearchStatus.UNSAT;
            }
        } else {
            if (gap > 0.0)
                return SearchStatistics.SearchStatus.SAT;
            else return SearchStatistics.SearchStatus.OPTIMAL;
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
