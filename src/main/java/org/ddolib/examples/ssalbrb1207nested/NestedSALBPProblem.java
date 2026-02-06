package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;
import org.ddolib.examples.ssalbrb.*;
import org.ddolib.modeling.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.common.solver.Solution;

import java.io.IOException;
import java.util.*;

/**
 * 嵌套动态规划问题：一型装配线平衡 + 人机协同调度
 *
 * ============================================================================
 * 问题描述：
 * ============================================================================
 * 外层DDO：决定任务到工位的分配，目标是最小化工位数
 * 内层DDO：对每个工位进行人机协同调度，目标是最小化makespan（确保不超过节拍时间）
 *
 * ============================================================================
 * 核心设计思想：
 * ============================================================================
 * 1. 【机器人保留策略】Robot Reservation Strategy
 *    - 识别瓶颈任务：人工时间 > 节拍时间的任务必须使用机器人
 *    - 保留机制：当剩余机器人数 <= 需要机器人的任务数时，优先为瓶颈任务保留机器人
 *    - 目的：确保问题可行性，避免瓶颈任务无法完成
 *
 * 2. 【可行性检查】Feasibility Check
 *    - 问题级别：检查是否存在任务即使用最快模式也超过节拍时间
 *    - 工位级别：检查工位内任务集合是否能在节拍时间内完成
 *    - 前继约束：确保任务分配满足优先级关系
 *
 * 3. 【懒惰求值】Lazy Evaluation
 *    - 乐观估计：在domain()中使用sumMinDurations快速判断可行性
 *    - 精确计算：在transition()中调用内层DDO进行精确验证
 *    - 缓存机制：缓存makespan和解序列，避免重复计算
 *
 * 4. 【优化策略】Optimization Strategies（可配置开关）
 *    - 容量割平面 (Capacity Cut)：基于最小时间和剪枝不可行工位
 *    - 不可行子集缓存 (Infeasibility Cache)：记录已知不可行的任务子集
 *    - 上界传播 (Bound Propagation)：使用已知最优解剪枝
 *    - 对称性破坏 (Symmetry Breaking)：强制工位按首任务编号排序
 *
 * ============================================================================
 * 决策编码：
 * ============================================================================
 * decision = task * 2 + robotFlag
 * - task: 要分配的任务索引 (0 到 nbTasks-1)
 * - robotFlag: 0 = 加入当前工位或开新工位不分配机器人
 *              1 = 开新工位并分配机器人
 *
 * ============================================================================
 * 状态表示：
 * ============================================================================
 * NestedSALBPState 包含：
 * - completedTasks: 已完成的任务集合（已关闭工位中的任务）
 * - currentStationTasks: 当前工位中的任务集合
 * - currentStationHasRobot: 当前工位是否有机器人
 * - usedRobots: 已使用的机器人数量（已关闭工位中的机器人）
 */
public class NestedSALBPProblem implements Problem<NestedSALBPState> {

    // ==================== 问题参数 (Problem Parameters) ====================
    public final int nbTasks;
    public final int cycleTime;
    public final int totalRobots;
    private final Map<Integer, List<Integer>> predecessors;
    private final Map<Integer, List<Integer>> successors;

    // ==================== 内层问题 (Inner Problem) ====================
    public final SSALBRBProblem innerProblem;

    // ==================== 缓存系统 (Cache System) ====================
    /**
     * Makespan缓存：任务子集 -> (是否有机器人 -> makespan)
     * 用途：避免重复调用内层DDO求解器计算相同工位的makespan
     */
    private final Map<Set<Integer>, Map<Boolean, Integer>> makespanCache;

    /**
     * 解序列缓存：任务子集 -> (是否有机器人 -> 解序列)
     * 用途：缓存内层DDO的完整解，用于打印详细调度方案
     */
    private final Map<Set<Integer>, Map<Boolean, int[]>> solutionCache;

    // ==================== 优化组件 (Optimization Components) ====================
    /**
     * 不可行子集缓存：记录已知不可行的任务子集
     * 原理：如果任务集合S不可行，则包含S的任何超集也不可行
     * 作用：快速剪枝，避免重复检查已知不可行的子集
     */
    private final InfeasibilityCache infeasibilityCache;

    /**
     * 上界传播：使用已知最优解进行剪枝
     * 原理：如果当前状态的下界 >= 已知上界，则可以剪枝
     * 作用：减少搜索空间，提前终止无希望的分支
     */
    private final BoundPropagation boundPropagation;

    /** 开关：是否启用不可行子集缓存 */
    private final boolean useInfeasibilityCache;

    /** 开关：是否启用容量割平面（基于最小时间和的快速剪枝） */
    private final boolean useCapacityCut;

    /** 开关：是否启用上界传播 */
    private final boolean useBoundPropagation;

    /** 开关：是否启用对称性破坏（强制工位按首任务编号排序） */
    private final boolean useSymmetryBreaking;

    // ==================== 调试统计 (Debug Statistics) ====================

    /** 统计：domain()方法被调用的总次数 */
    private static long domainCallCount = 0;
    /** 统计：内层DDO求解器被调用的总次数 */
    private static long innerDdoCallCount = 0;
    /** 统计：缓存命中次数（成功从缓存获取结果） */
    private static long cacheHitCount = 0;
    /** 统计：缓存未命中次数（需要重新计算） */
    private static long cacheMissCount = 0;
    /** 上次打印日志的时间戳 */
    private static long lastLogTime = System.currentTimeMillis();
    /** 求解开始时间戳 */
    private static long startTime = System.currentTimeMillis();
    /** 容量割平面剪枝统计：检查次数 */
    private long capacityCutChecks = 0;
    /** 容量割平面剪枝统计：成功剪枝次数 */
    private long capacityCutPrunes = 0;
    /** 对称性破坏统计：检查次数 */
    private long symmetryBreakingChecks = 0;
    /** 对称性破坏统计：成功剪枝次数 */
    private long symmetryBreakingPrunes = 0;

    // ==================== 构造函数 (Constructors) ====================

    /**
     * 构造函数（使用默认优化配置）
     *
     * @param dataFile 数据文件路径
     * @param cycleTime 节拍时间
     * @param totalRobots 可用机器人总数
     * @throws IOException 文件读取异常
     */
    public NestedSALBPProblem(String dataFile, int cycleTime, int totalRobots) throws IOException {
        this(dataFile, cycleTime, totalRobots, true, true, true, true);  // 默认所有优化都启用
    }

