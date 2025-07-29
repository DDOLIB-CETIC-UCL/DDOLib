package org.ddolib.modeling;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;

public class SolverInput<T, K> {
    public Problem<T> problem;
    public Relaxation<T> relax;
    public FastUpperBound<T> fub;
    public VariableHeuristic<T> varh;
    public StateRanking<T> ranking;
    public DominanceChecker<T, K> dominance;

    public SolverInput(
        Problem<T> problem,
        Relaxation<T> relax,
        FastUpperBound<T> fub,
        VariableHeuristic<T> varh,
        StateRanking<T> ranking,
        DominanceChecker<T, K> dominance
    ) {
        this.problem = problem;
        this.relax = relax;
        this.fub = fub;
        this.varh = varh;
        this.ranking = ranking;
        this.dominance = dominance;
    }

    /**
     * Create a new solver input with default values where possible
     * @param problem The problem
     * @param relax Relaxation operator for the problem
     * @return The input
     */
    public static <T> SolverInput<T, Integer> defaultInput(Problem<T> problem, Relaxation<T> relax) {
        return new SolverInput<>(problem, relax, new DefaultFastUpperBound<>(), new DefaultVariableHeuristic<>(), new DefaultStateRanking<>(), new DefaultDominanceChecker<>());
    }

    /**
     * Create a new solver input with default values where possible
     * @param problem The problem
     * @param relax Relaxation operator for the problem
     * @param dominance Dominance checker for the problem
     * @return The input
     */
    public static <T, K> SolverInput<T, K> defaultInput(Problem<T> problem, Relaxation<T> relax, DominanceChecker<T, K> dominance) {
        return new SolverInput<>(problem, relax, new DefaultFastUpperBound<>(), new DefaultVariableHeuristic<>(), new DefaultStateRanking<>(), dominance);
    }
}