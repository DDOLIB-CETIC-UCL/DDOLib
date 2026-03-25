package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Main program for nested dynamic programming model
 * Outer: Assembly line balancing (minimize number of stations)
 * Inner: Single-station human-robot collaborative scheduling
 */
public class NestedSALBPDdoMain {
    public static void main(String[] args) throws IOException {
        // ==================== Optimization switch configuration ====================
        // Set to true to enable optimization, false to disable (for comparison experiments)
        final boolean USE_INFEASIBILITY_CACHE = true;   // ← Infeasibility cache
        final boolean USE_CAPACITY_CUT = true;          // ← Capacity cut
        final boolean USE_BOUND_PROPAGATION = true;     // ← Bound propagation
        final boolean USE_SYMMETRY_BREAKING = true;     // ← Symmetry breaking

        // ==================== Data file path configuration ====================
        // Supports two formats:
        // 1. .csv format (new format): task,th,tr,tc,successor
        // 2. .alb format (original format)

        // [Currently using] CSV format - 20-task dataset
//        final String instance = args.length == 0
//                ? Path.of("data", "generated_SALBP1", "small data set_n=20", "20_324.csv").toString()
//                : args[0];

//        final String instance = args.length == 0
//                ? Path.of("data", "generated_SALBP1", "medium data set_n=50", "50_476.csv").toString()
//                : args[0];

        final String instance = args.length == 0
                ? Path.of("data", "generated_SALBP1", "large data set_n=100", "100_11.csv").toString()
                : args[0];

        // [Alternative] ALB format - dataset
//        final String instance = args.length == 0
//                ? Path.of("data", "SALBP1", "small data set_n=20", "instance_n=20_106.alb").toString()
//                : args[0];

        // Cycle time
        final int cycleTime = args.length >= 2 ?
                Integer.parseInt(args[1]) : 1000;

        // Total available robots
        final int totalRobots = args.length >= 3 ?
                Integer.parseInt(args[2]) : 5;


        System.out.println("Instance: " + instance);
        System.out.println("Cycle Time: " + cycleTime);
        System.out.println("Total Robots: " + totalRobots);
        System.out.println();

        final NestedSALBPProblem problem = new NestedSALBPProblem(instance, cycleTime, totalRobots,
                USE_INFEASIBILITY_CACHE, USE_CAPACITY_CUT,
                USE_BOUND_PROPAGATION, USE_SYMMETRY_BREAKING);

        final DdoModel<NestedSALBPState> model = new DdoModel<>() {
            @Override
            public Problem<NestedSALBPState> problem() {
                return problem;
            }

            @Override
            public Relaxation<NestedSALBPState> relaxation() {
                return new NestedSALBPRelax();
            }

            @Override
            public StateRanking<NestedSALBPState> ranking() {
                return new NestedSALBPRanking(problem.totalRobots);
            }

            @Override
            public FastLowerBound<NestedSALBPState> lowerBound() {
                return new NestedSALBPFastLowerBound(problem);
            }

            @Override
            public WidthHeuristic<NestedSALBPState> widthHeuristic() {
                return new FixedWidth<>(10);
            }

            @Override
            public DominanceChecker<NestedSALBPState> dominance() {
                return new SimpleDominanceChecker<>(new NestedSALBPDominance(), problem.nbTasks);
            }

            @Override
            public Frontier<NestedSALBPState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public boolean exportDot() {
                return false;
            }
        };

        Solution solution = Solvers.minimizeDdo(model,
                (sol, searchStats) -> {
                    // Callback invoked every time a better solution is found
                    System.out.println("\n===== New Incumbent Solution =====");

                    // Update best solution for bound propagation
                    if (sol != null && sol.length > 0) {
                        try {
                            int solutionValue = (int) problem.evaluate(sol);
                            problem.updateBestSolution(solutionValue);
                        } catch (Exception e) {
                            // Ignore evaluation errors
                        }
                    }

                    // Calculate global lower bound
                    double globalLB = searchStats.incumbent() * (1.0 - searchStats.gap() / 100.0);
                    System.out.printf("Global Lower Bound: %.2f (from gap calculation)%n", globalLB);

                    System.out.println("\n" + searchStats);

                    if (sol != null && sol.length > 0) {
                        System.out.println("Solution:" + java.util.Arrays.toString(sol));
                        System.out.println();
                        printNestedSolution(problem, sol);
                    }
                });

        System.out.println("\n" + solution.statistics());

        // Print cache statistics
        problem.printCacheStatistics();

        // Print optimization statistics
        problem.printOptimizationStatistics();
    }