    /**
     * 构造函数（完整配置）
     *
     * @param dataFile 数据文件路径
     * @param cycleTime 节拍时间
     * @param totalRobots 可用机器人总数
     * @param useInfeasibilityCache 是否启用不可行子集缓存
     * @param useCapacityCut 是否启用容量割平面
     * @param useBoundPropagation 是否启用上界传播
     * @param useSymmetryBreaking 是否启用对称性破坏
     * @throws IOException 文件读取异常
     */
    public NestedSALBPProblem(String dataFile, int cycleTime, int totalRobots,
                              boolean useInfeasibilityCache, boolean useCapacityCut,
                              boolean useBoundPropagation, boolean useSymmetryBreaking) throws IOException {
        // 初始化内层问题
        this.innerProblem = new SSALBRBProblem(dataFile);
        this.nbTasks = innerProblem.nbTasks;
        this.cycleTime = cycleTime;
        this.totalRobots = totalRobots;
        this.predecessors = innerProblem.predecessors;
        this.successors = innerProblem.successors;

        // 初始化缓存
        this.makespanCache = new HashMap<>();
        this.solutionCache = new HashMap<>();

        // 初始化优化组件和开关
        this.useInfeasibilityCache = useInfeasibilityCache;
        this.useCapacityCut = useCapacityCut;
        this.useBoundPropagation = useBoundPropagation;
        this.useSymmetryBreaking = useSymmetryBreaking;
        this.infeasibilityCache = new InfeasibilityCache();
        this.boundPropagation = new BoundPropagation();

        // 打印优化配置
        System.out.println("=== 优化配置 ===");
        System.out.println("容量割平面: " + (useCapacityCut ? "启用 ✓" : "禁用 ✗"));
        System.out.println("InfeasibilityCache: " + (useInfeasibilityCache ? "启用 ✓" : "禁用 ✗"));
        System.out.println("BoundPropagation: " + (useBoundPropagation ? "启用 ✓" : "禁用 ✗"));
        System.out.println("SymmetryBreaking: " + (useSymmetryBreaking ? "启用 ✓" : "禁用 ✗"));
        System.out.println();

        // 问题可行性检查：检查是否存在任务即使用最快的加工方式也超过节拍时间
        checkProblemFeasibility();

        // 打印需要机器人的任务信息
        printRobotRequirementAnalysis();
    }

    // ==================== 问题可行性检查 (Problem Feasibility Check) ====================
    /**
     * 检查问题是否可行：是否存在任务即使用最快的加工方式也超过节拍时间
     *
     * 检查逻辑：
     * - 对每个任务，计算三种模式（人工、机器人、协同）中的最小加工时间
     * - 如果最小加工时间 > 节拍时间，则该任务无法在任何工位中完成
     * - 此时问题不可行，直接抛出异常
     *
     * @throws IllegalArgumentException 如果问题不可行
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
     *
     * @param task 任务索引
     * @return 最小加工时间
     */
    private int getMinDuration(int task) {
        return Math.min(innerProblem.humanDurations[task],
                Math.min(innerProblem.robotDurations[task],
                        innerProblem.collaborationDurations[task]));
    }

    /**
     * 【机器人保留策略的核心】检查任务是否必须使用机器人
     *
     * 判断标准：人工时间 > 节拍时间
     *
     * 原理：
     * - 如果人工时间 > cycleTime，则该任务单独在无机器人工位上无法完成
     * - 这类任务称为"瓶颈任务"，必须分配到有机器人的工位
     * - 这是机器人保留策略的基础：当机器人稀缺时，优先为这些任务保留机器人
     *
     * 注意：这个方法是机器人保留策略的关键，在domain()中被大量使用
     *
     * @param task 任务索引
     * @return true 如果任务必须使用机器人
     */
    public boolean taskRequiresRobot(int task) {
        return innerProblem.humanDurations[task] > cycleTime;
    }

    /**
     * 打印需要机器人的任务分析
     *
     * 用途：帮助理解问题特征，判断机器人资源是否充足
     */
    private void printRobotRequirementAnalysis() {
        System.out.println("\n=== 任务分析 ===");
        int robotRequiredCount = 0;
        for (int task = 0; task < nbTasks; task++) {
            if (taskRequiresRobot(task)) {
                robotRequiredCount++;
                System.out.printf("任务 %d 必须使用机器人: humanDur=%d > cycleTime=%d, minDur=%d%n",
                        task + 1, innerProblem.humanDurations[task], cycleTime,
                        getMinDuration(task));
            }
        }
        System.out.printf("共有 %d 个任务必须使用机器人，可用机器人数: %d%n", robotRequiredCount, totalRobots);
        System.out.println();
    }

    // ==================== Problem接口实现 (Problem Interface Implementation) ====================

    /**
     * 返回初始状态
     *
     * 初始状态特征：
     * - 没有任何任务被完成
     * - 当前工位为空
     * - 没有使用任何机器人
     */
    @Override
    public NestedSALBPState initialState() {
        return new NestedSALBPState(
                Collections.emptySet(),         // 已完成任务为空
                Collections.emptySet(),         // 当前工位任务为空
                false,                          // 当前工位机器人状态（占位符）
                0);                             // 已使用机器人数为0
    }

    /**
     * 返回变量数量（等于任务数量）
     */
    @Override
    public int nbVars() {
        return nbTasks;
    }

    /**
     * 返回初始值（初始状态还没有任何工位）
     */
    @Override
    public double initialValue() {
        return 0;  // 初始状态还没有任何工位
    }

    /**
     * 返回最优值（如果已知）
     */
    @Override
    public Optional<Double> optimalValue() {
        return innerProblem.optimalValue();
    }

    // ==================== 决策域生成（核心逻辑）(Domain Generation - Core Logic) ====================

