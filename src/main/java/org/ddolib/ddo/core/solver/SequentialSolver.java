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
import org.ddolib.ddo.core.frontier.CutSetType;
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.Random;

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
 * @param <K> The type of dominance keys.
 */
public final class SequentialSolver<T, K> implements Solver {
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
     * A heuristic to choose the maximum width of the DD you compile
     */
    private final WidthHeuristic<T> width;
    /**
     * A heuristic to choose the next variable to branch on when developing a DD
     */
    private final VariableHeuristic<T> varh;
    private final ClusterStrat relaxStrat;
    private final ClusterStrat restrictionStrat;

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
     * The heuristic defining a very rough estimation (upper bound) of the optimal value.
     */
    private final FastUpperBound<T> fub;

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


    /**
     * Add a time limit for the search, by default it is set to infinity
     */
    private int timeLimit = Integer.MAX_VALUE;

    /**
     * Add a gap limit for the search, by default it is set to zero
     */
    private double gapLimit = 0.0;

    /**
     * Creates a fully qualified instance
     *
     * @param problem   The problem we want to maximize.
     * @param relax     A suitable relaxation for the problem we want to maximize
     * @param varh      A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking   A heuristic to identify the most promising nodes.
     * @param width     A heuristic to choose the maximum width of the DD you compile.
     * @param frontier  The set of nodes that must still be explored before
     *                  the problem can be considered 'solved'.
     *                  <p>
     *                  # Note:
     *                  This fringe orders the nodes by upper bound (so the highest ub is going
     *                  to pop first). So, it is guaranteed that the upper bound of the first
     *                  node being popped is an upper bound on the value reachable by exploring
     *                  any of the nodes remaining on the fringe. As a consequence, the
     *                  exploration can be stopped as soon as a node with an ub &#8804; current best
     *                  lower bound is popped.
     * @param fub       The heuristic defining a very rough estimation (upper bound) of the optimal value.
     * @param dominance The dominance object that will be used to prune the search space.
     * @param timeLimit The budget of time give to the solver to solve the problem.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     */

    public SequentialSolver(
            final Problem<T> problem,
            final Relaxation<T> relax,
            final VariableHeuristic<T> varh,
            final StateRanking<T> ranking,
            final WidthHeuristic<T> width,
            final Frontier<T> frontier,
            final FastUpperBound<T> fub,
            final DominanceChecker<T, K> dominance,
            int timeLimit,
            double gapLimit,
            final ClusterStrat relaxStrat,
            final ClusterStrat restrictionStrat,
            final StateDistance<T> distance,
            final StateCoordinates<T> coord,
            final int seed) {
        this.relaxStrat = relaxStrat;
        this.restrictionStrat = restrictionStrat;
        this.problem = problem;
        this.relax = relax;
        this.varh = varh;
        this.ranking = ranking;
        this.width = width;
        this.fub = fub;
        this.dominance = dominance;
        this.frontier = frontier;
        this.mdd = new LinkedDecisionDiagram<>();
        this.bestLB = Double.NEGATIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.timeLimit = timeLimit;
        this.gapLimit = gapLimit;
        this.distance = distance;
        this.coord = coord;
        this.bestUB = Integer.MAX_VALUE;
        this.rnd = new Random(seed);
    }


    @Override
    public SearchStatistics maximize() {
        return maximize(0, false);
    }

