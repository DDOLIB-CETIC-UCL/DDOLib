package org.ddolib.common.solver;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.cache.Cache;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;

/**
 * Class containing all the parameter needed for all kind of solver with default value.
 * <b>Warning: </b> The majority of the fields are initialized with {@code null}
 *
 * @param <T> The type of states.
 * @param <K> The type of dominance keys.
 */
public class SolverConfig<T, K> {

    // USEFUL FOR ALL SOLVER

    /**
     * The problem we want to maximize.
     */
    public Problem<T> problem = null;

    /**
     * * A heuristic to choose the next variable to branch on when developing a DD.
     */
    public VariableHeuristic<T> varh = null;

    /**
     * The heuristic defining a very rough estimation (upper bound) of the optimal value.
     */
    public FastUpperBound<T> ub = new DefaultFastUpperBound<>();

    /**
     * The dominance object that will be used to prune the search space.
     */
    public DominanceChecker<T, K> dominance = new DefaultDominanceChecker<>();


    // USEFUL FOR DDO SOLVER

    /**
     * A suitable relaxation for the problem we want to maximize.
     */
    public Relaxation<T> relax = null;

    /**
     * A heuristic to identify the most promising nodes.
     */
    public StateRanking<T> ranking = null;

    /**
     * A heuristic to choose the maximum width of the DD you compile.
     */
    public WidthHeuristic<T> width = null;

    /**
     * The set of nodes that must still be explored before
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
    public Frontier<T> frontier = null;

    /**
     * The budget of time give to the solver to solve the problem.
     */
    public Integer timeLimit = Integer.MAX_VALUE;
    /**
     * The stop the search when the gat of the search reach the limit.
     */
    public Double gapLimit = 0.0;


    // USEFUL FOR PARALLEL SOLVER

    /**
     * The number of threads that can be used in parallel.
     */
    public Integer nbThreads = Runtime.getRuntime().availableProcessors();

    // USEFUl FOR SOLVER USING CACHE

    /**
     * The cache used to prune the search space.
     */
    public Cache<T> cache = null;

}