    /**
     * 生成决策域 - 这是整个算法的核心方法
     *
     * ============================================================================
     * 决策编码：decision = task * 2 + robotFlag
     * ============================================================================
     * - task: 要分配的任务索引 (0 到 nbTasks-1)
     * - robotFlag: 0 = 加入当前工位或开新工位不分配机器人
     *              1 = 开新工位并分配机器人
     *
     * ============================================================================
     * 核心策略：
     * ============================================================================
     *
     * 1. 【机器人保留策略】Robot Reservation Strategy
     *    - 计算剩余任务中有多少必须使用机器人（瓶颈任务）
     *    - 当剩余机器人 <= 需要机器人的任务数时：
     *      * robotsCriticallyShort: 真的不够，只处理需要机器人的任务
     *      * robotsAreScarce: 刚好够用，限制非必需任务使用机器人
     *    - 目的：确保瓶颈任务能获得机器人，避免问题不可行
     *
     * 2. 【前继约束】Precedence Constraints
     *    - 只考虑eligible的任务（所有前继已完成或在当前工位中）
     *    - 确保任务分配满足优先级关系
     *
     * 3. 【懒惰求值】Lazy Evaluation
     *    - 使用乐观估计（sumMinDurations）快速判断可行性
     *    - 减少内层DDO调用次数，提高效率
     *
     * 4. 【上界传播剪枝】Bound Propagation Pruning
     *    - 计算当前状态的下界（已完成工位数 + 未完成任务需要的工位数下界）
     *    - 如果下界 >= 已知上界，则剪枝（不生成任何决策）
     *
     * 5. 【对称性破坏】Symmetry Breaking
     *    - 强制工位按"第一个任务的编号"排序
     *    - 避免探索对称的工位排列（如 [(3,4), (1,2)] 和 [(1,2), (3,4)]）
     *
     * @param state 当前状态
     * @param var 当前变量索引（未使用，保留用于接口兼容）
     * @return 可行决策的迭代器
     */
    @Override
    public Iterator<Integer> domain(NestedSALBPState state, int var) {
        domainCallCount++;
        logProgress(state, var);

        // 如果所有任务都已完成，返回空决策
        if (state.isComplete(nbTasks)) {
            return Collections.emptyIterator();
        }

        // ========== 优化策略1：上界传播剪枝 ==========
        if (useBoundPropagation) {
            // 计算当前已完成的工位数（保守估计）
            int completedStations = computeCompletedStations(state);

            // 计算未完成任务（当前工位 + 剩余任务）需要的工位数下界
            // 关键：将当前工位任务和剩余任务一起计算，避免高估
            double unfinishedStationsLB = computeLowerBoundForUnfinishedTasks(state);

            // 总下界 = 已完成工位数 + 未完成任务的工位数下界
            double totalLowerBound = completedStations + unfinishedStationsLB;

//            // ✅ 安全边际：由于DDO状态合并可能导致下界高估，使用安全边际
//            double safetyMargin = 1.0;  // 基础安全边际
//
//            // 如果当前工位非空，增加边际（状态信息可能不准确）
//            if (!state.currentStationTasks().isEmpty()) {
//                safetyMargin += 0.5;
//            }
//
//            // 如果机器人使用率高，增加边际（机器人信息可能在合并时丢失）
//            double robotUsageRatio = (double) state.usedRobots() / totalRobots;
//            if (robotUsageRatio > 0.7) {
//                safetyMargin += 0.5;
//            }

            // 使用计算后的下界进行剪枝
            double relaxedLowerBound = totalLowerBound;
//            double relaxedLowerBound = totalLowerBound - safetyMargin;

            // 检查是否可以剪枝
            if (boundPropagation.canPrune(0, relaxedLowerBound)) {
                return Collections.emptyIterator();  // 剪枝：当前分支无希望
            }
        }

        // ========== 准备决策生成 ==========
        List<Integer> decisions = new ArrayList<>();
        Set<Integer> remaining = state.getRemainingTasks(nbTasks);
        Set<Integer> currentStationTasks = state.currentStationTasks();
        int remainingRobots = state.remainingRobots(totalRobots);

        // ========== 机器人保留策略：计算剩余任务中有多少必须使用机器人 ==========
        int tasksRequiringRobot = 0;
        for (int t : remaining) {
            if (taskRequiresRobot(t)) {
                tasksRequiringRobot++;
            }
        }

        // 判断机器人是否紧缺
        boolean robotsCriticallyShort = (remainingRobots < tasksRequiringRobot);  // 真的不够
        boolean robotsAreScarce = (remainingRobots <= tasksRequiringRobot);       // 刚好够用

        // ========== 分类eligible任务 ==========
        List<Integer> eligibleRobotTasks = new ArrayList<>();    // 需要机器人的任务
        List<Integer> eligibleNormalTasks = new ArrayList<>();   // 不需要机器人的任务
        for (int task : remaining) {
            if (isTaskEligible(task, remaining, currentStationTasks)) {
                if (taskRequiresRobot(task)) {
                    eligibleRobotTasks.add(task);
                } else {
                    eligibleNormalTasks.add(task);
                }
            }
        }

        // ========== 决定要处理哪些任务 ==========
        List<Integer> tasksToProcess;
        if (!eligibleRobotTasks.isEmpty() && robotsCriticallyShort) {
            // 机器人真的不够了，只处理需要机器人的任务
            tasksToProcess = eligibleRobotTasks;
        } else {
            // 处理所有eligible任务
            tasksToProcess = new ArrayList<>();
            tasksToProcess.addAll(eligibleRobotTasks);
            tasksToProcess.addAll(eligibleNormalTasks);
        }

        // ========== 生成决策 ==========
        for (int task : tasksToProcess) {
            boolean requiresRobot = taskRequiresRobot(task);

            if (currentStationTasks.isEmpty()) {
                // 情况1：新开工位（当前工位为空）
                generateDecisionsForNewStation(decisions, task, requiresRobot, remainingRobots, robotsAreScarce);
            } else {
                // 情况2：当前工位已打开，判断能否继续放入
                generateDecisionsForExistingStation(decisions, task, requiresRobot,
                        state, remainingRobots, robotsAreScarce, currentStationTasks);
            }
        }

        // ========== 优化策略2：对称性破坏 ==========
        if (useSymmetryBreaking) {
            applySymmetryBreaking(state, decisions, remaining, currentStationTasks);
        }

        // 打印调试信息
        logDecisions(state, decisions, tasksRequiringRobot, remainingRobots, robotsAreScarce);

        return decisions.iterator();
    }

    /**
     * 应用对称性破坏约束
     *
     * ============================================================================
     * 规则：任务编号最小的未分配任务必须分配到下一个新工位
     * ============================================================================
     *
     * 原理：
     * - 强制工位按照"第一个任务的编号"排序
     * - 避免探索对称的工位排列
     *
     * 例如：
     * - 如果任务1还未分配，则任务1必须在下一个新工位
     * - 这样可以避免 [(3,4), (1,2)] 这种情况（因为1必须先分配）
     * - 只保留 [(1,2), (3,4)] 这种排列
     *
     * 实现：
     * - 找到最小的未分配且eligible的任务
     * - 移除其他任务的"开新工位"决策（robotFlag=1）
     * - 保留最小任务的所有决策 + 其他任务的"加入当前工位"决策
     *
     * @param state 当前状态
     * @param decisions 决策列表（会被修改）
     * @param remaining 剩余任务集合
     * @param currentStationTasks 当前工位任务集合
     */
    private void applySymmetryBreaking(NestedSALBPState state, List<Integer> decisions,
                                       Set<Integer> remaining, Set<Integer> currentStationTasks) {
        // 只在要开新工位时应用（当前工位为空，且不是第一个工位）
        if (!currentStationTasks.isEmpty() || state.completedTasks().isEmpty()) {
            return;
        }

        symmetryBreakingChecks++;

        // 找到最小的未分配且eligible的任务
        int minRemainingTask = remaining.stream()
                .filter(t -> isTaskEligible(t, remaining, currentStationTasks))
                .min(Integer::compareTo)
                .orElse(-1);

        if (minRemainingTask == -1) {
            return;  // 没有eligible任务
        }

        // 统计剪枝前的决策数
        int beforeSize = decisions.size();

        // 只允许最小的任务开新工位
        // 过滤掉其他任务的"开新工位"决策
        decisions.removeIf(d -> {
            int task = d / 2;
            int robotFlag = d % 2;
            // 保留：最小任务的所有决策 + 其他任务的"加入当前工位"决策
            // 移除：其他任务的"开新工位"决策
            return task != minRemainingTask && robotFlag == 1;
        });

        // 统计剪枝数
        int afterSize = decisions.size();
        if (afterSize < beforeSize) {
            symmetryBreakingPrunes += (beforeSize - afterSize);
        }
    }

    // ==================== 决策生成辅助方法 (Decision Generation Helper Methods) ====================

