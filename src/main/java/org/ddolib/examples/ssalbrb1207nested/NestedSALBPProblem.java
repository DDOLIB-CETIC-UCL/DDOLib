package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;
import org.ddolib.examples.ssalbrb.*;
import org.ddolib.modeling.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.Solution;

import java.io.IOException;
import java.util.*;

/**
 * 嵌套动态规划问题：一型装配线平衡 + 人机协同调度
 *
 * 外层：决定任务到工位的分配
 * 内层：调用单工位调度问题（SSALBRBProblem）
 */
public class NestedSALBPProblem implements Problem<NestedSALBPState> {


    public final int nbTasks;
    public final int cycleTime;
    public final int totalRobots;
    private final Map<Integer, List<Integer>> predecessors;
    private final Map<Integer, List<Integer>> successors;

    // 内层问题：单工位调度
    public final SSALBRBProblem innerProblem;

    // 缓存：任务子集 -> (是否有机器人 -> makespan)
    private final Map<Set<Integer>, Map<Boolean, Integer>> makespanCache;

    public NestedSALBPProblem(String dataFile, int cycleTime, int totalRobots) throws IOException {
        this.innerProblem = new SSALBRBProblem(dataFile);
        this.nbTasks = innerProblem.nbTasks;
        this.cycleTime = cycleTime;
        this.totalRobots = totalRobots;
        this.predecessors = innerProblem.predecessors;
        this.successors = innerProblem.successors;
        this.makespanCache = new HashMap<>();
    }

    @Override
    public NestedSALBPState initialState() {
        // 初始状态：没有已完成工位，也没有当前工位（第一个任务会触发新开工位1）
        // currentStationHasRobot设为false只是占位，因为currentStationTasks为空
        return new NestedSALBPState(
                Collections.emptyList(),        // 已完成工位列表为空
                Collections.emptySet(),         // 当前工位任务为空（还未开始第一个工位）
                false,                          // 当前工位机器人状态（占位，因为工位还未开始）
                0);                             // 已使用机器人数为0
    }

    private static long domainCallCount = 0;
    private static long lastLogTime = System.currentTimeMillis();

    private static long innerDdoCallCount = 0;
    private static long cacheHitCount = 0;
    private static long cacheMissCount = 0;

    @Override
    public Iterator<Integer> domain(NestedSALBPState state, int var) {
        domainCallCount++;
        long now = System.currentTimeMillis();
        if (now - lastLogTime > 5000) {  // 每5秒打印一次
            System.out.printf("[DEBUG] domain=%d, innerDDO=%d (hit=%d, miss=%d), var=%d, remaining=%d, usedRobots=%d, csHasRobot=%s%n",
                    domainCallCount, innerDdoCallCount, cacheHitCount, cacheMissCount, var,
                    state.getRemainingTasks(nbTasks).size(),
                    state.usedRobots(), state.currentStationHasRobot());
            lastLogTime = now;
        }

        if (state.isComplete(nbTasks)) {
            return Collections.emptyIterator();
        }

        List<Integer> decisions = new ArrayList<>();
        Set<Integer> remaining = state.getRemainingTasks(nbTasks);
        Set<Integer> currentStationTasks = state.currentStationTasks();
        int remainingRobots = state.remainingRobots(totalRobots);

        // 枚举每个 eligible 任务
        for (int task : remaining) {
            // 检查前继约束
            if (!isTaskEligible(task, remaining, currentStationTasks)) {
                continue;
            }

            if (currentStationTasks.isEmpty()) {
                // ===== 新开工位：机器人决策分叉点 =====
                // 优先生成有机器人的分支（鼓励使用机器人提高工位容量）
                if (remainingRobots > 0) {
                    decisions.add(task * 2 + 1);  // 新工位，分配机器人（优先）
                }
                decisions.add(task * 2 + 0);  // 新工位，不分配机器人
            } else {
                // ===== 当前工位已打开：判断能否继续放入 =====
                Set<Integer> testTasks = new LinkedHashSet<>(currentStationTasks);
                testTasks.add(task);

                // 使用乐观估计（工人时间之和）快速判断
                int sumHuman = sumHumanDurations(testTasks);

                if (sumHuman <= cycleTime) {
                    // 乐观情况：肯定能放入当前工位
                    decisions.add(task * 2 + 0);  // 加入当前工位
                } else {
                    // sumHuman > cycleTime：需要进一步判断
                    if (state.currentStationHasRobot()) {
                        // 当前工位有机器人：调用内层 DDO 精确判断
                        if (isStationFeasible(testTasks, true)) {
                            decisions.add(task * 2 + 0);  // 还能放入
                        } else {
                            // 当前工位已满，需要新开工位
                            if (remainingRobots > 0) {
                                decisions.add(task * 2 + 1);  // 新工位，分配机器人（优先）
                            }
                            decisions.add(task * 2 + 0);  // 新工位，不分配机器人
                        }
                    } else {
                        // 当前工位无机器人：sumHuman > cycleTime → infeasible
                        // 需要新开工位
                        if (remainingRobots > 0) {
                            decisions.add(task * 2 + 1);  // 新工位，分配机器人（优先）
                        }
                        decisions.add(task * 2 + 0);  // 新工位，不分配机器人
                    }
                }
            }
        }

        return decisions.iterator();
    }

