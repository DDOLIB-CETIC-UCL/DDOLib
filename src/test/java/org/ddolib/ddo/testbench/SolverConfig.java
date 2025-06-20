package org.ddolib.ddo.testbench;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Relaxation;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.heuristics.WidthHeuristic;
import org.ddolib.ddo.implem.dominance.DominanceChecker;

public record SolverConfig<T, K>(
        Relaxation<T> relax,
        VariableHeuristic<T> varh,
        StateRanking<T> ranking,
        WidthHeuristic<T> width,
        Frontier<T> frontier,
        DominanceChecker<T, K> dominance) {
}
