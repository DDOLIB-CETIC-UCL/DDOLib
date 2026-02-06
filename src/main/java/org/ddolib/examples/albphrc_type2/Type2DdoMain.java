package org.ddolib.examples.albphrc_type2;

import org.ddolib.modeling.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.common.solver.Solution;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.Decision;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 二型 ALBP-HRC 的主程序
 * 
 * 目标：给定工位数 m 和机器人数 Q，最小化节拍时间 C
 * 
 * 使用方法：
 * java Type2DdoMain <dataFile> <maxStations> <totalRobots>
 */
public class Type2DdoMain {
    
    public static void main(String[] args) throws IOException {
        // 数据文件 - 参考一型问题的数据文件路径
        final String instance = args.length >= 1 ?
                args[0] : Path.of("data", "SALBP1", "small data set_n=20", "instance_n=20_370.alb").toString();
        
        // 最大工位数 m
        final int maxStations = args.length >= 2 ?
                Integer.parseInt(args[1]) : 5;
        
        // 可用机器人数 Q
        final int totalRobots = args.length >= 3 ?
                Integer.parseInt(args[2]) : 2;
        
        System.out.println("=".repeat(80));
        System.out.println("二型 ALBP-HRC 求解器");
        System.out.println("=".repeat(80));
        System.out.printf("数据文件: %s%n", instance);
        System.out.printf("最大工位数: %d%n", maxStations);
        System.out.printf("机器人数: %d%n", totalRobots);
        System.out.println("=".repeat(80));
        
        // 创建问题实例
        Type2Problem problem = new Type2Problem(instance, maxStations, totalRobots);
        
        // 配置 DDO 模型
        DdoModel<Type2State> model = new DdoModel<>() {
            @Override
            public Problem<Type2State> problem() {
                return problem;
            }
            
            @Override
            public Relaxation<Type2State> relaxation() {
                return new Type2Relaxation();
            }
            
            @Override
            public StateRanking<Type2State> ranking() {
                return new Type2Ranking();
            }
            
            @Override
            public FastLowerBound<Type2State> lowerBound() {
                return new Type2FastLowerBound(problem);
            }
            
            @Override
            public DominanceChecker<Type2State> dominance() {
                return new SimpleDominanceChecker<>(new Type2Dominance(), problem.nbTasks);
            }
            
            @Override
            public WidthHeuristic<Type2State> widthHeuristic() {
                return new FixedWidth<>(1000);  // 增加 Beam width 到 1000
            }
            
            @Override
            public Frontier<Type2State> frontier() {
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
        
        // 求解
        System.out.println("\n开始求解...\n");
        long startTime = System.currentTimeMillis();
        
        final double[] bestCycleTime = {Double.POSITIVE_INFINITY};
        final int[][] bestSolution = {null};
        
        Solution solution = Solvers.minimizeDdo(model, (sol, stats) -> {
            if (sol != null && sol.length > 0) {
                try {
                    double cycleTime = problem.evaluate(sol);
                    
                    if (cycleTime < bestCycleTime[0]) {
                        bestCycleTime[0] = cycleTime;
                        bestSolution[0] = sol;
                        
                        System.out.println("\n" + "=".repeat(80));
                        System.out.printf("找到更好的解！节拍时间: %.0f%n", cycleTime);
                        System.out.println("=".repeat(80));
                        
                        // 打印解的详细信息
                        printSolution(problem, sol, cycleTime);
                        
                        // 打印搜索统计
                        System.out.println("\n搜索统计:");
                        System.out.println(stats);
                    }
                } catch (Exception e) {
                    System.err.println("评估解时出错: " + e.getMessage());
                }
            }
        });
        
        long endTime = System.currentTimeMillis();
        double elapsedSeconds = (endTime - startTime) / 1000.0;
        
        // 打印最终结果
        System.out.println("\n" + "=".repeat(80));
        System.out.println("求解完成");
        System.out.println("=".repeat(80));
        System.out.printf("最优节拍时间: %.0f%n", bestCycleTime[0]);
        System.out.printf("求解时间: %.2f 秒%n", elapsedSeconds);
        System.out.println("=".repeat(80));
    }
    
    /**
     * 打印解的详细信息
     */
    private static void printSolution(Type2Problem problem, int[] solution, double cycleTime) {
        System.out.println("\n工位分配:");
        
        Type2State state = problem.initialState();
        int currentStation = 0;
        int currentLoad = 0;
        StringBuilder stationTasks = new StringBuilder();
        
        for (int i = 0; i < solution.length; i++) {
            int decisionVal = solution[i];
            int task = decisionVal / 6;
            int action = decisionVal % 6;
            
            // 解码模式
            int mode;
            if (action == 0 || action == 3) mode = 0;  // Human
            else if (action == 1 || action == 4) mode = 1;  // Robot
            else mode = 2;  // Collaboration
            
            // 执行转移
            Type2State nextState = problem.transition(state, new Decision(i, decisionVal));
            
            // 检查是否开新工位
            if (nextState.stationIndex() != state.stationIndex()) {
                // 打印上一个工位
                if (currentStation > 0) {
                    System.out.printf("  工位 %d (机器人: %s, 负载: %d): %s%n",
                                     currentStation,
                                     state.currentStationHasRobot() ? "是" : "否",
                                     currentLoad,
                                     stationTasks.toString());
                }
                
                // 开始新工位
                currentStation = nextState.stationIndex();
                currentLoad = getDuration(problem, task, mode);
                stationTasks = new StringBuilder();
                stationTasks.append(String.format("任务%d(%s)", task + 1, getModeString(mode)));
            } else {
                // 加入当前工位
                if (stationTasks.length() > 0) {
                    stationTasks.append(", ");
                }
                stationTasks.append(String.format("任务%d(%s)", task + 1, getModeString(mode)));
                currentLoad = nextState.currentStationLoad();
            }
            
            state = nextState;
        }
        
        // 打印最后一个工位
        if (currentStation > 0) {
            System.out.printf("  工位 %d (机器人: %s, 负载: %d): %s%n",
                             currentStation,
                             state.currentStationHasRobot() ? "是" : "否",
                             currentLoad,
                             stationTasks.toString());
        }
        
        System.out.printf("\n总工位数: %d%n", state.stationIndex());
        System.out.printf("使用机器人数: %d%n", state.usedRobots() + (state.currentStationHasRobot() ? 1 : 0));
        System.out.printf("最大负载: %d%n", state.maxLoadSoFar());
    }
    
    private static int getDuration(Type2Problem problem, int task, int mode) {
        switch (mode) {
            case Type2Problem.MODE_HUMAN: return problem.humanDurations[task];
            case Type2Problem.MODE_ROBOT: return problem.robotDurations[task];
            case Type2Problem.MODE_COLLABORATION: return problem.collaborationDurations[task];
            default: return 0;
        }
    }
    
    private static String getModeString(int mode) {
        switch (mode) {
            case Type2Problem.MODE_HUMAN: return "H";
            case Type2Problem.MODE_ROBOT: return "R";
            case Type2Problem.MODE_COLLABORATION: return "HR";
            default: return "?";
        }
    }
}
