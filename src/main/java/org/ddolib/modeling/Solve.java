package org.ddolib.modeling;

import org.ddolib.astar.core.solver.ACSSolver;
import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.cache.Cache;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;

public class Solve<T> {
    public final SearchStatistics minimize(Model<T> model) {
        SolverConfig<T> config = new SolverConfig<>();
        config.problem = model.problem();
        config.dominance = model.dominance();
        config.flb = model.lowerBound();
        config.varh = model.variableHeuristic();
        return new AStarSolver<>(config).minimize();
    }

    public final SearchStatistics minimizeDdo(DdoModel<T> model) {
        SolverConfig<T> config = new SolverConfig<>();
        config.problem = model.problem();
        config.relax = model.relaxation();
        config.ranking = model.ranking();
        config.width = model.widthHeuristic();
        config.varh = model.variableHeuristic();
        config.flb = model.lowerBound();
        config.dominance = model.dominance();
        config.frontier = model.frontier();
        config.cache = (model.useCache()) ? new SimpleCache<>() : null;

        return new SequentialSolver<>(config).minimize();
    }

    public SearchStatistics minimizeAcs(AcsModel<T> model) {
        SolverConfig<T> config = new SolverConfig<>();
        config.problem = model.problem();
        config.dominance = model.dominance();
        config.flb = model.lowerBound();
        config.varh = model.variableHeuristic();
        return new ACSSolver<>(config, model.columnWidth()).minimize();
    }

    public void onSolution(SearchStatistics statistics) {
        System.out.println("Statistics ");
        System.out.println(statistics.toString());
    }
}
