package org.ddolib.ddo.implem.solver;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Problem;
import org.ddolib.ddo.core.Relaxation;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.heuristics.WidthHeuristic;
import org.ddolib.ddo.implem.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.implem.dominance.DominanceChecker;

public class Solvers {

    public static <T, K> SequentialSolver<T, K> sequentialSolver(final Problem<T> problem,
                                                                 final Relaxation<T> relax,
                                                                 final VariableHeuristic<T> varh,
                                                                 final StateRanking<T> ranking,
                                                                 final WidthHeuristic<T> width,
                                                                 final DominanceChecker<T, K> dominance,
                                                                 final Frontier<T> frontier) {
        return new SequentialSolver<>(problem, relax, varh, ranking, width, dominance, frontier);
    }

    public static <T> SequentialSolver<T, Integer> sequentialSolver(final Problem<T> problem,
                                                                    final Relaxation<T> relax,
                                                                    final VariableHeuristic<T> varh,
                                                                    final StateRanking<T> ranking,
                                                                    final WidthHeuristic<T> width,
                                                                    final Frontier<T> frontier) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new SequentialSolver<>(problem, relax, varh, ranking, width, defaultDominance, frontier);
    }

    public static <K, T> ParallelSolver<T, K> parallelSolver(final int nbThreads,
                                                             final Problem<T> problem,
                                                             final Relaxation<T> relax,
                                                             final VariableHeuristic<T> varh,
                                                             final StateRanking<T> ranking,
                                                             final WidthHeuristic<T> width,
                                                             final DominanceChecker<T, K> dominance,
                                                             final Frontier<T> frontier) {
        return new ParallelSolver<>(nbThreads, problem, relax, varh, ranking, width, dominance, frontier);
    }

    public static <T> ParallelSolver<T, Integer> parallelSolver(final int nbThreads,
                                                                final Problem<T> problem,
                                                                final Relaxation<T> relax,
                                                                final VariableHeuristic<T> varh,
                                                                final StateRanking<T> ranking,
                                                                final WidthHeuristic<T> width,
                                                                final Frontier<T> frontier) {
        DefaultDominanceChecker<T> defaultDominance = new DefaultDominanceChecker<>();
        return new ParallelSolver<>(nbThreads, problem, relax, varh, ranking, width, defaultDominance, frontier);
    }
}