    /**
     * 为新开工位生成决策
     *
     * 场景：当前工位为空，需要开启新工位
     *
     * 决策逻辑：
     * 1. 如果任务必须使用机器人（requiresRobot=true）：
     *    - 只生成"新工位+分配机器人"决策（robotFlag=1）
     *    - 前提：还有剩余机器人
     *
     * 2. 如果任务不强制需要机器人（requiresRobot=false）：
     *    - 如果机器人不紧缺：生成"新工位+分配机器人"和"新工位+不分配机器人"两种决策
     *    - 如果机器人紧缺：只生成"新工位+不分配机器人"决策（保留机器人给瓶颈任务）
     *
     * @param decisions 决策列表（会被修改）
     * @param task 任务索引
     * @param requiresRobot 任务是否必须使用机器人
     * @param remainingRobots 剩余机器人数量
     * @param robotsAreScarce 机器人是否紧缺
     */
    private void generateDecisionsForNewStation(List<Integer> decisions, int task,
                                                boolean requiresRobot, int remainingRobots, boolean robotsAreScarce) {
        if (requiresRobot) {
            // 任务必须使用机器人
            if (remainingRobots > 0) {
                decisions.add(task * 2 + 1);  // 新工位，必须分配机器人
            }
        } else {
            // 任务不强制需要机器人
            if (remainingRobots > 0 && !robotsAreScarce) {
                decisions.add(task * 2 + 1);  // 新工位，分配机器人（机器人充足时的选项）
            }
            decisions.add(task * 2 + 0);  // 新工位，不分配机器人
        }
    }

    /**
     * 为现有工位生成决策
     *
     * 场景：当前工位已有任务，判断能否继续放入新任务
     *
     * 决策逻辑：
     * 1. 如果任务必须使用机器人，但当前工位没有机器人：
     *    - 必须新开工位并分配机器人
     *
     * 2. 使用乐观估计判断能否加入当前工位：
     *    - 可行：生成"加入当前工位"决策（robotFlag=0）
     *    - 不可行：需要新开工位
     *      * 使用Maximum Load Rule检查当前工位是否还能加入其他任务
     *      * 如果工位未满，只允许"加入当前工位"（让transition()做精确判断）
     *      * 如果工位已满，允许"开新工位"
     *
     * @param decisions 决策列表（会被修改）
     * @param task 任务索引
     * @param requiresRobot 任务是否必须使用机器人
     * @param state 当前状态
     * @param remainingRobots 剩余机器人数量
     * @param robotsAreScarce 机器人是否紧缺
     * @param currentStationTasks 当前工位任务集合
     */
    private void generateDecisionsForExistingStation(List<Integer> decisions, int task,
                                                     boolean requiresRobot, NestedSALBPState state, int remainingRobots,
                                                     boolean robotsAreScarce, Set<Integer> currentStationTasks) {

        // 如果任务必须使用机器人，但当前工位没有机器人，则必须新开工位
        if (requiresRobot && !state.currentStationHasRobot()) {
            if (remainingRobots > 0) {
                decisions.add(task * 2 + 1);  // 新工位，必须分配机器人
            }
            return;
        }

        // 构造测试任务集合（当前工位 + 新任务）
        Set<Integer> testTasks = new LinkedHashSet<>(currentStationTasks);
        testTasks.add(task);

        // 使用乐观估计（减少内层DDO调用）
        if (isStationFeasibleOptimistic(testTasks, state.currentStationHasRobot())) {
            decisions.add(task * 2 + 0);  // 加入当前工位
        } else {
            // 乐观估计认为不可行，需要新开工位

            // ✅ Maximum Load Rule：检查是否还有其他任务可以加入当前工位
            boolean canAddMore = canAddMoreTasksToCurrentStation(state);

            if (requiresRobot) {
                // 只有当工位真的满了，才允许开新工位
                if (!canAddMore && remainingRobots > 0) {
                    decisions.add(task * 2 + 1);  // 新工位，必须分配机器人
                }
                // 如果当前工位有机器人，仍然尝试加入（让transition()做精确判断）
                if (state.currentStationHasRobot()) {
                    decisions.add(task * 2 + 0);  // 尝试加入当前有机器人工位
                }
            } else {
                // 只有当工位真的满了，才允许开新工位
                if (!canAddMore) {
                    if (remainingRobots > 0 && !robotsAreScarce) {
                        decisions.add(task * 2 + 1);  // 新工位，分配机器人
                    }
                    decisions.add(task * 2 + 0);  // 开新无机器人工位
                } else {
                    // 工位未满，只允许加入当前工位
                    decisions.add(task * 2 + 0);  // 尝试加入当前工位
                }
            }
        }
    }

    /**
     * 检查任务是否eligible（所有前继要么已完成，要么在当前工位中）
     *
     * 前继约束：任务只有在所有前继任务都已分配后才能被分配
     *
     * @param task 任务索引
     * @param remaining 剩余任务集合
     * @param currentStationTasks 当前工位任务集合
     * @return true 如果任务eligible
     */
    private boolean isTaskEligible(int task, Set<Integer> remaining, Set<Integer> currentStationTasks) {
        for (int pred : predecessors.get(task)) {
            if (remaining.contains(pred) && !currentStationTasks.contains(pred)) {
                return false;  // 存在未完成且不在当前工位的前继任务
            }
        }
        return true;
    }

    /**
     * Maximum Load Rule：检查当前工位是否还能加入更多任务
     *
     * ============================================================================
     * 核心思想：如果当前工位还有剩余容量，且存在任务可以加入，
     *          则不应该关闭当前工位开新工位。
     * ============================================================================
     *
     * 目的：
     * - 避免过早关闭工位，导致工位利用率低
     * - 确保在关闭工位前已经尽可能多地放入任务
     *
     * 检查逻辑：
     * 1. 遍历所有剩余任务
     * 2. 检查前继约束（是否eligible）
     * 3. 检查容量约束（使用乐观估计，避免调用内层DDO）
     * 4. 如果存在任务可以加入，返回true
     *
     * @param state 当前状态
     * @return true 如果还有任务可以加入当前工位
     */
    private boolean canAddMoreTasksToCurrentStation(NestedSALBPState state) {
        if (state.currentStationTasks().isEmpty()) {
            return false; // 当前工位为空，不适用
        }

        Set<Integer> remaining = state.getRemainingTasks(nbTasks);
        Set<Integer> currentTasks = state.currentStationTasks();

        // 检查是否有eligible任务可以加入
        for (int task : remaining) {
            // 检查前继约束
            if (!isTaskEligible(task, remaining, currentTasks)) {
                continue;
            }

            // 检查容量约束（使用乐观估计，避免调用内层DDO）
            Set<Integer> testTasks = new HashSet<>(currentTasks);
            testTasks.add(task);

            if (isStationFeasibleOptimistic(testTasks, state.currentStationHasRobot())) {
                return true; // 有任务可以加入
            }
        }

        return false; // 没有任务可以加入，工位已满
    }

    // ==================== 状态转移 (State Transition) ====================

