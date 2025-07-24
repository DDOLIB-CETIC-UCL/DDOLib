package org.ddolib.factory;

import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.solver.ExactSolver;
import org.ddolib.ddo.core.solver.ParallelSolver;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.ddo.core.solver.SequentialSolverWithCache;
import org.ddolib.modeling.*;

/**
 * Factory of solvers.
 */
public class Solvers {

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param cache    The cache used to prune the search space.
     * @param <T>       The type of the states.
     * @param <K>       The type of the dominance keys.
     * @param timeLimit the budget of time provide to solve the problem.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     * @return A solver for the input problem using the given configuration.
     */
    public static <K, T> SequentialSolverWithCache<K, T> sequentialSolverWithCache(final Problem<T> problem,
                                                                                   final Relaxation<T> relax,
                                                                                   final VariableHeuristic<T> varh,
                                                                                   final StateRanking<T> ranking,
                                                                                   final WidthHeuristic<T> width,
                                                                                   final Frontier<T> frontier,
                                                                                   final FastUpperBound<T> fub,
                                                                                   final DominanceChecker<T, K> dominance,
                                                                                   final SimpleCache<T> cache,
                                                                                   final int timeLimit,
                                                                                   final double gapLimit) {
        return new SequentialSolverWithCache<>(problem, relax, varh, ranking, width, frontier, fub, dominance, cache, timeLimit, gapLimit);
    }

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param cache    The cache used to prune the search space.
     * @param <T>       The type of the states.
     * @param <K>       The type of the dominance keys.
     * @param timeLimit the budget of time provide to solve the problem.
     * @return A solver for the input problem using the given configuration.
     */
    public static <K, T> SequentialSolverWithCache<K, T> sequentialSolverWithCache(final Problem<T> problem,
                                                                                   final Relaxation<T> relax,
                                                                                   final VariableHeuristic<T> varh,
                                                                                   final StateRanking<T> ranking,
                                                                                   final WidthHeuristic<T> width,
                                                                                   final Frontier<T> frontier,
                                                                                   final FastUpperBound<T> fub,
                                                                                   final DominanceChecker<T, K> dominance,
                                                                                   final SimpleCache<T> cache,
                                                                                   final int timeLimit) {
        return new SequentialSolverWithCache<>(problem, relax, varh, ranking, width, frontier, fub, dominance, cache, timeLimit, 0.0);
    }

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param cache    The cache used to prune the search space.
     * @param <T>       The type of the states.
     * @param <K>       The type of the dominance keys.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     * @return A solver for the input problem using the given configuration.
     */
    public static <K, T> SequentialSolverWithCache<K, T> sequentialSolverWithCache(final Problem<T> problem,
                                                                                   final Relaxation<T> relax,
                                                                                   final VariableHeuristic<T> varh,
                                                                                   final StateRanking<T> ranking,
                                                                                   final WidthHeuristic<T> width,
                                                                                   final Frontier<T> frontier,
                                                                                   final FastUpperBound<T> fub,
                                                                                   final DominanceChecker<T, K> dominance,
                                                                                   final SimpleCache<T> cache,
                                                                                   final double gapLimit) {
        return new SequentialSolverWithCache<>(problem, relax, varh, ranking, width, frontier, fub, dominance, cache, Integer.MAX_VALUE, gapLimit);
    }

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param cache    The cache used to prune the search space.
     * @param <T>       The type of the states.
     * @param <K>       The type of the dominance keys.
     * @return A solver for the input problem using the given configuration.
     */
    public static <K, T> SequentialSolverWithCache<K, T> sequentialSolverWithCache(final Problem<T> problem,
                                                                                   final Relaxation<T> relax,
                                                                                   final VariableHeuristic<T> varh,
                                                                                   final StateRanking<T> ranking,
                                                                                   final WidthHeuristic<T> width,
                                                                                   final Frontier<T> frontier,
                                                                                   final FastUpperBound<T> fub,
                                                                                   final DominanceChecker<T, K> dominance,
                                                                                   final SimpleCache<T> cache) {
        return new SequentialSolverWithCache<>(problem, relax, varh, ranking, width, frontier, fub, dominance, cache, Integer.MAX_VALUE, 0.0);
    }

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param cache    The cache used to prune the search space.
     * @param <T>       The type of the states.
     * @param timeLimit the budget of time provide to solve the problem.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolverWithCache<Integer, T> sequentialSolverWithCache(final Problem<T> problem,
                                                                                               final Relaxation<T> relax,
                                                                                               final VariableHeuristic<T> varh,
                                                                                               final StateRanking<T> ranking,
                                                                                               final WidthHeuristic<T> width,
                                                                                               final Frontier<T> frontier,
                                                                                               final FastUpperBound<T> fub,
                                                                                               final SimpleCache<T> cache,
                                                                                               final int timeLimit,
                                                                                               final double gapLimit ) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolverWithCache<>(problem, relax, varh, ranking, width, frontier, fub, defaultDominance, cache, timeLimit, gapLimit);
    }

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param cache    The cache used to prune the search space.
     * @param <T>       The type of the states.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolverWithCache<Integer, T> sequentialSolverWithCache(final Problem<T> problem,
                                                                                      final Relaxation<T> relax,
                                                                                      final VariableHeuristic<T> varh,
                                                                                      final StateRanking<T> ranking,
                                                                                      final WidthHeuristic<T> width,
                                                                                      final Frontier<T> frontier,
                                                                                      final FastUpperBound<T> fub,
                                                                                      final SimpleCache<T> cache,
                                                                                      final double gapLimit ) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolverWithCache<>(problem, relax, varh, ranking, width, frontier, fub, defaultDominance, cache, Integer.MAX_VALUE, gapLimit);
    }

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param cache    The cache used to prune the search space.
     * @param <T>       The type of the states.
     * @param timeLimit the budget of time provide to solve the problem.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolverWithCache<Integer, T> sequentialSolverWithCache(final Problem<T> problem,
                                                                                      final Relaxation<T> relax,
                                                                                      final VariableHeuristic<T> varh,
                                                                                      final StateRanking<T> ranking,
                                                                                      final WidthHeuristic<T> width,
                                                                                      final Frontier<T> frontier,
                                                                                      final FastUpperBound<T> fub,
                                                                                      final SimpleCache<T> cache,
                                                                                      final int timeLimit) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolverWithCache<>(problem, relax, varh, ranking, width, frontier, fub, defaultDominance, cache, timeLimit, 0.0);
    }

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param cache    The cache used to prune the search space.
     * @param <T>       The type of the states.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolverWithCache<Integer, T> sequentialSolverWithCache(final Problem<T> problem,
                                                                                      final Relaxation<T> relax,
                                                                                      final VariableHeuristic<T> varh,
                                                                                      final StateRanking<T> ranking,
                                                                                      final WidthHeuristic<T> width,
                                                                                      final Frontier<T> frontier,
                                                                                      final FastUpperBound<T> fub,
                                                                                      final SimpleCache<T> cache) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolverWithCache<>(problem, relax, varh, ranking, width, frontier, fub, defaultDominance, cache, Integer.MAX_VALUE, 0.0);
    }

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param <T>       The type of the states.
     * @param <K>       The type of the dominance keys.
     * @param timeLimit the budget of time provide to solve the problem.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> SequentialSolver<T, K> sequentialSolver(final Problem<T> problem,
                                                                 final Relaxation<T> relax,
                                                                 final VariableHeuristic<T> varh,
                                                                 final StateRanking<T> ranking,
                                                                 final WidthHeuristic<T> width,
                                                                 final Frontier<T> frontier,
                                                                 final FastUpperBound<T> fub,
                                                                 final DominanceChecker<T, K> dominance,
                                                                 final int timeLimit,
                                                                 final double gapLimit) {
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, fub, dominance, timeLimit, gapLimit);
    }

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param <T>       The type of the states.
     * @param <K>       The type of the dominance keys.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> SequentialSolver<T, K> sequentialSolver(final Problem<T> problem,
                                                                 final Relaxation<T> relax,
                                                                 final VariableHeuristic<T> varh,
                                                                 final StateRanking<T> ranking,
                                                                 final WidthHeuristic<T> width,
                                                                 final Frontier<T> frontier,
                                                                 final FastUpperBound<T> fub,
                                                                 final DominanceChecker<T, K> dominance,
                                                                 final double gapLimit) {
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, fub, dominance, Integer.MAX_VALUE, gapLimit);
    }

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param <T>       The type of the states.
     * @param <K>       The type of the dominance keys.
     * @param timeLimit the budget of time provide to solve the problem.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> SequentialSolver<T, K> sequentialSolver(final Problem<T> problem,
                                                                 final Relaxation<T> relax,
                                                                 final VariableHeuristic<T> varh,
                                                                 final StateRanking<T> ranking,
                                                                 final WidthHeuristic<T> width,
                                                                 final Frontier<T> frontier,
                                                                 final FastUpperBound<T> fub,
                                                                 final DominanceChecker<T, K> dominance,
                                                                 final int timeLimit) {
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, fub, dominance, timeLimit, 0.0);
    }

    /**
     * Instantiates a sequential solver for a given problem.
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
     * @param <T>       The type of the states.
     * @param <K>       The type of the dominance keys.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> SequentialSolver<T, K> sequentialSolver(final Problem<T> problem,
                                                                 final Relaxation<T> relax,
                                                                 final VariableHeuristic<T> varh,
                                                                 final StateRanking<T> ranking,
                                                                 final WidthHeuristic<T> width,
                                                                 final Frontier<T> frontier,
                                                                 final FastUpperBound<T> fub,
                                                                 final DominanceChecker<T, K> dominance) {
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, fub, dominance, Integer.MAX_VALUE, 0.0);
    }




    /**
     * Instantiates a sequential solver for a given problem. The instance do not use the dominance mechanism.
     *
     * @param problem  The problem we want to maximize.
     * @param relax    A suitable relaxation for the problem we want to maximize
     * @param varh     A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking  A heuristic to identify the most promising nodes.
     * @param width    A heuristic to choose the maximum width of the DD you compile.
     * @param frontier The set of nodes that must still be explored before
     *                 the problem can be considered 'solved'.
     *                 <p>
     *                 # Note:
     *                 This fringe orders the nodes by upper bound (so the highest ub is going
     *                 to pop first). So, it is guaranteed that the upper bound of the first
     *                 node being popped is an upper bound on the value reachable by exploring
     *                 any of the nodes remaining on the fringe. As a consequence, the
     *                 exploration can be stopped as soon as a node with an ub &#8804; current best
     *                 lower bound is popped.
     * @param fub      The heuristic defining a very rough estimation (upper bound) of the optimal value.
     * @param <T>      The type of the states.
     * @param timeLimit the budget of time provide to solve the problem.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolver<T, Integer> sequentialSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier,
                                                                    final FastUpperBound<T> fub,
                                                                    final int timeLimit,
                                                                    final double gapLimit) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, fub, defaultDominance, timeLimit, gapLimit);
    }

    /**
     * Instantiates a sequential solver for a given problem. This instance does not use the fast upper bound.
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
     * @param dominance The dominance object that will be used to prune the search space.
     * @param <T>       The type of states.
     * @param <K>       The type of dominance keys.
     * @param timeLimit the budget of time provide to solve the problem.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> SequentialSolver<T, K> sequentialSolver(final Problem<T> problem,
                                                                 final Relaxation<T> relax,
                                                                 final VariableHeuristic<T> varh,
                                                                 final StateRanking<T> ranking,
                                                                 final WidthHeuristic<T> width,
                                                                 final Frontier<T> frontier,
                                                                 final DominanceChecker<T, K> dominance,
                                                                 final int timeLimit,
                                                                 final double gapLimit) {
        DefaultFastUpperBound<T> defaultFub = new DefaultFastUpperBound<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, defaultFub, dominance, timeLimit, gapLimit);
    }

    /**
     * Instantiates a sequential solver for a given problem. This instance does not use the fast upper bound.
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
     * @param dominance The dominance object that will be used to prune the search space.
     * @param <T>       The type of states.
     * @param <K>       The type of dominance keys.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> SequentialSolver<T, K> sequentialSolver(final Problem<T> problem,
                                                                 final Relaxation<T> relax,
                                                                 final VariableHeuristic<T> varh,
                                                                 final StateRanking<T> ranking,
                                                                 final WidthHeuristic<T> width,
                                                                 final Frontier<T> frontier,
                                                                 final DominanceChecker<T, K> dominance) {
        DefaultFastUpperBound<T> defaultFub = new DefaultFastUpperBound<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, defaultFub, dominance, Integer.MAX_VALUE, 0.0);
    }

    /**
     * Instantiates a sequential solver for a given problem. The instance do not use neither the fast upper
     * bound neither the dominance mechanism.
     *
     * @param problem  The problem we want to maximize.
     * @param relax    A suitable relaxation for the problem we want to maximize
     * @param varh     A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking  A heuristic to identify the most promising nodes.
     * @param width    A heuristic to choose the maximum width of the DD you compile.
     * @param frontier The set of nodes that must still be explored before
     *                 the problem can be considered 'solved'.
     *                 <p>
     *                 # Note:
     *                 This fringe orders the nodes by upper bound (so the highest ub is going
     *                 to pop first). So, it is guaranteed that the upper bound of the first
     *                 node being popped is an upper bound on the value reachable by exploring
     *                 any of the nodes remaining on the fringe. As a consequence, the
     *                 exploration can be stopped as soon as a node with an ub &#8804; current best
     *                 lower bound is popped.
     * @param <T>      The type of states.
     * @param timeLimit the budget of time provide to solve the problem.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolver<T, Integer> sequentialSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier,
                                                                    final int timeLimit,
                                                                    final double gapLimit) {
        DefaultFastUpperBound<T> defaultFub = new DefaultFastUpperBound<>();
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, defaultFub, defaultDominance, timeLimit, gapLimit);
    }

    /**
     * Instantiates a sequential solver for a given problem. The instance do not use neither the fast upper
     * bound neither the dominance mechanism.
     *
     * @param problem  The problem we want to maximize.
     * @param relax    A suitable relaxation for the problem we want to maximize
     * @param varh     A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking  A heuristic to identify the most promising nodes.
     * @param width    A heuristic to choose the maximum width of the DD you compile.
     * @param frontier The set of nodes that must still be explored before
     *                 the problem can be considered 'solved'.
     *                 <p>
     *                 # Note:
     *                 This fringe orders the nodes by upper bound (so the highest ub is going
     *                 to pop first). So, it is guaranteed that the upper bound of the first
     *                 node being popped is an upper bound on the value reachable by exploring
     *                 any of the nodes remaining on the fringe. As a consequence, the
     *                 exploration can be stopped as soon as a node with an ub &#8804; current best
     *                 lower bound is popped.
     * @param <T>      The type of states.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolver<T, Integer> sequentialSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier,
                                                                    final double gapLimit) {
        DefaultFastUpperBound<T> defaultFub = new DefaultFastUpperBound<>();
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, defaultFub, defaultDominance, Integer.MAX_VALUE, gapLimit);
    }

    /**
     * Instantiates a sequential solver for a given problem. The instance do not use neither the fast upper
     * bound neither the dominance mechanism.
     *
     * @param problem  The problem we want to maximize.
     * @param relax    A suitable relaxation for the problem we want to maximize
     * @param varh     A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking  A heuristic to identify the most promising nodes.
     * @param width    A heuristic to choose the maximum width of the DD you compile.
     * @param frontier The set of nodes that must still be explored before
     *                 the problem can be considered 'solved'.
     *                 <p>
     *                 # Note:
     *                 This fringe orders the nodes by upper bound (so the highest ub is going
     *                 to pop first). So, it is guaranteed that the upper bound of the first
     *                 node being popped is an upper bound on the value reachable by exploring
     *                 any of the nodes remaining on the fringe. As a consequence, the
     *                 exploration can be stopped as soon as a node with an ub &#8804; current best
     *                 lower bound is popped.
     * @param <T>      The type of states.
     * @param timeLimit the budget of time provide to solve the problem.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolver<T, Integer> sequentialSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier,
                                                                    final int timeLimit) {
        DefaultFastUpperBound<T> defaultFub = new DefaultFastUpperBound<>();
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, defaultFub, defaultDominance, timeLimit, 0.0);
    }

    /**
     * Instantiates a sequential solver for a given problem. The instance do not use neither the fast upper
     * bound neither the dominance mechanism.
     *
     * @param problem  The problem we want to maximize.
     * @param relax    A suitable relaxation for the problem we want to maximize
     * @param varh     A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking  A heuristic to identify the most promising nodes.
     * @param width    A heuristic to choose the maximum width of the DD you compile.
     * @param frontier The set of nodes that must still be explored before
     *                 the problem can be considered 'solved'.
     *                 <p>
     *                 # Note:
     *                 This fringe orders the nodes by upper bound (so the highest ub is going
     *                 to pop first). So, it is guaranteed that the upper bound of the first
     *                 node being popped is an upper bound on the value reachable by exploring
     *                 any of the nodes remaining on the fringe. As a consequence, the
     *                 exploration can be stopped as soon as a node with an ub &#8804; current best
     *                 lower bound is popped.
     * @param <T>      The type of states.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolver<T, Integer> sequentialSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier) {
        DefaultFastUpperBound<T> defaultFub = new DefaultFastUpperBound<>();
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, defaultFub, defaultDominance, Integer.MAX_VALUE, 0.0);
    }

    /**
     * Instantiates a sequential solver for a given problem. The instance do not use the dominance mechanism.
     *
     * @param problem  The problem we want to maximize.
     * @param relax    A suitable relaxation for the problem we want to maximize
     * @param varh     A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking  A heuristic to identify the most promising nodes.
     * @param width    A heuristic to choose the maximum width of the DD you compile.
     * @param frontier The set of nodes that must still be explored before
     *                 the problem can be considered 'solved'.
     *                 <p>
     *                 # Note:
     *                 This fringe orders the nodes by upper bound (so the highest ub is going
     *                 to pop first). So, it is guaranteed that the upper bound of the first
     *                 node being popped is an upper bound on the value reachable by exploring
     *                 any of the nodes remaining on the fringe. As a consequence, the
     *                 exploration can be stopped as soon as a node with an ub &#8804; current best
     *                 lower bound is popped.
     * @param fub      The heuristic defining a very rough estimation (upper bound) of the optimal value.
     * @param <T>      The type of the states.
     * @param gapLimit  The stop the search when the gat of the search reach the limit.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolver<T, Integer> sequentialSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier,
                                                                    final FastUpperBound<T> fub,
                                                                    final double gapLimit) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, fub, defaultDominance, Integer.MAX_VALUE, gapLimit);
    }

    /**
     * Instantiates a sequential solver for a given problem. The instance do not use the dominance mechanism.
     *
     * @param problem  The problem we want to maximize.
     * @param relax    A suitable relaxation for the problem we want to maximize
     * @param varh     A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking  A heuristic to identify the most promising nodes.
     * @param width    A heuristic to choose the maximum width of the DD you compile.
     * @param frontier The set of nodes that must still be explored before
     *                 the problem can be considered 'solved'.
     *                 <p>
     *                 # Note:
     *                 This fringe orders the nodes by upper bound (so the highest ub is going
     *                 to pop first). So, it is guaranteed that the upper bound of the first
     *                 node being popped is an upper bound on the value reachable by exploring
     *                 any of the nodes remaining on the fringe. As a consequence, the
     *                 exploration can be stopped as soon as a node with an ub &#8804; current best
     *                 lower bound is popped.
     * @param fub      The heuristic defining a very rough estimation (upper bound) of the optimal value.
     * @param <T>      The type of the states.
     * @param timeLimit the budget of time provide to solve the problem.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolver<T, Integer> sequentialSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier,
                                                                    final FastUpperBound<T> fub,
                                                                    final int timeLimit) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, fub, defaultDominance, timeLimit, 0.0);
    }

    /**
     * Instantiates a sequential solver for a given problem. The instance do not use the dominance mechanism.
     *
     * @param problem  The problem we want to maximize.
     * @param relax    A suitable relaxation for the problem we want to maximize
     * @param varh     A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking  A heuristic to identify the most promising nodes.
     * @param width    A heuristic to choose the maximum width of the DD you compile.
     * @param frontier The set of nodes that must still be explored before
     *                 the problem can be considered 'solved'.
     *                 <p>
     *                 # Note:
     *                 This fringe orders the nodes by upper bound (so the highest ub is going
     *                 to pop first). So, it is guaranteed that the upper bound of the first
     *                 node being popped is an upper bound on the value reachable by exploring
     *                 any of the nodes remaining on the fringe. As a consequence, the
     *                 exploration can be stopped as soon as a node with an ub &#8804; current best
     *                 lower bound is popped.
     * @param fub      The heuristic defining a very rough estimation (upper bound) of the optimal value.
     * @param <T>      The type of the states.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolver<T, Integer> sequentialSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier,
                                                                    final FastUpperBound<T> fub) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, fub, defaultDominance, Integer.MAX_VALUE, 0.0);
    }

    /**
     * Instantiates a parallel solver for a given problem. The instance does not use the dominance mechanism.
     *
     * @param nbThreads The number of threads that can be used in parallel.
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
     * @param <T>       The type of the states.
     * @return A solver for the input problem using the given configuration.
     */



    public static <T, K> ParallelSolver<T, K> parallelSolver(final int nbThreads,
                                                             final Problem<T> problem,
                                                             final Relaxation<T> relax,
                                                             final VariableHeuristic<T> varh,
                                                             final StateRanking<T> ranking,
                                                             final WidthHeuristic<T> width,
                                                             final Frontier<T> frontier,
                                                             final FastUpperBound<T> fub,
                                                             final DominanceChecker<T, K> dominance) {
        return new ParallelSolver<>(nbThreads, problem, relax, varh, ranking, width, frontier, fub, dominance);
    }

    /**
     * Instantiates a parallel solver for a given problem. The instance does not use the dominance mechanism.
     *
     * @param nbThreads The number of threads that can be used in parallel.
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
     * @param <T>       The type of the states.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> ParallelSolver<T, Integer> parallelSolver(final int nbThreads,
                                                                final Problem<T> problem,
                                                                final Relaxation<T> relax,
                                                                final VariableHeuristic<T> varh,
                                                                final StateRanking<T> ranking,
                                                                final WidthHeuristic<T> width,
                                                                final Frontier<T> frontier,
                                                                final FastUpperBound<T> fub) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new ParallelSolver<>(nbThreads, problem, relax, varh, ranking, width, frontier, fub,
                defaultDominance);
    }

    /**
     * Instantiates a parallel solver for a given problem. This instance does not use the fast upper bound.
     *
     * @param nbThreads The number of threads that can be used in parallel.
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
     * @param dominance The dominance object that will be used to prune the search space.
     * @param <T>       The type of states.
     * @param <K>       The type of dominance keys.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> ParallelSolver<T, K> parallelSolver(final int nbThreads,
                                                             final Problem<T> problem,
                                                             final Relaxation<T> relax,
                                                             final VariableHeuristic<T> varh,
                                                             final StateRanking<T> ranking,
                                                             final WidthHeuristic<T> width,
                                                             final Frontier<T> frontier,
                                                             final DominanceChecker<T, K> dominance) {
        DefaultFastUpperBound<T> defaultFub = new DefaultFastUpperBound<>();
        return new ParallelSolver<>(nbThreads, problem, relax, varh, ranking, width, frontier, defaultFub, dominance);
    }

    /**
     * Instantiates a parallel solver for a given problem. The instance does not use neither the fast upper bound
     * neither the dominance mechanism.
     *
     * @param nbThreads The number of threads that can be used in parallel.
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
     * @param <T>       The type of the states.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> ParallelSolver<T, Integer> parallelSolver(final int nbThreads,
                                                                final Problem<T> problem,
                                                                final Relaxation<T> relax,
                                                                final VariableHeuristic<T> varh,
                                                                final StateRanking<T> ranking,
                                                                final WidthHeuristic<T> width,
                                                                final Frontier<T> frontier) {
        DefaultFastUpperBound<T> defaultFub = new DefaultFastUpperBound<>();
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new ParallelSolver<>(nbThreads, problem, relax, varh, ranking, width, frontier, defaultFub,
                defaultDominance);
    }

    /**
     * Instantiates a solver using only exact mdd.
     *
     * @param problem   The problem we want to maximize.
     * @param relax     A suitable relaxation for the problem we want to maximize
     * @param varh      A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking   A heuristic to identify the most promising nodes.
     * @param fub       The heuristic defining a very rough estimation (upper bound) of the optimal value.
     * @param dominance The dominance object that will be used to prune the search space.
     * @param <T>       The type of states.
     * @param <K>       The type of dominance keys.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> ExactSolver<T, K> exactSolver(final Problem<T> problem,
                                                       final Relaxation<T> relax,
                                                       final VariableHeuristic<T> varh,
                                                       final StateRanking<T> ranking,
                                                       final FastUpperBound<T> fub,
                                                       final DominanceChecker<T, K> dominance) {
        return new ExactSolver<>(problem, relax, varh, ranking, fub, dominance);
    }

    /**
     * Instantiates a solver using only exact mdd. The instance does not use dominance.
     *
     * @param problem The problem we want to maximize.
     * @param relax   A suitable relaxation for the problem we want to maximize
     * @param varh    A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking A heuristic to identify the most promising nodes.
     * @param <T>     The type of states.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> ExactSolver<T, Integer> exactSolver(final Problem<T> problem,
                                                          final Relaxation<T> relax,
                                                          final VariableHeuristic<T> varh,
                                                          final StateRanking<T> ranking) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        DefaultFastUpperBound<T> defaultFub = new DefaultFastUpperBound<>();
        return new ExactSolver<>(problem, relax, varh, ranking, defaultFub, defaultDominance);
    }


    /**
     * Instantiates an A* solver for a given problem.
     *
     * @param problem   The problem we want to maximize.
     * @param ub        A suitable admissible upper-bound for the problem we want to maximize
     * @param varh      A heuristic to choose the next variable to branch on when developing a DD.
     * @param dominance The dominance object that will be used to prune the search space.
     * @param <T>       The type of the states.
     * @param <K>       The type of the dominance keys.
     * @return An A* solver for the input problem using the given configuration.
     */
    public static <T, K> AStarSolver<T, K> astarSolver(Problem<T> problem,
                                                       VariableHeuristic<T> varh,
                                                       FastUpperBound<T> ub,
                                                       DominanceChecker<T, K> dominance) {
        return new AStarSolver(problem, varh, ub, dominance);
    }
}
