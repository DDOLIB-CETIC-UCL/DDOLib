package org.ddolib.modeling.layered;

import org.ddolib.solving.acs.core.solver.layered.AcsSolver;
import org.ddolib.solving.astar.core.solver.layered.AStarSolver;
import org.ddolib.solving.awastar.core.solver.layered.AwAstarSolver;
import org.ddolib.common.solver.Solution;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.solving.ddo.core.solver.layered.ExactSolver;
import org.ddolib.solving.ddo.core.solver.layered.RelaxationSolver;
import org.ddolib.solving.ddo.core.solver.layered.RestrictionSolver;
import org.ddolib.solving.ddo.core.solver.layered.SequentialSolver;
import org.ddolib.solving.lns.core.solver.layered.LnsSolver;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Solvers {
    // DDO Solver Methods
    public static <T> Solution minimizeDdo(DdoModel<T> model) {
        return minimizeDdo(model, stats -> false, (sol, s) -> {});
    }
    public static <T> Solution minimizeDdo(DdoModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeDdo(model, limit, (sol, s) -> {});
    }
    public static <T> Solution minimizeDdo(DdoModel<T> model, BiConsumer<int[], SearchStatistics> onSolution) {
        return minimizeDdo(model, s -> false, onSolution);
    }
    public static <T> Solution minimizeDdo(DdoModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        return new SequentialSolver<>(model).minimize(limit, onSolution);
    }

    public static <T> Solution relaxedDdo(DdoModel<T> model) {
        return relaxedDdo(model, stats -> false, (sol, s) -> {});
    }
    public static <T> Solution relaxedDdo(DdoModel<T> model, Predicate<SearchStatistics> limit) {
        return relaxedDdo(model, limit, (sol, s) -> {});
    }
    public static <T> Solution relaxedDdo(DdoModel<T> model, BiConsumer<int[], SearchStatistics> onSolution) {
        return relaxedDdo(model, s -> false, onSolution);
    }
    public static <T> Solution relaxedDdo(DdoModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        return new RelaxationSolver<>(model).minimize(limit, onSolution);
    }

    public static <T> Solution restrictedDdo(DdoModel<T> model) {
        return restrictedDdo(model, stats -> false, (sol, s) -> {});
    }
    public static <T> Solution restrictedDdo(DdoModel<T> model, Predicate<SearchStatistics> limit) {
        return restrictedDdo(model, limit, (sol, s) -> {});
    }
    public static <T> Solution restrictedDdo(DdoModel<T> model, BiConsumer<int[], SearchStatistics> onSolution) {
        return restrictedDdo(model, s -> false, onSolution);
    }
    public static <T> Solution restrictedDdo(DdoModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        return new RestrictionSolver<>(model).minimize(limit, onSolution);
    }

    // A* Solver Methods
    public static <T> Solution minimizeAstar(Model<T> model) {
        return minimizeAstar(model, s -> false, (sol, s) -> {});
    }
    public static <T> Solution minimizeAstar(Model<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAstar(model, limit, (sol, s) -> {});
    }
    public static <T> Solution minimizeAstar(Model<T> model, BiConsumer<int[], SearchStatistics> onSolution) {
        return minimizeAstar(model, s -> false, onSolution);
    }
    public static <T> Solution minimizeAstar(Model<T> model, Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        return new AStarSolver<>(model).minimize(limit, onSolution);
    }

    // Anytime Column Search (ACS) Solver Methods
    public static <T> Solution minimizeAcs(AcsModel<T> model) {
        return minimizeAcs(model, s -> false, (sol, s) -> {});
    }
    public static <T> Solution minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAcs(model, limit, (sol, s) -> {});
    }
    public static <T> Solution minimizeAcs(AcsModel<T> model, BiConsumer<int[], SearchStatistics> onSolution) {
        return minimizeAcs(model, s -> false, onSolution);
    }
    public static <T> Solution minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        return new AcsSolver<>(model).minimize(limit, onSolution);
    }

    // Anytime Weighted A* Solver Methods
    public static <T> Solution minimizeAwAStar(AwAstarModel<T> model) {
        return new AwAstarSolver<>(model).minimize(s -> false, (sol, s) -> {});
    }
    public static <T> Solution minimizeAwAStar(AwAstarModel<T> model, Predicate<SearchStatistics> limit) {
        return new AwAstarSolver<>(model).minimize(limit, (sol, s) -> {});
    }
    public static <T> Solution minimizeAwAStar(AwAstarModel<T> model, BiConsumer<int[], SearchStatistics> onSolution) {
        return new AwAstarSolver<>(model).minimize(s -> false, onSolution);
    }
    public static <T> Solution minimizeAwAStar(AwAstarModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        return new AwAstarSolver<>(model).minimize(limit, onSolution);
    }

    // Exact (DDO) Solver Methods
    public static <T> Solution minimizeExact(ExactModel<T> model) {
        return new ExactSolver<>(model).minimize(s -> false, (sol, s) -> {});
    }
    public static <T> Solution minimizeExact(ExactModel<T> model, BiConsumer<int[], SearchStatistics> onSolution) {
        return new ExactSolver<>(model).minimize(s -> false, onSolution);
    }

    // LNS
    public static <T> Solution minimizeLns(LnsModel<T> model) {
        return minimizeLns(model, stats -> false, (sol, s) -> {});
    }
    public static <T> Solution minimizeLns(LnsModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeLns(model, limit, (sol, s) -> {});
    }
    public static <T> Solution minimizeLns(LnsModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        return new LnsSolver<>(model).minimize(limit, onSolution);
    }
}