    /**
     * 检查任务是否eligible（所有前继要么已完成，要么在当前工位中）
     */
    private boolean isTaskEligible(int task, Set<Integer> remaining, Set<Integer> currentStationTasks) {
        for (int pred : predecessors.get(task)) {
            // 前继还在remaining中，且不在当前工位中 -> 不可行
            if (remaining.contains(pred) && !currentStationTasks.contains(pred)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算工人时间之和（乐观下界）
     * 用于快速判断是否需要调用内层 DDO
     */
    private int sumHumanDurations(Set<Integer> tasks) {
        int sum = 0;
        for (int task : tasks) {
            sum += innerProblem.humanDurations[task];
        }
        return sum;
    }

    /**
     * 判断任务集合在给定机器人配置下是否 feasible（makespan ≤ cycleTime）
     *
     * 优化策略（lazy evaluation）：
     * - 先计算 sum(humanDurations)
     * - 如果 sum ≤ cycleTime → 无论有没有机器人都 feasible，不需要调用内层 DDO
     * - 如果 sum > cycleTime：
     *   - 无机器人：肯定 infeasible
     *   - 有机器人：需要调用内层 DDO 精确计算
     */
    private boolean isStationFeasible(Set<Integer> tasks, boolean hasRobot) {
        int sumHuman = sumHumanDurations(tasks);

        if (sumHuman <= cycleTime) {
            // 乐观情况：工人时间之和都不超，肯定 feasible
            return true;
        }

        // sum > cycleTime
        if (!hasRobot) {
            // 无机器人：makespan = sumHuman > cycleTime → infeasible
            return false;
        }

        // 有机器人：需要精确计算
        int makespan = computeStationMakespan(tasks, true);
        return makespan <= cycleTime;
    }

    /**
     * 调用内层模型计算单工位makespan
     */
    public int computeStationMakespan(Set<Integer> tasks, boolean hasRobot) {
        // 查缓存
        if (makespanCache.containsKey(tasks) && makespanCache.get(tasks).containsKey(hasRobot)) {
            cacheHitCount++;
            return makespanCache.get(tasks).get(hasRobot);
        }
        cacheMissCount++;

        int makespan;

        if (!hasRobot) {
            // 优化：没有机器人时，所有任务只能用人工模式
            // makespan = 所有任务人工时间的简单加和（顺序执行）
            makespan = 0;
            for (int task : tasks) {
                makespan += innerProblem.humanDurations[task];
            }
        } else {
            // 有机器人时，需要调用内层DDO求解器来找最优调度
            innerDdoCallCount++;
            SSALBRBProblem subProblem = createSubProblem(tasks, true);
            makespan = solveInnerProblem(subProblem);
        }

        // 缓存结果
        makespanCache.computeIfAbsent(tasks, k -> new HashMap<>()).put(hasRobot, makespan);

        return makespan;
    }

    /**
     * 创建子问题：只包含指定任务
     * 如果没有机器人，禁用robot和collaboration模式（设置为超大值会导致溢出）
     */
    private SSALBRBProblem createSubProblem(Set<Integer> tasks, boolean hasRobot) {
        // 创建任务映射（从子集索引到原始索引）
        List<Integer> taskList = new ArrayList<>(tasks);
        int subNbTasks = taskList.size();

        int[] subHDurations = new int[subNbTasks];
        int[] subRDurations = new int[subNbTasks];
        int[] subCDurations = new int[subNbTasks];

        for (int i = 0; i < subNbTasks; i++) {
            int originalTask = taskList.get(i);
            subHDurations[i] = innerProblem.humanDurations[originalTask];

            if (hasRobot) {
                // 有机器人：使用正常的时间
                subRDurations[i] = innerProblem.robotDurations[originalTask];
                subCDurations[i] = innerProblem.collaborationDurations[originalTask];
            } else {
                // 没有机器人：禁用robot和collaboration模式
                // 使用一个大但不会溢出的值（cycleTime * 10000）
                subRDurations[i] = cycleTime * 10000;
                subCDurations[i] = cycleTime * 10000;
            }
        }

        // 构建子问题的前继关系
        Map<Integer, List<Integer>> subSuccessors = new HashMap<>();
        for (int i = 0; i < subNbTasks; i++) {
            int originalTask = taskList.get(i);
            List<Integer> originalSuccs = successors.get(originalTask);

            List<Integer> subSuccs = new ArrayList<>();
            for (int succ : originalSuccs) {
                int subIndex = taskList.indexOf(succ);
                if (subIndex >= 0) {  // 后继在子集中
                    subSuccs.add(subIndex);
                }
            }
            subSuccessors.put(i, subSuccs);
        }

        return new SSALBRBProblem(subNbTasks, subHDurations, subRDurations, subCDurations, subSuccessors);
    }

    /**
     * 求解内层问题（使用完整的DDO求解器）
     */
    private int solveInnerProblem(SSALBRBProblem subProblem) {
        // 使用完整的DDO求解器，修复了整数溢出问题
        return solveDDO(subProblem);
    }

    /**
     * 内层问题的解（包含任务和操作模式）
     */
    public static class InnerSolution {
        public final int[] tasks;  // 任务序列（原始索引）
        public final int[] modes;  // 对应的操作模式（0=Human, 1=Robot, 2=Collaboration）

        public InnerSolution(int[] tasks, int[] modes) {
            this.tasks = tasks;
            this.modes = modes;
        }
    }

    /**
     * 求解内层问题并返回任务调度顺序和操作模式
     * @param stationTasks 工位上的任务集合（原始索引）
     * @param hasRobot 是否有机器人
     * @return 任务调度顺序和操作模式
     */
    public InnerSolution solveInnerProblemWithModes(Set<Integer> stationTasks, boolean hasRobot) {
        // 创建任务映射
        List<Integer> taskList = new ArrayList<>(stationTasks);

        SSALBRBProblem subProblem = createSubProblem(stationTasks, hasRobot);
        int[] subSolution = solveDDOForSolution(subProblem);

        if (subSolution == null || subSolution.length == 0) {
            return null;
        }

        // 将子问题的决策值解码并映射回原始索引
        // 内层DDO的decision编码: task * 3 + mode
        int[] originalTasks = new int[subSolution.length];
        int[] modes = new int[subSolution.length];

        for (int i = 0; i < subSolution.length; i++) {
            int decision = subSolution[i];
            int subTaskIndex = decision / 3;  // 从decision解码出任务索引
            int mode = decision % 3;          // 从decision解码出操作模式

            originalTasks[i] = taskList.get(subTaskIndex);
            modes[i] = mode;
        }

        return new InnerSolution(originalTasks, modes);
    }

    /**
     * 求解内层问题并返回任务调度顺序
     * @param stationTasks 工位上的任务集合（原始索引）
     * @param hasRobot 是否有机器人
     * @return 任务调度顺序（原始索引）
     */
    public int[] solveInnerProblem(Set<Integer> stationTasks, boolean hasRobot) {
        InnerSolution solution = solveInnerProblemWithModes(stationTasks, hasRobot);
        return solution != null ? solution.tasks : null;
    }

    /**
     * 使用DDO求解器求解单工位调度问题，返回解的顺序
     */
    private int[] solveDDOForSolution(SSALBRBProblem problem) {
        // 配置DDO模型（使用ssalbrb包中的组件）
        DdoModel<SSALBRBState> innerModel = new DdoModel<>() {
            @Override
            public Problem<SSALBRBState> problem() {
                return problem;
            }

            @Override
            public Relaxation<SSALBRBState> relaxation() {
                return new SSALBRBRelax(problem.humanDurations, problem.robotDurations, problem.collaborationDurations);
            }

            @Override
            public StateRanking<SSALBRBState> ranking() {
                return new SSALBRBRanking();
            }

            @Override
            public FastLowerBound<SSALBRBState> lowerBound() {
                return new SSALBRBFastLowerBound(problem);
            }

            @Override
            public org.ddolib.ddo.core.heuristics.width.WidthHeuristic<SSALBRBState> widthHeuristic() {
                return new FixedWidth<>(10);
            }

            @Override
            public boolean exportDot() {
                return false;
            }
        };

        final int[][] resultSolution = {null};

        // 求解内层问题
        Solution solution = Solvers.minimizeDdo(innerModel, (sol, searchStats) -> {
            if (sol != null && sol.length > 0) {
                resultSolution[0] = sol;
            }
        });

        return resultSolution[0];
    }

    /**
     * 使用DDO求解器求解单工位调度问题
     */
    private int solveDDO(SSALBRBProblem problem) {
        // 配置DDO模型（使用ssalbrb包中的组件）
        DdoModel<SSALBRBState> innerModel = new DdoModel<>() {
            @Override
            public Problem<SSALBRBState> problem() {
                return problem;
            }

            @Override
            public Relaxation<SSALBRBState> relaxation() {
                return new SSALBRBRelax(problem.humanDurations, problem.robotDurations, problem.collaborationDurations);
            }

            @Override
            public StateRanking<SSALBRBState> ranking() {
                return new SSALBRBRanking();
            }

            @Override
            public FastLowerBound<SSALBRBState> lowerBound() {
                return new SSALBRBFastLowerBound(problem);
            }

            @Override
            public org.ddolib.ddo.core.heuristics.width.WidthHeuristic<SSALBRBState> widthHeuristic() {
                return new FixedWidth<>(10);
            }

            @Override
            public boolean exportDot() {
                return false;
            }
        };

        final int[] resultMakespan = {Integer.MAX_VALUE};
        final int[][] resultSolution = {null};

        // 求解内层问题
        Solution solution = Solvers.minimizeDdo(innerModel, (sol, searchStats) -> {
            if (sol != null && sol.length > 0) {
                resultSolution[0] = sol;
                try {
                    double makespan = problem.evaluate(sol);
                    resultMakespan[0] = (int) makespan;
                } catch (Exception e) {
                    resultMakespan[0] = Integer.MAX_VALUE;
                }
            }
        });

        return resultMakespan[0];
    }

    /**
     * 贪心调度算法
     */
    private int greedyScheduling(SSALBRBProblem problem) {
        int humanReady = 0;
        int robotReady = 0;
        int[] completionTimes = new int[problem.nbTasks];
        Arrays.fill(completionTimes, -1);
        Set<Integer> remaining = new LinkedHashSet<>();
        for (int i = 0; i < problem.nbTasks; i++) {
            remaining.add(i);
        }

        while (!remaining.isEmpty()) {
            // 选择eligible任务
            int selectedTask = -1;
            int selectedMode = -1;
            int minCompletion = Integer.MAX_VALUE;

            for (int task : remaining) {
                // 检查前继
                boolean eligible = true;
                int latestPred = 0;
                for (int pred : problem.predecessors.get(task)) {
                    if (completionTimes[pred] < 0) {
                        eligible = false;
                        break;
                    }
                    latestPred = Math.max(latestPred, completionTimes[pred]);
                }

                if (!eligible) continue;

                // 尝试三种模式
                int[] modes = {SSALBRBProblem.MODE_HUMAN, SSALBRBProblem.MODE_ROBOT, SSALBRBProblem.MODE_COLLABORATION};
                int[] durations = {
                        problem.humanDurations[task],
                        problem.robotDurations[task],
                        problem.collaborationDurations[task]
                };

                for (int i = 0; i < modes.length; i++) {
                    int mode = modes[i];
                    int duration = durations[i];

                    if (duration == Integer.MAX_VALUE) continue;

                    int start, completion;
                    if (mode == SSALBRBProblem.MODE_HUMAN) {
                        start = Math.max(humanReady, latestPred);
                        completion = start + duration;
                    } else if (mode == SSALBRBProblem.MODE_ROBOT) {
                        start = Math.max(robotReady, latestPred);
                        completion = start + duration;
                    } else {  // COLLABORATION
                        start = Math.max(Math.max(humanReady, robotReady), latestPred);
                        completion = start + duration;
                    }

                    if (completion < minCompletion) {
                        minCompletion = completion;
                        selectedTask = task;
                        selectedMode = mode;
                    }
                }
            }

            if (selectedTask == -1) break;  // 无法继续

            // 执行选中的任务
            int latestPred = 0;
            for (int pred : problem.predecessors.get(selectedTask)) {
                latestPred = Math.max(latestPred, completionTimes[pred]);
            }

            int start, completion;
            int duration;
            if (selectedMode == SSALBRBProblem.MODE_HUMAN) {
                duration = problem.humanDurations[selectedTask];
                start = Math.max(humanReady, latestPred);
                completion = start + duration;
                humanReady = completion;
            } else if (selectedMode == SSALBRBProblem.MODE_ROBOT) {
                duration = problem.robotDurations[selectedTask];
                start = Math.max(robotReady, latestPred);
                completion = start + duration;
                robotReady = completion;
            } else {
                duration = problem.collaborationDurations[selectedTask];
                start = Math.max(Math.max(humanReady, robotReady), latestPred);
                completion = start + duration;
                humanReady = completion;
                robotReady = completion;
            }

            completionTimes[selectedTask] = completion;
            remaining.remove(selectedTask);
        }

        return Math.max(humanReady, robotReady);
    }

    @Override
    public NestedSALBPState transition(NestedSALBPState state, Decision decision) {
        // 解码决策：decision = task * 2 + robotFlag
        int decisionVal = decision.val();
        int task = decisionVal / 2;
        int robotFlag = decisionVal % 2;
        boolean assignRobot = (robotFlag == 1);

        // 判断：是否需要新开工位
        boolean needNewStation = false;

        if (state.currentStationTasks().isEmpty()) {
            // 当前工位为空：开启新工位
            needNewStation = true;
        } else {
            // 使用 isStationFeasible 判断（内部有 lazy evaluation）
            Set<Integer> testTasks = new LinkedHashSet<>(state.currentStationTasks());
            testTasks.add(task);
            needNewStation = !isStationFeasible(testTasks, state.currentStationHasRobot());
        }

        if (!needNewStation) {
            // 加入当前工位（robotFlag 在此情况下无意义，因为机器人决策已在工位打开时确定）
            Set<Integer> newStationTasks = new LinkedHashSet<>(state.currentStationTasks());
            newStationTasks.add(task);

            return new NestedSALBPState(
                    state.completedStations(),
                    newStationTasks,
                    state.currentStationHasRobot(),  // 机器人状态不变
                    state.usedRobots());
        } else {
            // 新开工位
            List<Set<Integer>> newCompletedStations = new ArrayList<>(state.completedStations());
            int newUsedRobots = state.usedRobots();

            // 只有当前工位不为空时，才将其加入已完成列表
            if (!state.currentStationTasks().isEmpty()) {
                newCompletedStations.add(state.currentStationTasks());
                if (state.currentStationHasRobot()) {
                    newUsedRobots++;
                }
            }

            Set<Integer> freshStationTasks = Set.of(task);

            return new NestedSALBPState(
                    newCompletedStations,
                    freshStationTasks,
                    assignRobot,  // 新工位的机器人决策由 robotFlag 决定
                    newUsedRobots);
        }
    }

    @Override
    public double transitionCost(NestedSALBPState state, Decision decision) {
        // 解码决策
        int decisionVal = decision.val();
        int task = decisionVal / 2;

        // 判断：是否新开工位
        if (state.currentStationTasks().isEmpty()) {
            // 当前工位为空：开启第一个工位
            return 1.0;
        }

        Set<Integer> testTasks = new LinkedHashSet<>(state.currentStationTasks());
        testTasks.add(task);

        // 使用 isStationFeasible 判断（内部有 lazy evaluation）
        if (isStationFeasible(testTasks, state.currentStationHasRobot())) {
            return 0.0;  // 加入当前工位
        } else {
            return 1.0;  // 新开工位
        }
    }

    @Override
    public Optional<Double> optimalValue() {
        // 使用数据文件中的 <solution> 值作为最优工位数
        // 注意：测试数据文件中的 solution 应该是最优工位数，而不是 makespan
        return innerProblem.optimalValue();
    }

    @Override
    public int nbVars() {
        return nbTasks;
    }

    @Override
    public double initialValue() {
        // 初始状态还没有任何工位，第一个任务会触发cost=1（新开工位1）
        return 0;
    }

    @Override
    public double evaluate(int[] solution) throws org.ddolib.modeling.InvalidSolutionException {
        // 重建状态并计算工位数
        NestedSALBPState state = initialState();

        for (int decisionVal : solution) {
            // 解码决策
            int task = decisionVal / 2;
            int robotFlag = decisionVal % 2;
            boolean assignRobot = (robotFlag == 1);

            // 判断：是否需要新开工位
            boolean needNewStation = false;

            if (state.currentStationTasks().isEmpty()) {
                // 当前工位为空：开启新工位
                needNewStation = true;
            } else {
                // 使用 isStationFeasible 判断
                Set<Integer> testTasks = new LinkedHashSet<>(state.currentStationTasks());
                testTasks.add(task);
                needNewStation = !isStationFeasible(testTasks, state.currentStationHasRobot());
            }

            if (!needNewStation) {
                // 加入当前工位
                Set<Integer> newStationTasks = new LinkedHashSet<>(state.currentStationTasks());
                newStationTasks.add(task);

                state = new NestedSALBPState(
                        state.completedStations(),
                        newStationTasks,
                        state.currentStationHasRobot(),
                        state.usedRobots());
            } else {
                // 新开工位
                List<Set<Integer>> newCompletedStations = new ArrayList<>(state.completedStations());
                int newUsedRobots = state.usedRobots();

                // 只有当前工位不为空时，才将其加入已完成列表
                if (!state.currentStationTasks().isEmpty()) {
                    newCompletedStations.add(state.currentStationTasks());
                    if (state.currentStationHasRobot()) {
                        newUsedRobots++;
                    }
                }

                Set<Integer> freshStationTasks = Set.of(task);
                state = new NestedSALBPState(
                        newCompletedStations,
                        freshStationTasks,
                        assignRobot,
                        newUsedRobots);
            }
        }

        // 总工位数
        return state.getUsedStations();
    }

}
