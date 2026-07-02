package org.ddolib.nolayer.modeling;

import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.layered.common.solver.Solution;
import org.ddolib.nolayer.solving.acs.core.solver.AcsSolver;
import org.ddolib.nolayer.solving.astar.core.solver.AStarSolver;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Solvers {
    // Anytime Column Search (ACS) Solver Methods
    public static <T> Solution minimizeAcs(AcsModel<T> model) {
        return minimizeAcs(model, s -> false, (sol, s) -> {
        });
    }

    public static <T> Solution minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAcs(model, limit, (sol, s) -> {
        });
    }

    public static <T> Solution minimizeAcs(AcsModel<T> model, BiConsumer<int[], SearchStatistics> onSolution) {
        return minimizeAcs(model, s -> false, onSolution);
    }

    public static <T> Solution minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        return new AcsSolver<>(model).minimize(limit, onSolution);
    }

    // DDO Solver Methods
    public static <T> Solution minimizeDdo(DdoModel<T> model) {
        return minimizeDdo(model, s -> false, (sol, s) -> {
        });
    }

    public static <T> Solution minimizeDdo(DdoModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeDdo(model, limit, (sol, s) -> {
        });
    }

    public static <T> Solution minimizeDdo(DdoModel<T> model, BiConsumer<int[], SearchStatistics> onSolution) {
        return minimizeDdo(model, s -> false, onSolution);
    }

    public static <T> Solution minimizeDdo(DdoModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        return new org.ddolib.nolayer.solving.ddo.core.solver.DdoSolver<>(model).minimize(limit, onSolution);
    }

    // A* Solver Methods
    public static <T> Solution minimizeAstar(Model<T> model) {
        return minimizeAstar(model, s -> false, (sol, s) -> {
        });
    }

    public static <T> Solution minimizeAstar(Model<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAstar(model, limit, (sol, s) -> {
        });
    }

    public static <T> Solution minimizeAstar(Model<T> model, BiConsumer<int[], SearchStatistics> onSolution) {
        return minimizeAstar(model, s -> false, onSolution);
    }

    public static <T> Solution minimizeAstar(Model<T> model, Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        return new AStarSolver<>(model).minimize(limit, onSolution);
    }
}