    /**
     * Print nested solution details
     */
    private static void printNestedSolution(NestedSALBPProblem problem, int[] solution) {
        System.out.println("\n=== Solution Details ===");

        // Convert task indices to real numbers (after decoding decision, index+1)
        int[] taskNumbers = new int[solution.length];
        for (int i = 0; i < solution.length; i++) {
            int task = solution[i] / 2;  // Decode decision
            taskNumbers[i] = task + 1;
        }
        System.out.println("Task assignment sequence: " + java.util.Arrays.toString(taskNumbers));

        // Reconstruct state to get detailed information for each station
        NestedSALBPState state = problem.initialState();
        int stationNum = 1;

        for (int decisionVal : solution) {
            // Decode decision
            int task = decisionVal / 2;
            int robotFlag = decisionVal % 2;
            boolean assignRobot = (robotFlag == 1);

            // Check: whether to open new station
            boolean willOpenNewStation = false;

            if (state.currentStationTasks().isEmpty()) {
                // Current station is empty: open new station
                willOpenNewStation = true;
            } else {
                // Check if adding task exceeds time limit
                java.util.Set<Integer> testTasks = new java.util.LinkedHashSet<>(state.currentStationTasks());
                testTasks.add(task);
                willOpenNewStation = !problem.isStationSchedulable(testTasks, state.currentStationHasRobot());
            }

            if (willOpenNewStation && !state.currentStationTasks().isEmpty()) {
                // Print current station
                System.out.printf("\nStation %d:%n", stationNum);

                // Get scheduling order and operation modes from inner DDO
                NestedSALBPProblem.InnerSolution innerSolution = problem.solveInnerProblemWithModes(
                        state.currentStationTasks(),
                        state.currentStationHasRobot()
                );

                // Convert task indices to real numbers (in scheduling order)
                java.util.List<Integer> stationTaskNumbers = new java.util.ArrayList<>();
                java.util.List<String> taskModeDetails = new java.util.ArrayList<>();
                String[] modeNames = {"Human", "Robot", "Collaboration"};

                if (innerSolution != null && innerSolution.tasks.length > 0) {
                    for (int i = 0; i < innerSolution.tasks.length; i++) {
                        int taskNum = innerSolution.tasks[i] + 1;
                        int mode = innerSolution.modes[i];
                        stationTaskNumbers.add(taskNum);
                        taskModeDetails.add("Task " + taskNum + ": " + modeNames[mode]);
                    }
                } else {
                    // If no solution, use original order
                    for (int t : state.currentStationTasks()) {
                        stationTaskNumbers.add(t + 1);
                    }
                }

                System.out.printf("  Tasks: %s%n", stationTaskNumbers);
                System.out.printf("  Has Robot: %s%n", state.currentStationHasRobot() ? "Yes" : "No");
                if (!taskModeDetails.isEmpty()) {
                    System.out.printf("  Task Modes: %s%n", String.join(", ", taskModeDetails));
                }

                stationNum++;
            }

            // Manually simulate state transition
            if (!willOpenNewStation) {
                // Join current station
                java.util.Set<Integer> newStationTasks = new java.util.LinkedHashSet<>(state.currentStationTasks());
                newStationTasks.add(task);

                state = new NestedSALBPState(
                        state.completedTasks(),
                        newStationTasks,
                        state.maybeCompletedTasks(),  // Keep unchanged
                        state.currentStationHasRobot(),
                        state.usedRobots());
            } else {
                // Open new station
                java.util.Set<Integer> newCompletedTasks =
                        new java.util.LinkedHashSet<>(state.completedTasks());
                int newUsedRobots = state.usedRobots();

                // Only add tasks to completed set if current station is not empty
                if (!state.currentStationTasks().isEmpty()) {
                    newCompletedTasks.addAll(state.currentStationTasks());
                    if (state.currentStationHasRobot()) {
                        newUsedRobots++;
                    }
                }

                java.util.Set<Integer> freshStationTasks = java.util.Set.of(task);
                state = new NestedSALBPState(
                        newCompletedTasks,
                        freshStationTasks,
                        state.maybeCompletedTasks(),  // Keep unchanged
                        assignRobot,
                        newUsedRobots);
            }
        }

        // Print last station
        if (!state.currentStationTasks().isEmpty()) {
            System.out.printf("\nStation %d:%n", stationNum);

            // Get scheduling order and operation modes from inner DDO
            NestedSALBPProblem.InnerSolution innerSolution = problem.solveInnerProblemWithModes(
                    state.currentStationTasks(),
                    state.currentStationHasRobot()
            );

            // Convert task indices to real numbers (in scheduling order)
            java.util.List<Integer> lastStationTaskNumbers = new java.util.ArrayList<>();
            java.util.List<String> taskModeDetails = new java.util.ArrayList<>();
            String[] modeNames = {"Human", "Robot", "Collaboration"};

            if (innerSolution != null && innerSolution.tasks.length > 0) {
                for (int i = 0; i < innerSolution.tasks.length; i++) {
                    int taskNum = innerSolution.tasks[i] + 1;
                    int mode = innerSolution.modes[i];
                    lastStationTaskNumbers.add(taskNum);
                    taskModeDetails.add("Task " + taskNum + ": " + modeNames[mode]);
                }
            } else {
                // If no solution, use original order
                for (int t : state.currentStationTasks()) {
                    lastStationTaskNumbers.add(t + 1);
                }
            }

            System.out.printf("  Tasks: %s%n", lastStationTaskNumbers);
            System.out.printf("  Has Robot: %s%n", state.currentStationHasRobot() ? "Yes" : "No");
            if (!taskModeDetails.isEmpty()) {
                System.out.printf("  Task Modes: %s%n", String.join(", ", taskModeDetails));
            }
        }

        System.out.println("\nTotal stations used: " + stationNum);
    }
}
