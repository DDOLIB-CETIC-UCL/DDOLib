package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 嵌套动态规划模型的主程序
 * 外层：装配线平衡（最小化工位数）
 * 内层：单工位人机协同调度
 */
public class NestedSALBPDdoMain {
    public static void main(String[] args) throws IOException {
        // 数据文件
//        final String instance = args.length == 0
//                ? Path.of("data", "SALBP1", "large data set_n=100", "instance_n=100_458.alb").toString()
//                : args[0];
//        final String instance = args.length == 0
//                ? Path.of("data", "SALBP1", "medium data set_n=50", "instance_n=50_361.alb").toString()
//                : args[0];
        final String instance = args.length == 0
                ? Path.of("data", "SALBP1", "small data set_n=20", "instance_n=20_144.alb").toString()
                : args[0];
//        final String instance = args.length >= 1 ?
//                args[0] : Path.of("data", "test_5tasks_3.alb").toString();

//        final String instance = args.length >= 1 ?
//                args[0] : Path.of("src", "test", "resources", "NestedSALBP", "test_5tasks_3.alb").toString();


        // 循环时间（cycle time）
        final int cycleTime = args.length >= 2 ?
                Integer.parseInt(args[1]) : 1000;

        // 可用机器人总数
        final int totalRobots = args.length >= 3 ?
                Integer.parseInt(args[2]) : 3;

        System.out.println("=== 嵌套动态规划：一型装配线平衡 + 人机协同调度 ===");
        System.out.println("Instance: " + instance);
        System.out.println("Cycle Time: " + cycleTime);
        System.out.println("Total Robots: " + totalRobots);
        System.out.println();

        final NestedSALBPProblem problem = new NestedSALBPProblem(instance, cycleTime, totalRobots);

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
                return new FixedWidth<>(20);
            }

            @Override
            public boolean exportDot() {
                return false;
            }
        };

        Solution solution = Solvers.minimizeDdo(model,
                (sol, searchStats) -> {
                    // 每次找到更好的解时都会调用这个callback
                    System.out.println("\n===== New Incumbent Solution =====");
                    System.out.println("\n" + searchStats);

                    if (sol != null && sol.length > 0) {
                        System.out.println("Solution:" + java.util.Arrays.toString(sol));
                        System.out.println();
                        printNestedSolution(problem, sol);
                    }
                });

        System.out.println("\n" + solution.statistics());
    }

    /**
     * 打印嵌套解决方案
     */
    private static void printNestedSolution(NestedSALBPProblem problem, int[] solution) {
        System.out.println("\n=== Solution Details ===");

        // 将任务索引转换为真实序号（解码decision后 索引+1）
        int[] taskNumbers = new int[solution.length];
        for (int i = 0; i < solution.length; i++) {
            int task = solution[i] / 2;  // 解码decision
            taskNumbers[i] = task + 1;
        }
        System.out.println("Task assignment sequence: " + java.util.Arrays.toString(taskNumbers));

        // 重建状态以获取每个工位的详细信息
        NestedSALBPState state = problem.initialState();
        int stationNum = 1;

        for (int decisionVal : solution) {
            // 解码决策
            int task = decisionVal / 2;
            int robotFlag = decisionVal % 2;
            boolean assignRobot = (robotFlag == 1);

            // 判断：是否新开工位
            boolean willOpenNewStation = false;

            if (state.currentStationTasks().isEmpty()) {
                // 当前工位为空：开启新工位
                willOpenNewStation = true;
            } else {
                // 检查加入任务后是否超时
                java.util.Set<Integer> testTasks = new java.util.LinkedHashSet<>(state.currentStationTasks());
                testTasks.add(task);
                int makespan = problem.computeStationMakespan(testTasks, state.currentStationHasRobot());
                willOpenNewStation = (makespan > problem.cycleTime);
            }

            if (willOpenNewStation && !state.currentStationTasks().isEmpty()) {
                // 打印当前工位
                System.out.printf("\nStation %d:%n", stationNum);

                // 获取内层DDO的调度顺序和操作模式
                NestedSALBPProblem.InnerSolution innerSolution = problem.solveInnerProblemWithModes(
                        state.currentStationTasks(),
                        state.currentStationHasRobot()
                );

                // 将任务索引转换为真实序号（按调度顺序）
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
                    // 如果没有解，按原顺序
                    for (int t : state.currentStationTasks()) {
                        stationTaskNumbers.add(t + 1);
                    }
                }

                System.out.printf("  Tasks: %s%n", stationTaskNumbers);
                System.out.printf("  Has Robot: %s%n", state.currentStationHasRobot() ? "Yes" : "No");
                if (!taskModeDetails.isEmpty()) {
                    System.out.printf("  Task Modes: %s%n", String.join(", ", taskModeDetails));
                }
                System.out.printf("  Makespan: %d%n", problem.computeStationMakespan(state.currentStationTasks(), state.currentStationHasRobot()));

                stationNum++;
            }

            // 手动模拟状态转移
            if (!willOpenNewStation) {
                // 加入当前工位
                java.util.Set<Integer> newStationTasks = new java.util.LinkedHashSet<>(state.currentStationTasks());
                newStationTasks.add(task);

                state = new NestedSALBPState(
                        state.completedStations(),
                        newStationTasks,
                        state.currentStationHasRobot(),
                        state.usedRobots());
            } else {
                // 新开工位
                java.util.List<java.util.Set<Integer>> newCompletedStations =
                        new java.util.ArrayList<>(state.completedStations());
                int newUsedRobots = state.usedRobots();

                // 只有当前工位不为空时，才将其加入已完成列表
                if (!state.currentStationTasks().isEmpty()) {
                    newCompletedStations.add(state.currentStationTasks());
                    if (state.currentStationHasRobot()) {
                        newUsedRobots++;
                    }
                }

                java.util.Set<Integer> freshStationTasks = java.util.Set.of(task);
                state = new NestedSALBPState(
                        newCompletedStations,
                        freshStationTasks,
                        assignRobot,
                        newUsedRobots);
            }
        }

        // 打印最后一个工位
        if (!state.currentStationTasks().isEmpty()) {
            System.out.printf("\nStation %d:%n", stationNum);

            // 获取内层DDO的调度顺序和操作模式
            NestedSALBPProblem.InnerSolution innerSolution = problem.solveInnerProblemWithModes(
                    state.currentStationTasks(),
                    state.currentStationHasRobot()
            );

            // 将任务索引转换为真实序号（按调度顺序）
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
                // 如果没有解，按原顺序
                for (int t : state.currentStationTasks()) {
                    lastStationTaskNumbers.add(t + 1);
                }
            }

            System.out.printf("  Tasks: %s%n", lastStationTaskNumbers);
            System.out.printf("  Has Robot: %s%n", state.currentStationHasRobot() ? "Yes" : "No");
            if (!taskModeDetails.isEmpty()) {
                System.out.printf("  Task Modes: %s%n", String.join(", ", taskModeDetails));
            }
            System.out.printf("  Makespan: %d%n", problem.computeStationMakespan(state.currentStationTasks(), state.currentStationHasRobot()));
        }

        System.out.println("\nTotal stations used: " + state.getUsedStations());
    }
}
