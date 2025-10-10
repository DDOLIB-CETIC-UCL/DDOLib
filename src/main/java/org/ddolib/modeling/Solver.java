package org.ddolib.modeling;

import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.profiling.SearchStatistics;

public class Solver {

    public static <T> SearchStatistics minimize(Model<T> model) {
        SolverConfig<T> config = new SolverConfig<>();
        config.problem = model.problem();
        config.dominance = model.dominance();
        config.flb = model.lowerBound();
        config.varh = model.variableHeuristic();

        return new AStarSolver<>(config).minimize();
    }
}
