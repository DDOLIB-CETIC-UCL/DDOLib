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

        // 问题可行性检查：检查是否存在任务即使用最快的加工方式也超过节拍时间
        checkProblemFeasibility();

        // 打印需要机器人的任务信息
        System.out.println("\n=== 任务分析 ===");
        int robotRequiredCount = 0;
        for (int task = 0; task < nbTasks; task++) {
            if (taskRequiresRobot(task)) {
                robotRequiredCount++;
                System.out.printf("任务 %d 必须使用机器人: humanDur=%d > cycleTime=%d, minDur=%d%n",
                        task + 1, innerProblem.humanDurations[task], cycleTime,
                        Math.min(innerProblem.humanDurations[task],
                                Math.min(innerProblem.robotDurations[task],
                                        innerProblem.collaborationDurations[task])));
            }
        }
        System.out.printf("共有 %d 个任务必须使用机器人，可用机器人数: %d%n", robotRequiredCount, totalRobots);
        System.out.println();
    }

    /**
     * 检查问题是否可行：是否存在任务的最小加工时间超过节拍时间
     * 如果存在，则问题不可行，直接抛出异常
     */
    private void checkProblemFeasibility() {
        for (int task = 0; task < nbTasks; task++) {
            int minDuration = getMinDuration(task);
            if (minDuration > cycleTime) {
                throw new IllegalArgumentException(
                        String.format("问题不可行：任务 %d 的最小加工时间 %d 超过节拍时间 %d。" +
                                        "(Human=%d, Robot=%d, Collab=%d)",
                                task + 1, minDuration, cycleTime,
                                innerProblem.humanDurations[task],
                                innerProblem.robotDurations[task],
                                innerProblem.collaborationDurations[task]));
            }
        }
    }

    /**
     * 获取任务的最小加工时间（三种模式中最小的）
     */
    private int getMinDuration(int task) {
        return Math.min(innerProblem.humanDurations[task],
                Math.min(innerProblem.robotDurations[task],
                        innerProblem.collaborationDurations[task]));
    }

    /**
     * 检查任务是否必须使用机器人（人工时间超过节拍时间）
     * 如果人工时间 > cycleTime，则该任务单独在无机器人工位上无法完成
     */
    public boolean taskRequiresRobot(int task) {
        return innerProblem.humanDurations[task] > cycleTime;
    }

    @Override
    public NestedSALBPState initialState() {
        // 初始状态：没有已完成工位，也没有当前工位（第一个任务会触发新开工位1）
        // currentStationHasRobot设为false只是占位，因为currentStationTasks为空
        return new NestedSALBPState(
                Collections.emptySet(),         // 已完成任务为空
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

        // 首次调用时打印详细信息
        if (domainCallCount == 1) {
            System.out.printf("[DEBUG-INIT] 首次domain调用: var=%d, state=%s%n", var, state);
            System.out.printf("[DEBUG-INIT] remaining=%d, currentStation=%s, hasRobot=%s, remainingRobots=%d%n",
                    state.getRemainingTasks(nbTasks).size(),
                    state.currentStationTasks(),
                    state.currentStationHasRobot(),
                    state.remainingRobots(totalRobots));
        }

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

        // 【关键】计算剩余任务中有多少必须使用机器人
        // 用于决定是否需要预留机器人
        int tasksRequiringRobot = 0;
        for (int t : remaining) {
            if (taskRequiresRobot(t)) {
                tasksRequiringRobot++;
            }
        }
        // 注意：不需要减去currentStationTasks中的任务，因为它们已经不在remaining中了
        // 如果当前工位有机器人且包含需要机器人的任务，这些任务已经"消耗"了一个机器人
        // 而remainingRobots已经正确地减去了当前工位的机器人（如果有的话）

        // 机器人是否紧缺：
        // - 严格紧缺：remainingRobots < tasksRequiringRobot（机器人真的不够）
        // - 刚好够用：remainingRobots == tasksRequiringRobot（需要预留，但可以探索）
        boolean robotsCriticallyShort = (remainingRobots < tasksRequiringRobot);
        boolean robotsAreScarce = (remainingRobots <= tasksRequiringRobot);

        // 【关键优化】检查是否有 eligible 且需要机器人的任务
        List<Integer> eligibleRobotTasks = new ArrayList<>();
        List<Integer> eligibleNormalTasks = new ArrayList<>();
        for (int task : remaining) {
            if (isTaskEligible(task, remaining, currentStationTasks)) {
                if (taskRequiresRobot(task)) {
                    eligibleRobotTasks.add(task);
                } else {
                    eligibleNormalTasks.add(task);
                }
            }
        }

        // 决定要处理哪些任务
        // 只有当机器人**真正不够**时才强制只处理需要机器人的任务
        // 当机器人刚好够时，处理所有任务（但用robotsAreScarce限制机器人分配）
        List<Integer> tasksToProcess;
        if (!eligibleRobotTasks.isEmpty() && robotsCriticallyShort) {
            // 机器人真的不够了，只处理需要机器人的任务
            tasksToProcess = eligibleRobotTasks;
        } else {
            // 处理所有 eligible 任务
            tasksToProcess = new ArrayList<>();
            tasksToProcess.addAll(eligibleRobotTasks);
            tasksToProcess.addAll(eligibleNormalTasks);
        }

        // 枚举要处理的任务（已经过滤过eligible）
        for (int task : tasksToProcess) {
            // 检查该任务是否必须使用机器人（人工时间超过节拍时间）
            boolean requiresRobot = taskRequiresRobot(task);

            if (currentStationTasks.isEmpty()) {
                // ===== 新开工位：机器人决策分叉点 =====
                if (requiresRobot) {
                    // 任务必须使用机器人，只有当有剩余机器人时才生成分支
                    if (remainingRobots > 0) {
                        decisions.add(task * 2 + 1);  // 新工位，必须分配机器人
                    }
                    // 不生成无机器人分支，因为该任务无法在无机器人工位完成
                } else {
                    // 任务不强制需要机器人
                    // 【关键修改】如果机器人紧缺，不要把机器人分配给不需要它的任务
                    if (remainingRobots > 0 && !robotsAreScarce) {
                        decisions.add(task * 2 + 1);  // 新工位，分配机器人
                    }
                    decisions.add(task * 2 + 0);  // 新工位，不分配机器人
                }
            } else {
                // ===== 当前工位已打开：判断能否继续放入 =====

                // 如果任务必须使用机器人，但当前工位没有机器人，则必须新开工位
                if (requiresRobot && !state.currentStationHasRobot()) {
                    // 该任务无法加入当前无机器人工位，必须新开有机器人工位
                    if (remainingRobots > 0) {
                        decisions.add(task * 2 + 1);  // 新工位，必须分配机器人
                    }
                    // 如果没有剩余机器人，则该分支不可行，不生成决策
                    continue;
                }

                Set<Integer> testTasks = new LinkedHashSet<>(currentStationTasks);
                testTasks.add(task);

                // 使用乐观估计（减少内层 DDO 调用）
                // domain() 用乐观估计，transition() 会精确判断
                if (isStationFeasibleOptimistic(testTasks, state.currentStationHasRobot())) {
                    // 乐观认为可以放入当前工位
                    decisions.add(task * 2 + 0);  // 加入当前工位
                } else {
                    // 乐观估计认为不可行，需要新开工位
                    if (requiresRobot) {
                        // 任务必须使用机器人
                        if (remainingRobots > 0) {
                            decisions.add(task * 2 + 1);  // 新工位，必须分配机器人
                        }
                        // 【关键修复】如果没有剩余机器人，但当前工位有机器人，
                        // 仍然尝试加入当前工位（让transition()做精确判断）
                        // 这允许多个需要机器人的任务共享同一个有机器人的工位
                        if (state.currentStationHasRobot()) {
                            decisions.add(task * 2 + 0);  // 尝试加入当前有机器人工位
                        }
                    } else {
                        // 任务不强制需要机器人
                        // 【关键修改】如果机器人紧缺，不要把机器人分配给不需要它的任务
                        if (remainingRobots > 0 && !robotsAreScarce) {
                            decisions.add(task * 2 + 1);  // 新工位，分配机器人
                        }
                        decisions.add(task * 2 + 0);  // 尝试加入当前工位，或开新无机器人工位
                    }
                }
            }
        }

        // 首次调用时打印生成的决策
        if (domainCallCount == 1) {
            System.out.printf("[DEBUG-INIT] 机器人预留: tasksRequiringRobot=%d, remainingRobots=%d, robotsAreScarce=%s%n",
                    tasksRequiringRobot, remainingRobots, robotsAreScarce);
            System.out.printf("[DEBUG-INIT] 生成的决策数: %d, decisions=%s%n", decisions.size(), decisions);
        }

        // 调试：检查是否返回空决策列表
        if (decisions.isEmpty() && !state.isComplete(nbTasks)) {
            System.out.printf("[WARNING] domain() 返回空决策！var=%d, remaining=%d, currentStation=%s, hasRobot=%s, remainingRobots=%d%n",
                    var, remaining.size(), currentStationTasks, state.currentStationHasRobot(), remainingRobots);
            // 打印所有remaining任务的信息
            for (int task : remaining) {
                boolean eligible = isTaskEligible(task, remaining, currentStationTasks);
                boolean needsRobot = taskRequiresRobot(task);
                System.out.printf("  Task %d: eligible=%s, requiresRobot=%s, humanDur=%d%n",
                        task + 1, eligible, needsRobot, innerProblem.humanDurations[task]);
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
     * 计算 minDur 之和（更乐观的下界）
     * minDur[i] = min(humanDur[i], robotDur[i], collabDur[i])
     */
    private int sumMinDurations(Set<Integer> tasks) {
        int sum = 0;
        for (int task : tasks) {
            int tH = innerProblem.humanDurations[task];
            int tR = innerProblem.robotDurations[task];
            int tC = innerProblem.collaborationDurations[task];
            sum += Math.min(tH, Math.min(tR, tC));
        }
        return sum;
    }

    /**
     * 乐观版本：用于 domain() 生成决策
     * 使用 sumMinDur 进行更乐观的估计，减少内层 DDO 调用
     * 可能过于乐观，但不影响正确性（transition 会精确判断）
     */
    private boolean isStationFeasibleOptimistic(Set<Integer> tasks, boolean hasRobot) {
        // 首先检查：如果工位没有机器人，但存在任务必须使用机器人，则不可行
        if (!hasRobot) {
            for (int task : tasks) {
                if (taskRequiresRobot(task)) {
                    return false;  // 该任务人工时间超过节拍时间，必须有机器人
                }
            }
            // 无机器人：只能用 human 模式，用 sumHuman 判断
            return sumHumanDurations(tasks) <= cycleTime;
        }

        // 有机器人：用 sumMinDur 乐观估计
        // 如果 sumMinDur <= cycleTime，乐观认为可行（让 transition 精确判断）
        int sumMinDur = sumMinDurations(tasks);
        return sumMinDur <= cycleTime;
    }

    /**
     * 判断任务集合在给定机器人配置下是否 feasible（makespan ≤ cycleTime）
     *
     * 优化策略（lazy evaluation）：
     * - 首先检查是否存在任务必须使用机器人但工位没有机器人
     * - 先计算 sum(humanDurations)
     * - 如果 sum ≤ cycleTime → 无论有没有机器人都 feasible，不需要调用内层 DDO
     * - 如果 sum > cycleTime：
     *   - 无机器人：肯定 infeasible
     *   - 有机器人：需要调用内层 DDO 精确计算
     */
    private boolean isStationFeasible(Set<Integer> tasks, boolean hasRobot) {
        // 首先检查：如果工位没有机器人，但存在任务必须使用机器人，则不可行
        if (!hasRobot) {
            for (int task : tasks) {
                if (taskRequiresRobot(task)) {
                    return false;  // 该任务人工时间超过节拍时间，必须有机器人
                }
            }
        }

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
                    state.completedTasks(),
                    newStationTasks,
                    state.currentStationHasRobot(),  // 机器人状态不变
                    state.usedRobots());
        } else {
            // 新开工位
            Set<Integer> newCompletedTasks = new LinkedHashSet<>(state.completedTasks());
            int newUsedRobots = state.usedRobots();

            // 只有当前工位不为空时，才将其任务加入已完成集合
            if (!state.currentStationTasks().isEmpty()) {
                newCompletedTasks.addAll(state.currentStationTasks());
                if (state.currentStationHasRobot()) {
                    newUsedRobots++;
                }
            }

            Set<Integer> freshStationTasks = Set.of(task);

            // 【关键修复】如果任务需要机器人，新工位必须有机器人
            // 即使决策中robotFlag=0，也要强制分配机器人给新工位
            boolean newStationHasRobot = assignRobot;
            if (taskRequiresRobot(task)) {
                // 检查是否有剩余机器人可用
                int availableRobots = totalRobots - newUsedRobots;
                if (availableRobots > 0) {
                    newStationHasRobot = true;  // 强制分配机器人
                }
                // 如果没有剩余机器人，保持assignRobot的值
                // （这种情况理论上不应该发生，因为domain()应该已经过滤掉了）
            }

            return new NestedSALBPState(
                    newCompletedTasks,
                    freshStationTasks,
                    newStationHasRobot,  // 新工位的机器人决策
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
        int stationCount = 0;

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
                        state.completedTasks(),
                        newStationTasks,
                        state.currentStationHasRobot(),
                        state.usedRobots());
            } else {
                // 新开工位
                Set<Integer> newCompletedTasks = new LinkedHashSet<>(state.completedTasks());
                int newUsedRobots = state.usedRobots();

                // 只有当前工位不为空时，才将其任务加入已完成集合
                if (!state.currentStationTasks().isEmpty()) {
                    newCompletedTasks.addAll(state.currentStationTasks());
                    stationCount++;  // 完成一个工位
                    if (state.currentStationHasRobot()) {
                        newUsedRobots++;
                    }
                }

                Set<Integer> freshStationTasks = Set.of(task);
                state = new NestedSALBPState(
                        newCompletedTasks,
                        freshStationTasks,
                        assignRobot,
                        newUsedRobots);
            }
        }

        // 总工位数 = 已完成工位 + (当前工位非空则+1)
        if (!state.currentStationTasks().isEmpty()) {
            stationCount++;
        }
        return stationCount;
    }

}
