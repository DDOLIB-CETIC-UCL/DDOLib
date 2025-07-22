package org.ddolib.ddo.implem.solver;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.*;
import org.ddolib.ddo.implem.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.implem.dominance.DominanceChecker;
import org.ddolib.ddo.implem.heuristics.DefaultStateCoordinates;
import org.ddolib.ddo.implem.heuristics.DefaultStateDistance;

import java.util.Random;

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
                                                                 final DominanceChecker<T, K> dominance,
                                                                 final RelaxationStrat relaxStrat,
                                                                 final RestrictionStrat restrictionStrat,
                                                                 final StateDistance<T> distance,
                                                                 final StateCoordinates<T> coord,
                                                                 final int seed) {
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, dominance, relaxStrat, restrictionStrat, distance, coord, seed);
    }

    /**
     * Instantiates a sequential solver for a given problem. The instance uses the standard relaxation mechanism.
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
     * @param <T>      The type of the states.
     * @param <K>       The type of the dominance keys.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> SequentialSolver<T, K> sequentialSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier,
                                                                    final DominanceChecker<T, K> dominance) {
        RelaxationStrat relaxStrat = RelaxationStrat.Cost;
        RestrictionStrat restrictionStrat = RestrictionStrat.Cost;
        DefaultStateDistance<T> distance = new DefaultStateDistance<>();
        DefaultStateCoordinates<T> coord = new DefaultStateCoordinates<>();
        // TODO change where random is init or maybe remove it
        Random random = new Random();
        int seed = random.nextInt();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, dominance, relaxStrat, restrictionStrat, distance, coord, seed);
    }

    /**
     * Instantiates a sequential solver for a given problem. The instance does not use the dominance mechanism.
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
     * @param <T>      The type of the states.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> SequentialSolver<T, Integer> sequentialSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier,
                                                                    final RelaxationStrat relaxStrat,
                                                                    final RestrictionStrat restrictionStrat,
                                                                    final StateDistance<T> distance,
                                                                    final StateCoordinates<T> coord,
                                                                    final int seed) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, defaultDominance, relaxStrat, restrictionStrat, distance, coord, seed);
    }

    /**
     * Instantiates a sequential solver for a given problem. The instance does not use the dominance mechanism and use standard relaxation mechanism.
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
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        RelaxationStrat relaxStrat = RelaxationStrat.Cost;
        RestrictionStrat restrictionStrat = RestrictionStrat.Cost;
        DefaultStateDistance<T> distance = new DefaultStateDistance<>();
        DefaultStateCoordinates<T> coord = new DefaultStateCoordinates<>();
        // TODO change where random is init or maybe remove it
        Random random = new Random();
        int seed = random.nextInt();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, frontier, defaultDominance, relaxStrat, restrictionStrat, distance, coord, seed);
    }

    /**
     * Instantiates a parallel solver for a given problem.
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
                                                             final DominanceChecker<T, K> dominance,
                                                             final RelaxationStrat relaxStrat,
                                                             final RestrictionStrat restrictionStrat,
                                                             final StateDistance<T> distance,
                                                             final StateCoordinates<T> coord,
                                                             final int seed) {
        return new ParallelSolver<>(nbThreads, problem, relax, varh, ranking, width, frontier, dominance, relaxStrat, restrictionStrat, distance, coord, seed);
    }

    /**
     * Instantiates a parallel solver for a given problem. The instance uses the standard relaxation mechanism.
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
     * @param <T>      The type of the states.
     * @param <K>       The type of the dominance keys.
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
        RelaxationStrat relaxStrat = RelaxationStrat.Cost;
        RestrictionStrat restrictionStrat = RestrictionStrat.Cost;
        DefaultStateDistance<T> distance = new DefaultStateDistance<>();
        DefaultStateCoordinates<T> coord = new DefaultStateCoordinates<>();
        // TODO change where random is init or maybe remove it
        Random random = new Random();
        int seed = random.nextInt();
        return new ParallelSolver<>(nbThreads, problem, relax, varh, ranking, width, frontier, dominance, relaxStrat, restrictionStrat, distance, coord, seed);
    }

    /**
     * Instantiates a parallel solver for a given problem. The instance does not use the dominance mechanism.
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
     * @param <T>      The type of the states.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> ParallelSolver<T, Integer> parallelSolver(final int nbThreads,
                                                                  final Problem<T> problem,
                                                                  final Relaxation<T> relax,
                                                                  final VariableHeuristic<T> varh,
                                                                  final StateRanking<T> ranking,
                                                                  final WidthHeuristic<T> width,
                                                                  final Frontier<T> frontier,
                                                                  final RelaxationStrat relaxStrat,
                                                                  final RestrictionStrat restrictionStrat,
                                                                  final StateDistance<T> distance,
                                                                  final StateCoordinates<T> coord,
                                                                  final int seed) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new ParallelSolver<>(nbThreads, problem, relax, varh, ranking, width, frontier, defaultDominance, relaxStrat, restrictionStrat, distance, coord, seed);
    }

    /**
     * Instantiates a parallel solver for a given problem. The instance does not use the dominance mechanism and use the standard relaxation mechanism.
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
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        RelaxationStrat relaxStrat = RelaxationStrat.Cost;
        RestrictionStrat restrictionStrat = RestrictionStrat.Cost;
        DefaultStateDistance<T> distance = new DefaultStateDistance<>();
        DefaultStateCoordinates<T> coord = new DefaultStateCoordinates<>();
        // TODO change where random is init or maybe remove it
        Random random = new Random();
        int seed = random.nextInt();
        return new ParallelSolver<>(nbThreads, problem, relax, varh, ranking, width, frontier, defaultDominance, relaxStrat, restrictionStrat, distance, coord, seed);
    }

    /**
     * Instantiates a relaxation solver for a given problem.
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
     * @param <T>       The type of the states.
     * @param <K>       The type of the dominance keys.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> RelaxationSolver<T, K> relaxationSolver(final Problem<T> problem,
                                                                 final Relaxation<T> relax,
                                                                 final VariableHeuristic<T> varh,
                                                                 final StateRanking<T> ranking,
                                                                 final WidthHeuristic<T> width,
                                                                 final Frontier<T> frontier,
                                                                 final DominanceChecker<T, K> dominance,
                                                                 final RelaxationStrat relaxStrat,
                                                                 final StateDistance<T> distance,
                                                                 final StateCoordinates<T> coord,
                                                                 final int seed) {
        return new RelaxationSolver<>(problem, relax, varh, ranking, width, frontier, dominance, relaxStrat, distance, coord, seed);
    }

    /**
     * Instantiates a relaxation solver for a given problem. The instance uses the standard relaxation mechanism.
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
     * @param <T>      The type of the states.
     * @param <K>       The type of the dominance keys.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> RelaxationSolver<T, K> relaxationSolver(final Problem<T> problem,
                                                             final Relaxation<T> relax,
                                                             final VariableHeuristic<T> varh,
                                                             final StateRanking<T> ranking,
                                                             final WidthHeuristic<T> width,
                                                             final Frontier<T> frontier,
                                                             final DominanceChecker<T, K> dominance) {
        RelaxationStrat relaxStrat = RelaxationStrat.Cost;
        RestrictionStrat restrictionStrat = RestrictionStrat.Cost;
        DefaultStateDistance<T> distance = new DefaultStateDistance<>();
        DefaultStateCoordinates<T> coord = new DefaultStateCoordinates<>();
        // TODO change where random is init or maybe remove it
        Random random = new Random();
        int seed = random.nextInt();
        return new RelaxationSolver<>(problem, relax, varh, ranking, width, frontier, dominance, relaxStrat, distance, coord, seed);
    }

    /**
     * Instantiates a relaxation solver for a given problem. The instance does not use the dominance mechanism.
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
     * @param <T>      The type of the states.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> RelaxationSolver<T, Integer> relaxationSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier,
                                                                    final RelaxationStrat relaxStrat,
                                                                    final StateDistance<T> distance,
                                                                    final StateCoordinates<T> coord,
                                                                    final int seed) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new RelaxationSolver<>(problem, relax, varh, ranking, width, frontier, defaultDominance, relaxStrat, distance, coord, seed);
    }

    /**
     * Instantiates a relaxation solver for a given problem. The instance does not use the dominance mechanism and uses the standard relaxation mechanism.
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
     * @param <T>      The type of the states.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T> RelaxationSolver<T, Integer> relaxationSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        RelaxationStrat relaxStrat = RelaxationStrat.Cost;
        RestrictionStrat restrictionStrat = RestrictionStrat.Cost;
        DefaultStateDistance<T> distance = new DefaultStateDistance<>();
        DefaultStateCoordinates<T> coord = new DefaultStateCoordinates<>();
        // TODO change where random is init or maybe remove it
        Random random = new Random();
        int seed = random.nextInt();
        return new RelaxationSolver<>(problem, relax, varh, ranking, width, frontier, defaultDominance, relaxStrat, distance, coord, seed);
    }

    /**
     * Instantiates a solver using only exact mdd.
     *
     * @param problem   The problem we want to maximize.
     * @param relax     A suitable relaxation for the problem we want to maximize
     * @param varh      A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking   A heuristic to identify the most promising nodes.
     * @param dominance The dominance object that will be used to prune the search space.
     * @param <T>       The type of states.
     * @param <K>       The type of dominance keys.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> ExactSolver<T, K> exactSolver(final Problem<T> problem,
                                                       final Relaxation<T> relax,
                                                       final VariableHeuristic<T> varh,
                                                       final StateRanking<T> ranking,
                                                       final DominanceChecker<T, K> dominance) {
        return new ExactSolver<>(problem, relax, varh, ranking, dominance);
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
        return new ExactSolver<>(problem, relax, varh, ranking, defaultDominance);
    }

    /**
     * Instantiates a restriction solver for a given problem.
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
     * @param <T>       The type of the states.
     * @param <K>       The type of the dominance keys.
     * @return A solver for the input problem using the given configuration.
     */
    public static <T, K> RestrictionSolver<T, K> restrictionSolver(final Problem<T> problem,
                                                                 final Relaxation<T> relax,
                                                                 final VariableHeuristic<T> varh,
                                                                 final StateRanking<T> ranking,
                                                                 final WidthHeuristic<T> width,
                                                                 final Frontier<T> frontier,
                                                                 final DominanceChecker<T, K> dominance,
                                                                 final RestrictionStrat restrictionStrat,
                                                                 final StateDistance<T> distance,
                                                                 final StateCoordinates<T> coord,
                                                                 final int seed) {
        return new RestrictionSolver<>(problem, relax, varh, ranking, width, frontier, dominance, restrictionStrat, distance, coord, seed);
    }
}
