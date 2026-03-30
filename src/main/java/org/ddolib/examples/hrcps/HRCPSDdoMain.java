package org.ddolib.examples.hrcps;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Solves the HRCPS problem (minimise number of stations) using the DDO solver.
 */
public class HRCPSDdoMain {

    public static void main(String[] args) throws IOException {
        final boolean USE_INFEASIBILITY_CACHE = true;
        final boolean USE_CAPACITY_CUT = true;
        final boolean USE_BOUND_PROPAGATION = true;
        final boolean USE_SYMMETRY_BREAKING = true;

        final String instance = args.length >= 1 ? args[0]
                : Path.of("data", "HRCP", "small data set_n=20", "20_1").toString();

        final int cycleTime = args.length >= 2 ? Integer.parseInt(args[1]) : 1000;

        System.out.println("=".repeat(80));
        System.out.println("HRCPS – Minimise Stations (DDO Solver)");
        System.out.println("=".repeat(80));
        System.out.println("Instance:   " + instance);
        System.out.println("Cycle Time: " + cycleTime);
        System.out.println("=".repeat(80));
        System.out.println();

        final HRCPSProblem problem = new HRCPSProblem(instance, cycleTime,
                USE_INFEASIBILITY_CACHE, USE_CAPACITY_CUT,
                USE_BOUND_PROPAGATION, USE_SYMMETRY_BREAKING);

        final DdoModel<HRCPSState> model = new DdoModel<>() {
            @Override public Problem<HRCPSState> problem() { return problem; }
            @Override public Relaxation<HRCPSState> relaxation() { return new HRCPSRelax(); }
            @Override public StateRanking<HRCPSState> ranking() { return new HRCPSRanking(); }
            @Override public FastLowerBound<HRCPSState> lowerBound() { return new HRCPSFastLowerBound(problem); }
            @Override public WidthHeuristic<HRCPSState> widthHeuristic() { return new FixedWidth<>(10); }
            @Override public DominanceChecker<HRCPSState> dominance() {
                return new SimpleDominanceChecker<>(new HRCPSDominance(), problem.nbTasks);
            }
            @Override public Frontier<HRCPSState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }
            @Override public boolean useCache() { return true; }
            @Override public boolean exportDot() { return false; }
        };

        Solution solution = Solvers.minimizeDdo(model, (sol, stats) -> {
            System.out.println("\n===== New Incumbent =====");
            if (sol != null && sol.length > 0) {
                try {
                    int val = (int) problem.evaluate(sol);
                    problem.updateBestSolution(val);
                } catch (Exception ignored) {}
            }
            System.out.println(stats);
            if (sol != null && sol.length > 0) {
                printSolution(problem, sol);
            }
        });

        System.out.println("\n" + solution.statistics());
        problem.printCacheStatistics();
        problem.printOptimizationStatistics();
    }

    private static void printSolution(HRCPSProblem problem, int[] solution) {
        System.out.println("\n--- Solution Details ---");
        int[] taskNumbers = new int[solution.length];
        for (int i = 0; i < solution.length; i++) taskNumbers[i] = solution[i] + 1;
        System.out.println("Task sequence: " + Arrays.toString(taskNumbers));

        HRCPSState state = problem.initialState();
        int stationNum = 1;
        String[] modeNames = {"Human", "Robot", "Collab"};

        for (int task : solution) {
            boolean needNew;
            if (state.currentStationTasks().isEmpty()) {
                needNew = true;
            } else {
                Set<Integer> test = new LinkedHashSet<>(state.currentStationTasks());
                test.add(task);
                needNew = !problem.isStationSchedulable(test);
            }

            if (needNew && !state.currentStationTasks().isEmpty()) {
                printStation(problem, state.currentStationTasks(), stationNum, modeNames);
                stationNum++;
            }

            if (!needNew) {
                Set<Integer> newStation = new LinkedHashSet<>(state.currentStationTasks());
                newStation.add(task);
                state = new HRCPSState(state.completedTasks(), newStation, state.maybeCompletedTasks());
            } else {
                Set<Integer> newCompleted = new LinkedHashSet<>(state.completedTasks());
                if (!state.currentStationTasks().isEmpty()) newCompleted.addAll(state.currentStationTasks());
                state = new HRCPSState(newCompleted, Set.of(task), state.maybeCompletedTasks());
            }
        }
        if (!state.currentStationTasks().isEmpty()) {
            printStation(problem, state.currentStationTasks(), stationNum, modeNames);
        }
        System.out.println("Total stations: " + stationNum);
    }

    private static void printStation(HRCPSProblem problem, Set<Integer> tasks, int num, String[] modeNames) {
        System.out.printf("\nStation %d:%n", num);
        HRCPSProblem.InnerSolution inner = problem.solveInnerProblemWithModes(tasks);
        List<String> details = new ArrayList<>();
        if (inner != null) {
            for (int i = 0; i < inner.tasks.length; i++) {
                details.add("Task" + (inner.tasks[i] + 1) + ":" + modeNames[inner.modes[i]]);
            }
        } else {
            for (int t : tasks) details.add("Task" + (t + 1));
        }
        System.out.println("  Tasks: " + details);
    }
}

