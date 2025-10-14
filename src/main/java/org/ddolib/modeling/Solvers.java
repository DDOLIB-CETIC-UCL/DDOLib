package org.ddolib.modeling;

import org.ddolib.astar.core.solver.ACSSolver;
import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.solver.ExactSolver;
import org.ddolib.ddo.core.solver.SequentialSolver;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Solvers<T> {

    public final SearchStatistics minimizeDdo(DdoModel<T> model) {
        return minimizeDdo(model, stats -> false, (sol, s) -> {
        });
    }

    public final SearchStatistics minimizeDdo(DdoModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeDdo(model, limit, (sol, s) -> {
        });
    }

    public final SearchStatistics minimizeDdo(DdoModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<Set<Decision>, SearchStatistics> onSolution) {
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
        config.verbosityLevel = model.verbosityLevel();
        return new SequentialSolver<>(config).minimize(limit, onSolution);
    }

    public final SearchStatistics minimizeAstar(Model<T> model) {
        return minimizeAstar(model, s -> false);
    }

    public final SearchStatistics minimizeAstar(Model<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAstar(model, limit, (sol, s) -> {
        });
    }

    public final SearchStatistics minimizeAstar(Model<T> model, Predicate<SearchStatistics> limit, BiConsumer<Set<Decision>, SearchStatistics> onSolution) {
        SolverConfig<T> config = new SolverConfig<>();
        config.problem = model.problem();
        config.dominance = model.dominance();
        config.flb = model.lowerBound();
        config.varh = model.variableHeuristic();
        config.verbosityLevel = model.verbosityLevel();

        return new AStarSolver<>(config).minimize(limit, onSolution);
    }

    public SearchStatistics minimizeAcs(AcsModel<T> model) {
        return minimizeAcs(model, s -> false, (sol, s) -> {
        });
    }

    public SearchStatistics minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAcs(model, limit, (sol, s) -> {
        });
    }

    public SearchStatistics minimizeAcs(AcsModel<T> model, BiConsumer<Set<Decision>, SearchStatistics> onSolution) {
        return minimizeAcs(model, s -> false, onSolution);
    }

    public SearchStatistics minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<Set<Decision>, SearchStatistics> onSolution) {
        SolverConfig<T> config = new SolverConfig<>();
        config.problem = model.problem();
        config.dominance = model.dominance();
        config.flb = model.lowerBound();
        config.varh = model.variableHeuristic();
        config.verbosityLevel = model.verbosityLevel();

        return new ACSSolver<>(config, model.columnWidth()).minimize(limit, onSolution);
    }

    public SearchStatistics minimizeExact(Model<T> model) {
        SolverConfig<T> config = new SolverConfig<>();
        config.problem = model.problem();
        config.dominance = model.dominance();
        config.flb = model.lowerBound();
        config.varh = model.variableHeuristic();
        config.verbosityLevel = model.verbosityLevel();
        return new ExactSolver<>(config).minimize(s -> false, (sol, s) -> {
        });
    }
}
