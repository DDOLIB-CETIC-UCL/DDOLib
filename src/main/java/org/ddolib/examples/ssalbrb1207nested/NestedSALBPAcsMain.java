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
 * 嵌套装配线平衡问题 - ACS求解器
 * 
 * 使用 Anytime Column Search (ACS) 算法求解嵌套SALBP问题。
 * 
 * 算法特点：
 *   ACS是一种任意时间算法，可以在任何时候中断并返回当前最优解
 *   使用列搜索策略，逐步改进解的质量
 *   适合需要快速获得可行解的场景
 * 
 * 程序流程
 *   加载问题实例（任务数据、节拍时间、机器人数量）
 *   配置优化策略（容量割平面、不可行子集缓存等）
 *   定义ACS模型（问题、下界、支配关系、列宽度）
 *   运行ACS算法
 *   打印解的详细信息和统计数据
 */
public class NestedSALBPAcsMain {
    
    /**
     * 主程序入口
     * 
     * @param args 命令行参数
     *             args[0]: 数据文件路径（可选，默认使用20任务数据集）
     *             args[1]: 节拍时间（可选，默认1000）
     *             args[2]: 机器人总数（可选，默认4）
     * @throws IOException 文件读取异常
     */
    public static void main(String[] args) throws IOException {
        // ==================== 优化开关配置 ====================
        final boolean USE_INFEASIBILITY_CACHE = true;   // 不可行子集缓存
        final boolean USE_CAPACITY_CUT = true;          // 容量割平面
        final boolean USE_BOUND_PROPAGATION = true;     // 上界传播
        final boolean USE_SYMMETRY_BREAKING = true;     // 对称性破坏

        // ==================== 数据文件路径配置 ====================
        final String instance = args.length == 0
                ? Path.of("data", "generated_SALBP1", "small data set_n=20", "20_246.csv").toString()
                : args[0];

//        final String instance = args.length == 0
//                ? Path.of("data", "generated_SALBP1", "medium data set_n=50", "50_465.csv").toString()
//                : args[0];

//        final String instance = args.length == 0
//                ? Path.of("data", "generated_SALBP1", "large data set_n=100", "100_444.csv").toString()
//                : args[0];

        // 节拍时间
        final int cycleTime = args.length >= 2 ?
                Integer.parseInt(args[1]) : 1000;

        // 可用机器人总数
        final int totalRobots = args.length >= 3 ?
                Integer.parseInt(args[2]) : 3;

        System.out.println("=".repeat(80));
        System.out.println("嵌套装配线平衡问题 - ACS求解器");
        System.out.println("=".repeat(80));
        System.out.println("Instance: " + instance);
        System.out.println("Cycle Time: " + cycleTime);
        System.out.println("Total Robots: " + totalRobots);
        System.out.println("Solver: Anytime Column Search (ACS)");
        System.out.println("=".repeat(80));
        System.out.println();

        // 创建问题实例
        final NestedSALBPProblem problem = new NestedSALBPProblem(
                instance, cycleTime, totalRobots,
                USE_INFEASIBILITY_CACHE, USE_CAPACITY_CUT,
                USE_BOUND_PROPAGATION, USE_SYMMETRY_BREAKING);

        // 定义ACS模型
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
                // ACS的列宽度：控制每次迭代的搜索宽度
                return 10;
            }
        };

        // 运行ACS求解器
        Solution solution = Solvers.minimizeAcs(model, (sol, searchStats) -> {
            // 每次找到更好的解时都会调用这个callback
            System.out.println("\n" + "=".repeat(80));
            System.out.println("发现新的最优解");
            System.out.println("=".repeat(80));

            // 更新上界传播的最优解
            if (sol != null && sol.length > 0) {
                try {
                    int solutionValue = (int) problem.evaluate(sol);
                    problem.updateBestSolution(solutionValue);
                } catch (Exception e) {
                    // 忽略评估错误
                }
            }

            // 打印搜索统计信息
            System.out.println(searchStats);

            // 打印解的详细信息
            if (sol != null && sol.length > 0) {
                System.out.println("\n解向量: " + java.util.Arrays.toString(sol));
                System.out.println();
                printNestedSolution(problem, sol);
            }
        });

        // ==================== 打印最终结果 ====================
        System.out.println("\n" + "=".repeat(80));
        System.out.println("最终搜索统计");
        System.out.println("=".repeat(80));
        System.out.println(solution.statistics());

        // 打印缓存统计信息
        problem.printCacheStatistics();

        // 打印优化统计信息
        problem.printOptimizationStatistics();
    }

    /**
     * 打印嵌套解决方案的详细信息
     * 
     * @param problem 问题实例
     * @param solution 解向量
     */
    private static void printNestedSolution(NestedSALBPProblem problem, int[] solution) {
        System.out.println("=".repeat(80));
        System.out.println("解的详细信息");
        System.out.println("=".repeat(80));

        // 将任务索引转换为真实序号
        int[] taskNumbers = new int[solution.length];
        for (int i = 0; i < solution.length; i++) {
            int task = solution[i] / 2;
            taskNumbers[i] = task + 1;
        }
        System.out.println("任务分配序列: " + java.util.Arrays.toString(taskNumbers));

        // 重建状态以获取每个工位的详细信息
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

            // 状态转移
            if (!willOpenNewStation) {
                java.util.Set<Integer> newStationTasks = new java.util.LinkedHashSet<>(state.currentStationTasks());
                newStationTasks.add(task);
                state = new NestedSALBPState(
                        state.completedTasks(),
                        newStationTasks,
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
                        assignRobot,
                        newUsedRobots);
            }
        }

        // 打印最后一个工位
        if (!state.currentStationTasks().isEmpty()) {
            printStationDetails(problem, state, stationNum);
        }

        System.out.println("\n总工位数: " + stationNum);
        System.out.println("=".repeat(80));
    }

    /**
     * 打印单个工位的详细信息
     * 
     * @param problem 问题实例
     * @param state 当前状态
     * @param stationNum 工位编号
     */
    private static void printStationDetails(NestedSALBPProblem problem, NestedSALBPState state, int stationNum) {
        System.out.println("\n工位 " + stationNum + ":");

        NestedSALBPProblem.InnerSolution innerSolution = problem.solveInnerProblemWithModes(
                state.currentStationTasks(),
                state.currentStationHasRobot()
        );

        java.util.List<Integer> stationTaskNumbers = new java.util.ArrayList<>();
        java.util.List<String> taskModeDetails = new java.util.ArrayList<>();
        String[] modeNames = {"人工", "机器人", "协同"};

        if (innerSolution != null && innerSolution.tasks.length > 0) {
            for (int i = 0; i < innerSolution.tasks.length; i++) {
                int taskNum = innerSolution.tasks[i] + 1;
                int mode = innerSolution.modes[i];
                stationTaskNumbers.add(taskNum);
                taskModeDetails.add("任务" + taskNum + ": " + modeNames[mode]);
            }
        } else {
            for (int t : state.currentStationTasks()) {
                stationTaskNumbers.add(t + 1);
            }
        }

        System.out.println("  任务: " + stationTaskNumbers);
        System.out.println("  机器人: " + (state.currentStationHasRobot() ? "有" : "无"));
        if (!taskModeDetails.isEmpty()) {
            System.out.println("  任务模式: " + String.join(", ", taskModeDetails));
        }
        System.out.println("  完成时间: " + problem.computeStationMakespan(state.currentStationTasks(), state.currentStationHasRobot()));
    }
}