    @Override
    public SearchStatistics maximize(int verbosityLevel, boolean exportAsDot) {
        long start = System.currentTimeMillis();
        int printInterval = 500; //ms; half a second
        long nextPrint = start + printInterval;
        int nbIter = 0;
        int queueMaxSize = 0;
        frontier.push(root());
        while (!frontier.isEmpty()) {
            nbIter++;
            if (verbosityLevel >= 2) {
                long now = System.currentTimeMillis();
                if (now >= nextPrint) {
                    double bestInFrontier = frontier.bestInFrontier();
                    double gap = 100 * (bestInFrontier - bestLB) / bestLB;

                    System.out.printf("it:%d  frontierSize:%d bestObj:%g bestInFrontier:%g gap:%.1f%%%n",
                            nbIter, frontier.size(), bestLB, bestInFrontier, gap());

                    nextPrint = now + printInterval;
                }
            }

            queueMaxSize = Math.max(queueMaxSize, frontier.size());
            // 1. RESTRICTION
            SubProblem<T> sub = frontier.pop();
            double nodeUB = sub.getUpperBound();

            long end = System.currentTimeMillis();
            if (!frontier.isEmpty() && gapLimit != 0.0 && gap() <= gapLimit) {
                return new SearchStatistics(nbIter, queueMaxSize, end - start, currentSearchStatus(gap()), gap());
            }
            if (!frontier.isEmpty() && timeLimit != Integer.MAX_VALUE && end - start > 1000 * timeLimit) {
                return new SearchStatistics(nbIter, queueMaxSize, end - start, currentSearchStatus(gap()), gap());
            }

            if (verbosityLevel >= 3){
                System.out.println("it:" + nbIter + "\t" + sub.statistics());
                if (verbosityLevel >= 4) {
                    System.out.println("\t" + sub.getState());
                }
            }

            if (nodeUB <= bestLB) {
                double gap = gap();
                frontier.clear();
                end = System.currentTimeMillis();
                return new SearchStatistics(nbIter, queueMaxSize, end - start, currentSearchStatus(gap), gap);
            }

            int maxWidth = width.maximumWidth(sub.getState());
            CompilationInput<T, K> compilation = new CompilationInput<>(
                    CompilationType.Restricted,
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
                    exportAsDot && firstRestricted,
                    relaxStrat,
                    restrictionStrat,
                    distance,
                    coord,
                    rnd
            );

            mdd.compile(compilation);
            String problemName = problem.getClass().getSimpleName().replace("Problem", "");
            maybeUpdateBest(verbosityLevel, exportAsDot && firstRestricted);
            if (exportAsDot && firstRestricted) {
                exportDot(mdd.exportAsDot(),
                        Paths.get("output", problemName + "_restricted.dot").toString());
            }
            firstRestricted = false;


            if (mdd.isExact()) {
                continue;
            }

            // 2. RELAXATION
            compilation = new CompilationInput<>(
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
                    exportAsDot && firstRelaxed,
                    relaxStrat,
                    restrictionStrat,
                    distance,
                    coord,
                    rnd
            );
            mdd.compile(compilation);
            if (compilation.compilationType() == CompilationType.Relaxed && mdd.relaxedBestPathIsExact()
                    && frontier.cutSetType() == CutSetType.Frontier) {
                maybeUpdateBest(verbosityLevel, exportAsDot && firstRelaxed);
            }
            if (exportAsDot && firstRelaxed) {
                if (!mdd.isExact()) mdd.bestSolution(); // to update the best edges' color
                exportDot(mdd.exportAsDot(),
                        Paths.get("output", problemName + "_relaxed.dot").toString());
            }
            firstRelaxed = false;
            if (mdd.isExact()) {
                maybeUpdateBest(verbosityLevel, exportAsDot && firstRelaxed);
            } else {
                enqueueCutset();
            }
        }
        long end = System.currentTimeMillis();
        return new SearchStatistics(nbIter, queueMaxSize,end-start, SearchStatistics.SearchStatus.OPTIMAL, 0.0);
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

    /**
     * @return the root subproblem
     */
    private SubProblem<T> root() {
        return new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Double.POSITIVE_INFINITY,
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

    /**
     * If necessary, tightens the bound of nodes in the cutset of `mdd` and
     * then add the relevant nodes to the shared fringe.
     */
    private void enqueueCutset() {
        Iterator<SubProblem<T>> cutset = mdd.exactCutset();
        while (cutset.hasNext()) {
            SubProblem<T> cutsetNode = cutset.next();
            if (cutsetNode.getUpperBound() > bestLB) {
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
            if (bestLB == -Double.MAX_VALUE) {
                return SearchStatistics.SearchStatus.UNKNOWN;
            } else {
                return SearchStatistics.SearchStatus.UNSAT;
            }
        } else {
            if (Math.abs(gap) > 0.0) {
                return SearchStatistics.SearchStatus.SAT;
            }
            else return SearchStatistics.SearchStatus.OPTIMAL;
        }
    }

    private double gap() {
        if (frontier.isEmpty()) {
            return 0.0;
        } else {
            double bestInFrontier = frontier.bestInFrontier();
            return 100.0 * (bestInFrontier - bestLB) / bestLB;
        }
    }
}