    /**
     * 状态转移函数：根据决策生成新状态
     *
     * 决策解码：
     * - task = decision / 2：要分配的任务
     * - robotFlag = decision % 2：机器人标志（0或1）
     *
     * 转移逻辑：
     * 1. 判断是否需要新开工位（通过精确可行性检查）
     * 2. 如果不需要新开工位：将任务加入当前工位
     * 3. 如果需要新开工位：
     *    - 关闭当前工位（将任务移到completedTasks，更新usedRobots）
     *    - 开启新工位（将新任务放入新工位，根据需要分配机器人）
     *
     * @param state 当前状态
     * @param decision 决策
     * @return 新状态
     */
    @Override
    public NestedSALBPState transition(NestedSALBPState state, Decision decision) {
        int decisionVal = decision.val();
        int task = decisionVal / 2;
        int robotFlag = decisionVal % 2;
        boolean assignRobot = (robotFlag == 1);

        // 判断是否需要新开工位（使用精确可行性检查）
        boolean needNewStation = false;

        if (state.currentStationTasks().isEmpty()) {
            // 当前工位为空，必须新开工位
            needNewStation = true;
        } else {
            // 当前工位非空，检查能否加入
            Set<Integer> testTasks = new LinkedHashSet<>(state.currentStationTasks());
            testTasks.add(task);
            needNewStation = !isStationFeasible(testTasks, state.currentStationHasRobot());
        }

        if (!needNewStation) {
            // 情况1：加入当前工位
            Set<Integer> newStationTasks = new LinkedHashSet<>(state.currentStationTasks());
            newStationTasks.add(task);

            return new NestedSALBPState(
                    state.completedTasks(),
                    newStationTasks,
                    state.currentStationHasRobot(),
                    state.usedRobots());
        } else {
            // 情况2：新开工位
            Set<Integer> newCompletedTasks = new LinkedHashSet<>(state.completedTasks());
            int newUsedRobots = state.usedRobots();

            // 关闭当前工位（如果非空）
            if (!state.currentStationTasks().isEmpty()) {
                newCompletedTasks.addAll(state.currentStationTasks());
                if (state.currentStationHasRobot()) {
                    newUsedRobots++;
                }
            }

            // 开启新工位
            Set<Integer> freshStationTasks = Set.of(task);

            // 如果任务需要机器人，新工位必须有机器人
            boolean newStationHasRobot = assignRobot;
            if (taskRequiresRobot(task)) {
                int availableRobots = totalRobots - newUsedRobots;
                if (availableRobots > 0) {
                    newStationHasRobot = true;
                }
            }

            return new NestedSALBPState(
                    newCompletedTasks,
                    freshStationTasks,
                    newStationHasRobot,
                    newUsedRobots);
        }
    }

    /**
     * 计算转移代价（开新工位代价为1，加入当前工位代价为0）
     *
     * @param state 当前状态
     * @param decision 决策
     * @return 转移代价
     */
    @Override
    public double transitionCost(NestedSALBPState state, Decision decision) {
        int decisionVal = decision.val();
        int task = decisionVal / 2;

        if (state.currentStationTasks().isEmpty()) {
            return 1.0;  // 开启第一个工位
        }

        Set<Integer> testTasks = new LinkedHashSet<>(state.currentStationTasks());
        testTasks.add(task);

        if (isStationFeasible(testTasks, state.currentStationHasRobot())) {
            return 0.0;  // 加入当前工位，不增加工位数
        } else {
            return 1.0;  // 新开工位，工位数+1
        }
    }

