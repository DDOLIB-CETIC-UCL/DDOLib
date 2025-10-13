package org.ddolib.modeling;

import org.ddolib.astar.core.solver.ACSSolver;
import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Solver<T> {
    public final SearchStatistics minimizeAstar(Model<T> model) {
        return minimizeAstar(model, s -> false);
    }

    public final SearchStatistics minimizeAstar(Model<T> model, Predicate<SearchStatistics> limit) {
        SolverConfig<T> config = new SolverConfig<>();
        config.problem = model.problem();
        config.dominance = model.dominance();
        config.flb = model.lowerBound();
        config.varh = model.variableHeuristic();
        return new AStarSolver<>(config).minimize(limit);
    }

    public final SearchStatistics minimizeDdo(DdoModel<T> model) {
        return minimizeDdo(model, stats -> false);
    }

    public final SearchStatistics minimizeDdo(DdoModel<T> model, Predicate<SearchStatistics> limit) {
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

        return new SequentialSolver<>(config).minimize(limit);
    }

    public SearchStatistics minimizeAcs(AcsModel<T> model) {
        return minimizeAcs(model, s -> false, searchStatistics -> {});
    }

    public SearchStatistics minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAcs(model, limit, searchStatistics -> {});
    }

    public SearchStatistics minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit, Consumer<SearchStatistics> onSolution) {
        SolverConfig<T> config = new SolverConfig<>();
        config.problem = model.problem();
        config.dominance = model.dominance();
        config.flb = model.lowerBound();
        config.varh = model.variableHeuristic();
        return new ACSSolver<>(config, model.columnWidth()).minimize(limit);
    }
}
