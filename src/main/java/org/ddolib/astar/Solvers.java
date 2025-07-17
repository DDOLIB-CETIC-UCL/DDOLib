package org.ddolib.astar;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Problem;
import org.ddolib.ddo.core.Relaxation;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.heuristics.WidthHeuristic;
import org.ddolib.ddo.implem.dominance.DominanceChecker;
import org.ddolib.ddo.implem.solver.SequentialSolver;

public class Solvers {

    public static <T, K> AStarSolver<T, K> astarSolver(Problem<T> problem,
                                                       Relaxation<T> relax,
                                                       VariableHeuristic<T> varh,
                                                       DominanceChecker<T, K> dominance) {
        return new AStarSolver(problem, relax, varh, dominance);
    }
}