    /**
     * 评估完整解的目标值（工位数）
     *
     * 用途：验证解的正确性，计算最终目标值
     *
     * @param solution 完整解（决策序列）
     * @return 工位数
     * @throws org.ddolib.modeling.InvalidSolutionException 如果解不合法
     */
    @Override
    public double evaluate(int[] solution) throws org.ddolib.modeling.InvalidSolutionException {
        NestedSALBPState state = initialState();
        int stationCount = 0;

        // 模拟整个决策序列
        for (int decisionVal : solution) {
            int task = decisionVal / 2;
            int robotFlag = decisionVal % 2;
            boolean assignRobot = (robotFlag == 1);

            boolean needNewStation = false;

            if (state.currentStationTasks().isEmpty()) {
                needNewStation = true;
            } else {
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

                if (!state.currentStationTasks().isEmpty()) {
                    newCompletedTasks.addAll(state.currentStationTasks());
                    stationCount++;
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

        // 最后一个工位
        if (!state.currentStationTasks().isEmpty()) {
            stationCount++;
        }
        return stationCount;
    }

    // ==================== 工位可行性判断 (Station Feasibility Check) ====================

    /**
     * 乐观版本：用于domain()生成决策时的快速判断
     *
     * 目的：减少内层DDO调用次数，提高效率
     *
     * 判断逻辑：
     * 1. 如果工位没有机器人：
     *    - 检查是否存在必须使用机器人的任务 → 不可行
     *    - 检查人工时间之和是否超过节拍时间 → 超过则不可行
     *
     * 2. 如果工位有机器人：
     *    - 使用sumMinDurations（最小时间之和）进行乐观估计
     *    - 如果最小时间之和 <= 节拍时间 → 可能可行（乐观）
     *
     * 注意：这是乐观估计，可能会误判为可行（但不会误判为不可行）
     *
     * @param tasks 任务集合
     * @param hasRobot 是否有机器人
     * @return true 如果乐观估计认为可行
     */
    private boolean isStationFeasibleOptimistic(Set<Integer> tasks, boolean hasRobot) {
        // 如果工位没有机器人，但存在任务必须使用机器人，则不可行
        if (!hasRobot) {
            for (int task : tasks) {
                if (taskRequiresRobot(task)) {
                    return false;
                }
            }
            return sumHumanDurations(tasks) <= cycleTime;
        }

        // 有机器人：用sumMinDur乐观估计
        return sumMinDurations(tasks) <= cycleTime;
    }

    /**
     * 精确版本：用于transition()和evaluate()的精确判断
     *
     * 目的：确保状态转移的正确性，避免生成不可行的状态
     *
     * ============================================================================
     * 优化策略执行顺序（从快到慢）：
     * ============================================================================
     * 1. 容量割平面 (Capacity Cut) - 检查最小时间和是否超过容量
     * 2. 不可行子集缓存 (Infeasibility Cache) - 检查是否包含已知的不可行子集
     * 3. Makespan缓存 - 查询缓存的makespan
     * 4. 内层DDO - 精确计算（最慢，最后才调用）
     *
     * ============================================================================
     * 判断逻辑：
     * ============================================================================
     * 1. 【容量割平面】如果有机器人，检查最小时间和是否超过理论容量上限
     *    - 理论容量 = 1.5 × cycleTime（人和机器人并行工作的理论上限）
     *    - 如果最小时间和 > 理论容量 → 不可行
     *
     * 2. 【不可行子集缓存】检查是否包含已知的不可行子集
     *    - 如果包含 → 不可行
     *
     * 3. 【快速检查】如果工位没有机器人：
     *    - 检查是否存在必须使用机器人的任务 → 不可行
     *    - 检查人工时间之和是否超过节拍时间 → 超过则不可行
     *
     * 4. 【乐观估计】如果人工时间之和 <= 节拍时间 → 可行
     *
     * 5. 【精确计算】调用内层DDO计算makespan
     *    - 如果makespan <= 节拍时间 → 可行
     *    - 否则 → 不可行，并记录到不可行子集缓存
     *
     * @param tasks 任务集合
     * @param hasRobot 是否有机器人
     * @return true 如果精确判断认为可行
     */
    private boolean isStationFeasible(Set<Integer> tasks, boolean hasRobot) {
        // ========== 优化1：容量割平面（最快的检查） ==========
        // 注意：容量割平面只对有机器人的情况有效
        // 因为无机器人时，后续的 sumHumanDurations 检查更精确
        if (useCapacityCut && hasRobot) {
            capacityCutChecks++;  // 记录检查次数

            // 检查最小时间和是否超过容量
            // 有机器人时，人和机器人可以并行，最理想情况下能够达到的最大容量是 1.5 × cycleTime
            // （并行和协同时间分别为2/3和0.7）
            int minTotalDuration = sumMinDurations(tasks);
            int capacity = (int) Math.round(1.5 * cycleTime);

            if (minTotalDuration > capacity) {
                capacityCutPrunes++;  // 记录剪枝次数
                return false; // 容量割平面剪枝
            }
        }

        // ========== 优化2：不可行子集剪枝 ==========
        if (useInfeasibilityCache && hasRobot && infeasibilityCache.containsInfeasibleSubset(tasks)) {
            return false; // 不可行子集剪枝
        }

        // ========== 原有的可行性检查 ==========
        // 如果工位没有机器人，但存在任务必须使用机器人，则不可行
        if (!hasRobot) {
            for (int task : tasks) {
                if (taskRequiresRobot(task)) {
                    return false;
                }
            }
        }

        int sumHuman = sumHumanDurations(tasks);

        if (sumHuman <= cycleTime) {
            return true;  // 乐观情况：工人时间之和都不超
        }

        if (!hasRobot) {
            return false;  // 无机器人且sumHuman > cycleTime
        }

        // ========== 优化3：查询makespanCache，优化4：调用内层DDO ==========
        // 有机器人：需要精确计算
        int makespan = computeStationMakespan(tasks, true);
        boolean feasible = makespan <= cycleTime;

        // ========== 记录不可行子集 ==========
        if (useInfeasibilityCache && !feasible) {
            infeasibilityCache.recordInfeasible(tasks, true);
        }

        return feasible;
    }

    /**
     * 计算工人时间之和（乐观下界）
     *
     * 用途：快速估计工位负载
     *
     * @param tasks 任务集合
     * @return 工人时间之和
     */
    private int sumHumanDurations(Set<Integer> tasks) {
        int sum = 0;
        for (int task : tasks) {
            sum += innerProblem.humanDurations[task];
        }
        return sum;
    }

    /**
     * 计算minDur之和（更乐观的下界）
     *
     * 用途：乐观估计，用于快速判断可行性
     *
     * @param tasks 任务集合
     * @return 最小时间之和
     */
    private int sumMinDurations(Set<Integer> tasks) {
        int sum = 0;
        for (int task : tasks) {
            sum += getMinDuration(task);
        }
        return sum;
    }

    // ==================== 内层DDO求解 (Inner DDO Solving) ====================

    /**
     * 调用内层模型计算单工位makespan
     *
     * 优化：同时缓存makespan和解序列，避免重复调用DDO求解器
     *
     * 工作流程：
     * 1. 查询makespan缓存，如果命中则直接返回
     * 2. 如果没有机器人：直接计算人工时间之和（无需调用DDO）
     * 3. 如果有机器人：调用内层DDO求解器
     *    - 创建子问题（只包含指定任务）
     *    - 调用DDO求解器获取最优调度方案
     *    - 同时缓存makespan和解序列
     *
     * @param tasks 任务集合
     * @param hasRobot 是否有机器人
     * @return makespan（如果不可行则返回Integer.MAX_VALUE）
     */
    public int computeStationMakespan(Set<Integer> tasks, boolean hasRobot) {
        // 查makespan缓存
        if (makespanCache.containsKey(tasks) && makespanCache.get(tasks).containsKey(hasRobot)) {
            cacheHitCount++;
            return makespanCache.get(tasks).get(hasRobot);
        }
        cacheMissCount++;

        int makespan;

        if (!hasRobot) {
            // 优化：没有机器人时，所有任务只能用人工模式，直接计算总时间
            makespan = 0;
            for (int task : tasks) {
                makespan += innerProblem.humanDurations[task];
            }
            // 无机器人情况不需要缓存解序列（因为顺序不重要）
        } else {
            // 有机器人时，需要调用内层DDO求解器
            innerDdoCallCount++;
            SSALBRBProblem subProblem = createSubProblem(tasks, true);

            // 调用统一的求解方法，同时获取makespan和解序列
            int[] solution = solveDDOForSolution(subProblem);

            if (solution != null && solution.length > 0) {
                try {
                    makespan = (int) subProblem.evaluate(solution);
                    // 同时缓存解序列，供打印时使用
                    solutionCache.computeIfAbsent(tasks, k -> new HashMap<>()).put(hasRobot, solution);
                } catch (Exception e) {
                    makespan = Integer.MAX_VALUE;
                }
            } else {
                makespan = Integer.MAX_VALUE;
            }
        }

        // 缓存makespan结果
        makespanCache.computeIfAbsent(tasks, k -> new HashMap<>()).put(hasRobot, makespan);

        return makespan;
    }

    /**
     * 创建子问题：只包含指定任务
     *
     * 工作流程：
     * 1. 将任务集合转换为列表（建立索引映射）
     * 2. 提取子问题的任务时间数据
     * 3. 构建子问题的前继关系（映射到新索引）
     * 4. 如果没有机器人，禁用robot和collaboration模式（设置为极大值）
     *
     * @param tasks 任务集合
     * @param hasRobot 是否有机器人
     * @return 子问题实例
     */
    private SSALBRBProblem createSubProblem(Set<Integer> tasks, boolean hasRobot) {
        List<Integer> taskList = new ArrayList<>(tasks);
        int subNbTasks = taskList.size();

        // 提取子问题的任务时间数据
        int[] subHDurations = new int[subNbTasks];
        int[] subRDurations = new int[subNbTasks];
        int[] subCDurations = new int[subNbTasks];

        for (int i = 0; i < subNbTasks; i++) {
            int originalTask = taskList.get(i);
            subHDurations[i] = innerProblem.humanDurations[originalTask];

            if (hasRobot) {
                subRDurations[i] = innerProblem.robotDurations[originalTask];
                subCDurations[i] = innerProblem.collaborationDurations[originalTask];
            } else {
                // 没有机器人：禁用robot和collaboration模式
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
                if (subIndex >= 0) {
                    subSuccs.add(subIndex);
                }
            }
            subSuccessors.put(i, subSuccs);
        }

        return new SSALBRBProblem(subNbTasks, subHDurations, subRDurations, subCDurations, subSuccessors);
    }

    /**
     * 使用DDO求解器求解单工位调度问题，返回解的顺序
     *
     * 这是统一的求解方法，同时用于计算makespan和获取详细解
     *
     * 配置：
     * - Relaxation: SSALBRBRelax（松弛合并策略）
     * - Ranking: SSALBRBRanking（状态排序策略）
     * - FastLowerBound: SSALBRBFastLowerBound（快速下界）
     * - Width: FixedWidth(10)（固定宽度为10）
     *
     * @param problem 内层问题实例
     * @return 解序列（决策数组）
     */
    private int[] solveDDOForSolution(SSALBRBProblem problem) {
        // 构建内层DDO模型
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

        // 调用DDO求解器
        Solution solution = Solvers.minimizeDdo(innerModel, (sol, searchStats) -> {
            if (sol != null && sol.length > 0) {
                resultSolution[0] = sol;
            }
        });

        return resultSolution[0];
    }

    // ==================== 内层问题解的详细信息 (Inner Solution Details) ====================

    /**
     * 内层问题的解（包含任务和操作模式）
     *
     * 用途：表示单个工位的详细调度方案
     */
    public static class InnerSolution {
        /** 任务序列（原始索引） */
        public final int[] tasks;

        /** 对应的操作模式（0=Human, 1=Robot, 2=Collaboration） */
        public final int[] modes;

        public InnerSolution(int[] tasks, int[] modes) {
            this.tasks = tasks;
            this.modes = modes;
        }
    }

    /**
     * 求解内层问题并返回任务调度顺序和操作模式
     *
     * 优化：优先使用缓存的解序列，避免重复调用DDO求解器
     *
     * 工作流程：
     * 1. 查询解序列缓存
     * 2. 如果缓存未命中，调用DDO求解器并缓存结果
     * 3. 将子问题的决策值解码并映射回原始索引
     *
     * @param stationTasks 工位任务集合
     * @param hasRobot 是否有机器人
     * @return 内层解（包含任务序列和操作模式）
     */
    public InnerSolution solveInnerProblemWithModes(Set<Integer> stationTasks, boolean hasRobot) {
        List<Integer> taskList = new ArrayList<>(stationTasks);

        // 先查解序列缓存
        int[] subSolution = null;
        if (solutionCache.containsKey(stationTasks) &&
                solutionCache.get(stationTasks).containsKey(hasRobot)) {
            subSolution = solutionCache.get(stationTasks).get(hasRobot);
        } else {
            // 缓存未命中，调用DDO求解器
            SSALBRBProblem subProblem = createSubProblem(stationTasks, hasRobot);
            subSolution = solveDDOForSolution(subProblem);

            // 缓存解序列
            if (subSolution != null && subSolution.length > 0) {
                solutionCache.computeIfAbsent(stationTasks, k -> new HashMap<>())
                        .put(hasRobot, subSolution);
            }
        }

        if (subSolution == null || subSolution.length == 0) {
            return null;
        }

        // 将子问题的决策值解码并映射回原始索引
        int[] originalTasks = new int[subSolution.length];
        int[] modes = new int[subSolution.length];

        for (int i = 0; i < subSolution.length; i++) {
            int decision = subSolution[i];
            int subTaskIndex = decision / 3;
            int mode = decision % 3;

            originalTasks[i] = taskList.get(subTaskIndex);
            modes[i] = mode;
        }

        return new InnerSolution(originalTasks, modes);
    }

    /**
     * 求解内层问题并返回任务调度顺序（不包含操作模式）
     *
     * @param stationTasks 工位任务集合
     * @param hasRobot 是否有机器人
     * @return 任务序列
     */
    public int[] solveInnerProblem(Set<Integer> stationTasks, boolean hasRobot) {
        InnerSolution solution = solveInnerProblemWithModes(stationTasks, hasRobot);
        return solution != null ? solution.tasks : null;
    }

    // ==================== 调试和日志 (Debug and Logging) ====================

    /**
     * 打印缓存统计信息
     *
     * 包括：
     * - 缓存条目数量
     * - 缓存命中率
     * - 内层DDO调用次数
     * - 估算内存使用
     */
    public void printCacheStatistics() {
        System.out.println("\n=== Cache Statistics ===");
        System.out.printf("Makespan cache entries: %d%n", makespanCache.size());
        System.out.printf("Solution cache entries: %d%n", solutionCache.size());
        System.out.printf("Cache hits: %d%n", cacheHitCount);
        System.out.printf("Cache misses: %d%n", cacheMissCount);
        System.out.printf("Inner DDO calls: %d%n", innerDdoCallCount);

        // 估算内存使用
        long makespanMemory = makespanCache.size() * 200;  // bytes per entry
        long solutionMemory = solutionCache.size() * 300;  // bytes per entry (approximate)
        System.out.printf("Estimated memory: makespan=%.2f KB, solution=%.2f KB, total=%.2f KB%n",
                makespanMemory / 1024.0,
                solutionMemory / 1024.0,
                (makespanMemory + solutionMemory) / 1024.0);
    }

    /**
     * 计算已完成的工位数（保守估计，确保是下界）
     *
     * ============================================================================
     * 思路：使用 FastLowerBound 的专业算法（LB1 和 LB2）来计算已完成任务至少需要多少个工位
     * ============================================================================
     *
     * 优势：
     * - 使用与未完成任务相同的下界算法（LB1 和 LB2）
     * - 考虑了已使用的机器人数量
     * - 比简单的任务数量或工作量估计更准确
     *
     * 关键：必须保证是下界（不能高估已完成的工位数）
     * - 如果高估了已完成工位数，会导致总下界偏大，可能错误剪枝掉最优解
     *
     * @param state 当前状态
     * @return 已完成的工位数下界
     */
    private int computeCompletedStations(NestedSALBPState state) {
        // 如果没有完成任何任务，则完成的工位数为0
        if (state.completedTasks().isEmpty()) {
            return 0;
        }

        // 使用 FastLowerBound 的专业算法计算已完成任务的下界
        NestedSALBPFastLowerBound lbCalculator = new NestedSALBPFastLowerBound(this);

        // 直接调用 computeLowerBound 方法（现在是 public 的）
        // 参数：已完成的任务集合 + 已使用的机器人数量
        double completedStationsLB = lbCalculator.computeLowerBound(
                state.completedTasks(),
                state.usedRobots()
        );

        // 向上取整，确保是整数工位数
        return (int) Math.ceil(completedStationsLB);
    }

    /**
     * 计算未完成任务（当前工位 + 剩余任务）需要的工位数下界（用于上界传播）
     *
     * ============================================================================
     * 使用 NestedSALBPFastLowerBound 的专业算法（LB1 和 LB2）
     * ============================================================================
     * - LB1: 基于工作量平衡的下界
     * - LB2: 基于改进的装箱问题下界
     *
     * ============================================================================
     * 关键区别：FastLowerBound 和上界传播的语义不同
     * ============================================================================
     *
     * FastLowerBound.fastLowerBound() 的语义：
     * - 返回"还需要的新工位数"（增量）
     * - 当前工位非空时，返回 totalStations - 1（因为当前工位已存在，不算"新"工位）
     *
     * 上界传播的语义：
     * - 需要计算"未完成任务需要的总工位数"（包括当前工位）
     * - 当前工位还没有被计入"已完成工位数"
     *
     * 因此：
     * - 如果当前工位为空：直接使用 FastLowerBound 的结果
     * - 如果当前工位非空：需要把 FastLowerBound 减掉的1加回来
     *
     * @param currentState 当前状态
     * @return 未完成任务需要的工位数下界
     */
    private double computeLowerBoundForUnfinishedTasks(NestedSALBPState currentState) {
        Set<Integer> remainingTasks = currentState.getRemainingTasks(nbTasks);
        Set<Integer> currentStationTasks = currentState.currentStationTasks();

        // 如果没有未完成的任务，返回0
        if (remainingTasks.isEmpty() && currentStationTasks.isEmpty()) {
            return 0;
        }

        // 使用 FastLowerBound 的专业算法
        NestedSALBPFastLowerBound lbCalculator = new NestedSALBPFastLowerBound(this);

        // 调用 fastLowerBound 获取"还需要的新工位数"
        double incrementalLB = lbCalculator.fastLowerBound(currentState, remainingTasks);

        // 如果当前工位非空，FastLowerBound 已经减了1（认为当前工位已存在）
        // 但在上界传播中，当前工位还没有被计入"已完成工位数"
        // 所以需要加回来
        if (!currentStationTasks.isEmpty()) {
            return incrementalLB + 1;
        } else {
            return incrementalLB;
        }
    }

    /**
     * 更新最优解（用于上界传播）
     *
     * @param solutionValue 解的目标值（工位数）
     */
    public void updateBestSolution(int solutionValue) {
        if (useBoundPropagation) {
            boundPropagation.updateBestSolution(solutionValue);
        }
    }

    /**
     * 打印优化统计信息
     *
     * 包括：
     * - 各优化策略的启用状态
     * - 剪枝次数和剪枝率
     * - 总剪枝效果
     */
    public void printOptimizationStatistics() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("优化策略统计信息");
        System.out.println("=".repeat(60));

        // 容量割平面统计
        System.out.println("容量割平面: " + (useCapacityCut ? "启用 ✓" : "禁用 ✗"));
        if (useCapacityCut) {
            double pruneRate = capacityCutChecks > 0 ? (100.0 * capacityCutPrunes / capacityCutChecks) : 0.0;
            System.out.printf("  CapacityCut: checks=%d, prunes=%d, pruneRate=%.2f%%%n",
                    capacityCutChecks, capacityCutPrunes, pruneRate);
        }

        // InfeasibilityCache统计
        System.out.println("InfeasibilityCache: " + (useInfeasibilityCache ? "启用 ✓" : "禁用 ✗"));
        if (useInfeasibilityCache) {
            System.out.println("  " + infeasibilityCache.getStatistics());
        }

        // BoundPropagation统计
        System.out.println("BoundPropagation: " + (useBoundPropagation ? "启用 ✓" : "禁用 ✗"));
        if (useBoundPropagation) {
            System.out.println("  " + boundPropagation.getStatistics());
        }

        // SymmetryBreaking统计
        System.out.println("SymmetryBreaking: " + (useSymmetryBreaking ? "启用 ✓" : "禁用 ✗"));
        if (useSymmetryBreaking) {
            double pruneRate = symmetryBreakingChecks > 0 ?
                    (100.0 * symmetryBreakingPrunes / symmetryBreakingChecks) : 0.0;
            System.out.printf("  SymmetryBreaking: checks=%d, prunes=%d, pruneRate=%.2f%%%n",
                    symmetryBreakingChecks, symmetryBreakingPrunes, pruneRate);
        }

        System.out.println("=".repeat(60));

        // 内层DDO调用统计
        System.out.printf("内层DDO调用: %d 次 (缓存命中: %d, 未命中: %d)%n",
                innerDdoCallCount, cacheHitCount, cacheMissCount);

        // 总剪枝效果
        long totalPrunes = capacityCutPrunes +
                (useInfeasibilityCache ? infeasibilityCache.getPruneCount() : 0) +
                (useBoundPropagation ? boundPropagation.getPruneCount() : 0) +
                (useSymmetryBreaking ? symmetryBreakingPrunes : 0);
        System.out.printf("总剪枝次数: %d 次 (容量割平面: %d, InfeasibilityCache: %d, BoundPropagation: %d, SymmetryBreaking: %d)%n",
                totalPrunes, capacityCutPrunes,
                useInfeasibilityCache ? infeasibilityCache.getPruneCount() : 0,
                useBoundPropagation ? boundPropagation.getPruneCount() : 0,
                useSymmetryBreaking ? symmetryBreakingPrunes : 0);

        System.out.println("=".repeat(60));
    }

    /**
     * 记录进度信息（定期打印）
     *
     * @param state 当前状态
     * @param var 当前变量索引
     */
    private void logProgress(NestedSALBPState state, int var) {
        if (domainCallCount == 1) {
            startTime = System.currentTimeMillis();
            System.out.printf("[DEBUG-INIT] 首次domain调用: var=%d, state=%s%n", var, state);
            System.out.printf("[DEBUG-INIT] remaining=%d, currentStation=%s, hasRobot=%s, remainingRobots=%d%n",
                    state.getRemainingTasks(nbTasks).size(),
                    state.currentStationTasks(),
                    state.currentStationHasRobot(),
                    state.remainingRobots(totalRobots));
        }

        long now = System.currentTimeMillis();
        if (now - lastLogTime > 5000) {
            double elapsed = (now - startTime) / 1000.0;
            System.out.printf("[DEBUG] Elapsed: %.1fs | domain=%d, innerDDO=%d (hit=%d, miss=%d), var=%d, remaining=%d%n",
                    elapsed, domainCallCount, innerDdoCallCount, cacheHitCount, cacheMissCount, var,
                    state.getRemainingTasks(nbTasks).size());
            lastLogTime = now;
        }
    }

    /**
     * 记录决策生成信息（首次调用时打印详细信息）
     *
     * @param state 当前状态
     * @param decisions 生成的决策列表
     * @param tasksRequiringRobot 需要机器人的任务数
     * @param remainingRobots 剩余机器人数
     * @param robotsAreScarce 机器人是否紧缺
     */
    private void logDecisions(NestedSALBPState state, List<Integer> decisions,
                              int tasksRequiringRobot, int remainingRobots, boolean robotsAreScarce) {
        if (domainCallCount == 1) {
            System.out.printf("[DEBUG-INIT] 机器人预留: tasksRequiringRobot=%d, remainingRobots=%d, robotsAreScarce=%s%n",
                    tasksRequiringRobot, remainingRobots, robotsAreScarce);
            System.out.printf("[DEBUG-INIT] 生成的决策数: %d, decisions=%s%n", decisions.size(), decisions);
        }

        if (decisions.isEmpty() && !state.isComplete(nbTasks)) {
            Set<Integer> remaining = state.getRemainingTasks(nbTasks);
            Set<Integer> currentStationTasks = state.currentStationTasks();
            System.out.printf("[WARNING] domain() 返回空决策！remaining=%d, currentStation=%s, hasRobot=%s, remainingRobots=%d%n",
                    remaining.size(), currentStationTasks, state.currentStationHasRobot(), remainingRobots);
            for (int task : remaining) {
                boolean eligible = isTaskEligible(task, remaining, currentStationTasks);
                boolean needsRobot = taskRequiresRobot(task);
                System.out.printf("  Task %d: eligible=%s, requiresRobot=%s, humanDur=%d%n",
                        task + 1, eligible, needsRobot, innerProblem.humanDurations[task]);
            }
        }
    }
}
