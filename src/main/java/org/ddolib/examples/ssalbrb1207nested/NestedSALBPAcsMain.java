package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Nested Assembly Line Balancing Problem - ACS Solver
 *
 * Solve the nested SALBP problem using Anytime Column Search (ACS) algorithm.
 *
 * Algorithm characteristics:
 *   ACS is an anytime algorithm that can be interrupted at any time and return the current best solution
 *   Uses column search strategy to gradually improve solution quality
 *   Suitable for scenarios where quick feasible solutions are needed
 *
 * Program flow:
 *   Load problem instance (task data, cycle time, robot quantity)
 *   Configure optimization strategy (capacity cuts, infeasibility cache, etc.)
 *   Define ACS model (problem, lower bound, dominance relation, column width)
 *   Run ACS algorithm
 *   Print detailed information and statistics of the solution
 */
public class NestedSALBPAcsMain {
    
    /**
     * Main program entry point
     * 
     * @param args Command line arguments
     *             args[0]: Data file path (optional, defaults to 20-task dataset)
     *             args[1]: Cycle time (optional, defaults to 1000)
     *             args[2]: Total number of robots (optional, defaults to 4)
     * @throws IOException File read exception
     */
    public static void main(String[] args) throws IOException {
        // ==================== Optimization configuration ====================
        final boolean USE_INFEASIBILITY_CACHE = true;   // Infeasibility cache
        final boolean USE_CAPACITY_CUT = true;          // Capacity cuts
        final boolean USE_BOUND_PROPAGATION = true;     // Upper bound propagation
        final boolean USE_SYMMETRY_BREAKING = true;     // Symmetry breaking

        // ==================== Data file path configuration ====================
//        final String instance = args.length == 0
//                ? Path.of("data", "generated_SALBP1", "small data set_n=20", "20_468.csv").toString()
//                : args[0];

//        final String instance = args.length == 0
//                ? Path.of("data", "generated_SALBP1", "medium data set_n=50", "50_476.csv").toString()
//                : args[0];

        final String instance = args.length == 0
                ? Path.of("data", "generated_SALBP1", "large data set_n=100", "100_11.csv").toString()
                : args[0];

        // Cycle time
        final int cycleTime = args.length >= 2 ?
                Integer.parseInt(args[1]) : 1000;

        // Total available robots
        final int totalRobots = args.length >= 3 ?
                Integer.parseInt(args[2]) : 5;

        System.out.println("=".repeat(80));
        System.out.println("Nested Assembly Line Balancing Problem - ACS Solver");
        System.out.println("=".repeat(80));
        System.out.println("Instance: " + instance);
        System.out.println("Cycle Time: " + cycleTime);
        System.out.println("Total Robots: " + totalRobots);
        System.out.println("Solver: Anytime Column Search (ACS)");
        System.out.println("=".repeat(80));
        System.out.println();

        // Create problem instance
        final NestedSALBPProblem problem = new NestedSALBPProblem(
                instance, cycleTime, totalRobots,
                USE_INFEASIBILITY_CACHE, USE_CAPACITY_CUT,
                USE_BOUND_PROPAGATION, USE_SYMMETRY_BREAKING);

        // Define ACS model
        final AcsModel<NestedSALBPState> model = new AcsModel<>() {
            @Override
            public Problem<NestedSALBPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<NestedSALBPState> lowerBound() {
                return new NestedSALBPFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<NestedSALBPState> dominance() {
                return new SimpleDominanceChecker<>(new NestedSALBPDominance(), problem.nbTasks);
            }

            @Override
            public int columnWidth() {
                // ACS column width: controls the search width for each iteration
                return 10;
            }
        };

        // Run ACS solver
        Solution solution = Solvers.minimizeAcs(model, (sol, searchStats) -> {
            // This callback is invoked every time a better solution is found
            System.out.println("\n" + "=".repeat(80));
            System.out.println("Found new optimal solution");
            System.out.println("=".repeat(80));

            // Update the best solution for upper bound propagation
            if (sol != null && sol.length > 0) {
                try {
                    int solutionValue = (int) problem.evaluate(sol);
                    problem.updateBestSolution(solutionValue);
                } catch (Exception e) {
                    // Ignore evaluation errors
                }
            }

            // Print search statistics
            System.out.println(searchStats);

            // Print detailed information about the solution
            if (sol != null && sol.length > 0) {
                System.out.println("\nSolution Vector: " + java.util.Arrays.toString(sol));
                System.out.println();
                printNestedSolution(problem, sol);
            }
        });

        // ==================== Print final results ====================
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Final Search Statistics");
        System.out.println("=".repeat(80));
        System.out.println(solution.statistics());

        // Print cache statistics
        problem.printCacheStatistics();

        // Print optimization statistics
        problem.printOptimizationStatistics();
    }

    /**
     * Print detailed information about nested solution
     * 
     * @param problem Problem instance
     * @param solution Solution vector
     */
    private static void printNestedSolution(NestedSALBPProblem problem, int[] solution) {
        System.out.println("=".repeat(80));
        System.out.println("Solution Details");
        System.out.println("=".repeat(80));

        // Convert task indices to actual task numbers
        int[] taskNumbers = new int[solution.length];
        for (int i = 0; i < solution.length; i++) {
            int task = solution[i] / 2;
            taskNumbers[i] = task + 1;
        }
        System.out.println("Task Assignment Sequence: " + java.util.Arrays.toString(taskNumbers));

        // Reconstruct state to get detailed information for each station
        NestedSALBPState state = problem.initialState();
        int stationNum = 1;

        for (int decisionVal : solution) {
            int task = decisionVal / 2;
            int robotFlag = decisionVal % 2;
            boolean assignRobot = (robotFlag == 1);

            boolean willOpenNewStation = false;

            if (state.currentStationTasks().isEmpty()) {
                willOpenNewStation = true;
            } else {
                java.util.Set<Integer> testTasks = new java.util.LinkedHashSet<>(state.currentStationTasks());
                testTasks.add(task);
                int makespan = problem.computeStationMakespan(testTasks, state.currentStationHasRobot());
                willOpenNewStation = (makespan > problem.cycleTime);
            }

            if (willOpenNewStation && !state.currentStationTasks().isEmpty()) {
                printStationDetails(problem, state, stationNum);
                stationNum++;
            }

            // State transition
            if (!willOpenNewStation) {
                java.util.Set<Integer> newStationTasks = new java.util.LinkedHashSet<>(state.currentStationTasks());
                newStationTasks.add(task);
                state = new NestedSALBPState(
                        state.completedTasks(),
                        newStationTasks,
                        state.maybeCompletedTasks(),  // Keep unchanged
                        state.currentStationHasRobot(),
                        state.usedRobots());
            } else {
                java.util.Set<Integer> newCompletedTasks = new java.util.LinkedHashSet<>(state.completedTasks());
                int newUsedRobots = state.usedRobots();

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
            printStationDetails(problem, state, stationNum);
        }

        System.out.println("\nTotal Stations: " + stationNum);
        System.out.println("=".repeat(80));
    }

    /**
     * Print detailed information about a single station
     * 
     * @param problem Problem instance
     * @param state Current state
     * @param stationNum Station number
     */
    private static void printStationDetails(NestedSALBPProblem problem, NestedSALBPState state, int stationNum) {
        System.out.println("\nStation " + stationNum + ":");

        NestedSALBPProblem.InnerSolution innerSolution = problem.solveInnerProblemWithModes(
                state.currentStationTasks(),
                state.currentStationHasRobot()
        );

        java.util.List<Integer> stationTaskNumbers = new java.util.ArrayList<>();
        java.util.List<String> taskModeDetails = new java.util.ArrayList<>();
        String[] modeNames = {"Manual", "Robot", "Cooperative"};

        if (innerSolution != null && innerSolution.tasks.length > 0) {
            for (int i = 0; i < innerSolution.tasks.length; i++) {
                int taskNum = innerSolution.tasks[i] + 1;
                int mode = innerSolution.modes[i];
                stationTaskNumbers.add(taskNum);
                taskModeDetails.add("Task" + taskNum + ": " + modeNames[mode]);
            }
        } else {
            for (int t : state.currentStationTasks()) {
                stationTaskNumbers.add(t + 1);
            }
        }

        System.out.println("  Tasks: " + stationTaskNumbers);
        System.out.println("  Robot: " + (state.currentStationHasRobot() ? "Yes" : "No"));
        if (!taskModeDetails.isEmpty()) {
            System.out.println("  Task Modes: " + String.join(", ", taskModeDetails));
        }
        System.out.println("  Completion Time: " + problem.computeStationMakespan(state.currentStationTasks(), state.currentStationHasRobot()));
    }
}

