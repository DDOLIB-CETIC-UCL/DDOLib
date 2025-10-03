package org.ddolib.common.solver;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;

/**
 * Class containing all the parameter needed for all kind of solver with default value.
 *
 * @param <T> The type of states.
 * @param <K> The type of dominance keys.
 */
public class SolverConfig<T, K> {

    // USEFUL FOR ALL SOLVER

    /**
     * The problem we want to maximize ({@code null} by default).
     */
    public Problem<T> problem = null;

    /**
     * * A heuristic to choose the next variable to branch on when developing a DD ({@code null} by default).
     */
    public VariableHeuristic<T> varh = null;

    /**
     * The heuristic defining a very rough estimation (upper bound) of the optimal value
     * ({@link DefaultFastUpperBound} by default).
     */
    public FastUpperBound<T> fub = new DefaultFastUpperBound<>();

    /**
     * The dominance object that will be used to prune the search space ({@link DefaultDominanceChecker} by default).
     */
    public DominanceChecker<T, K> dominance = new DefaultDominanceChecker<>();


    // USEFUL FOR DDO SOLVER

    /**
     * A suitable relaxation for the problem we want to maximize ({@code null} by default).
     */
    public Relaxation<T> relax = null;

    /**
     * A heuristic to identify the most promising nodes ({@code null} by default).
     */
    public StateRanking<T> ranking = null;

    /**
     * A heuristic to choose the maximum width of the DD you compile ({@code null} by default).
     */
    public WidthHeuristic<T> width = null;

    /**
     * The set of nodes that must still be explored before
     * the problem can be considered 'solved' ({@code null} by default).
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
     * The budget of time give to the solver to solve the problem ({@code Integer.MAX_VALUE} by
     * default).
     */
    public Integer timeLimit = Integer.MAX_VALUE;
    /**
     * The stop the search when the gat of the search reach the limit ({@code 0} by default).
     */
    public Double gapLimit = 0.0;


    // USEFUL FOR PARALLEL SOLVER

    /**
     * The number of threads that can be used in parallel (all available processors by default).
     */
    public Integer nbThreads = Runtime.getRuntime().availableProcessors();

    // USEFUl FOR SOLVER USING CACHE

    /**
     * The cache used to prune the search space ({@code null} by default).
     */
    public SimpleCache<T> cache = null;


    // PARAMETERS FOR DEBUG / VERBOSITY STUFF

    /**
     * <ul>
     *     <li>0: no verbosity (default)</li>
     *     <li>1: display newBest whenever there is a newBest</li>
     *     <li>2: 1 + statistics about the front every half a second (or so)</li>
     *     <li>3: 2 + every developed sub-problem</li>
     *     <li>4: 3 + details about the developed state</li>
     *
     * </ul>
     * <p>
     * <p>
     * 3: 2 + every developed sub-problem
     * 4: 3 + details about the developed state
     */
    public Integer verbosityLevel = 0;

    /**
     * Whether we want to export the first explored restricted and relaxed mdd ({@code false} by
     * default.
     * Tooltips are configured to give additional information on nodes and edges.
     */
    public Boolean exportAsDot = false;

    /**
     * Strategy to select which nodes should be merged together on a relaxed DD.
     */
    public ReductionStrategy<T> relaxStrategy = new CostBased<>(this.ranking);

    /**
     * Strategy to select which nodes should be dropped on a restricted DD.
     */
    public ReductionStrategy<T> restrictStrategy = new CostBased<>(this.ranking);


    /**
     * <ul>
     *     <li>0: no additional tests (default)</li>
     *     <li>1: checks if the upper bound is well-defined</li>
     *     <li>2: 1 + export diagram with failure in {@code output/failure.dot}</li>
     * </ul>
     */
    public Integer debugLevel = 0;

}
