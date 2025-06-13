package org.ddolib.ddo.implem.solver;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.heuristics.WidthHeuristic;
import org.ddolib.ddo.implem.dominance.Dominance;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.mdd.LinkedDecisionDiagram;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

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
 * @param <K> the type of key
 * @param <T> the type of state
 */
public final class SequentialSolver<K, T> implements Solver {
    /**
     * The problem we want to maximize
     */
    private final Problem<T> problem;
    /**
     * A suitable relaxation for the problem we want to maximize
     */
    private final Relaxation<T> relax;
    /**
     * An heuristic to identify the most promising nodes
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
     * This is the fringe: the set of nodes that must still be explored before
     * the problem can be considered 'solved'.
     * <p>
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
     * <p>
     * # Note:
     * This approach is recommended, however we do not force this design choice.
     * You might decide against reusing the same object over and over (even though
     * it has been designed to be reused). Should you decide to not reuse this
     * object, then you can simply ignore this field (and remove it altogether).
     */
    private final DecisionDiagram<T, K> mdd;

    /**
     * This is the value of the best known lower bound.
     */
    private int bestLB;
    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    /**
     * This is the dominance object that will be used to prune the search space.
     */
    private SimpleDominanceChecker<T, K> dominance;

    private boolean firstRestricted = true;
    private boolean firstRelaxed = true;

    /**
     * Creates a fully qualified instance
     */
    public SequentialSolver(
            final Problem<T> problem,
            final Relaxation<T> relax,
            final VariableHeuristic<T> varh,
            final StateRanking<T> ranking,
            final WidthHeuristic<T> width,
            final SimpleDominanceChecker<T, K> dominance,
            final Frontier<T> frontier) {
        this.problem = problem;
        this.relax = relax;
        this.varh = varh;
        this.ranking = ranking;
        this.width = width;
        this.dominance = dominance;
        this.frontier = frontier;
        this.mdd = new LinkedDecisionDiagram<>();
        this.bestLB = Integer.MIN_VALUE;
        this.bestSol = Optional.empty();
    }

    public SequentialSolver(
            final Problem<T> problem,
            final Relaxation<T> relax,
            final VariableHeuristic<T> varh,
            final StateRanking<T> ranking,
            final WidthHeuristic<T> width,
            final Frontier<T> frontier) {

        this(problem,
                relax,
                varh,
                ranking,
                width,
                new SimpleDominanceChecker(new Dominance<T, Integer>() {
                    @Override
                    public Integer getKey(T t) {
                        return 0;
                    }

                    @Override
                    public boolean isDominatedOrEqual(T state1, T state2) {
                        return false;
                    }
                }, problem.nbVars()),
                frontier);
    }


    @Override
    public SearchStatistics maximize() {
        return maximize(0);
    }

    @Override
    public SearchStatistics maximize(int verbosityLevel) {
        int nbIter = 0;
        int queueMaxSize = 0;
        frontier.push(root());
        while (!frontier.isEmpty()) {
            if (verbosityLevel >= 1) System.out.println("it " + nbIter + "\t frontier:" + frontier.size() + "\t " +
                    "bestObj:" + bestLB);

            nbIter++;
            queueMaxSize = Math.max(queueMaxSize, frontier.size());
            // 1. RESTRICTION
            SubProblem<T> sub = frontier.pop();
            int nodeUB = sub.getUpperBound();

            if (verbosityLevel >= 2)
                System.out.println("subProblem(ub:" + nodeUB + " val:" + sub.getValue() + " depth:" + sub.getPath().size() + " fastUpperBound:" + (nodeUB - sub.getValue()) + "):" + sub.getState());
            if (verbosityLevel >= 1) System.out.println("\n");
            if (nodeUB <= bestLB) {
                frontier.clear();
                return new SearchStatistics(nbIter, queueMaxSize);
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
                    dominance,
                    bestLB,
                    frontier.cutSetType(),
                    verbosityLevel >= 3 && firstRestricted
            );

            mdd.compile(compilation);
            maybeUpdateBest(verbosityLevel);
            System.out.println(mdd.exportAsDot());
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
                    dominance,
                    bestLB,
                    frontier.cutSetType(),
                    false
            );
            mdd.compile(compilation);
            if (mdd.isExact()) {
                maybeUpdateBest(verbosityLevel);
            } else {
                enqueueCutset();
            }
        }
        return new SearchStatistics(nbIter, queueMaxSize);
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

    /**
     * @return the root subproblem
     */
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
    private void maybeUpdateBest(int verbosityLevel) {
        Optional<Integer> ddval = mdd.bestValue();
        if (ddval.isPresent() && ddval.get() > bestLB) {
            bestLB = ddval.get();
            bestSol = mdd.bestSolution();
            if (verbosityLevel > 2) System.out.println("new best " + bestLB);
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
}
