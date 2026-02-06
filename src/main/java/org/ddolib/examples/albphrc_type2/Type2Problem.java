package org.ddolib.examples.albphrc_type2;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;
import org.ddolib.examples.ssalbrb.SSALBRBProblem;

import java.io.IOException;
import java.util.*;

/**
 * 二型 ALBP-HRC 问题定义
 * 
 * 目标：给定工位数 m 和机器人数 Q，最小化节拍时间 C
 * 
 * 决策编码：decision = task * 6 + action
 * - action = 0: 加入当前工位，Human 模式
 * - action = 1: 加入当前工位，Robot 模式
 * - action = 2: 加入当前工位，Collaboration 模式
 * - action = 3: 开新工位（无机器人），Human 模式
 * - action = 4: 开新工位（有机器人），Robot 模式
 * - action = 5: 开新工位（有机器人），Collaboration 模式
 * 
 * 代价函数：edgeCost = max(newLoad, maxLoadSoFar) - maxLoadSoFar
 * 路径总代价 = maxLoadSoFar = 最小节拍时间
 */
public class Type2Problem implements Problem<Type2State> {
    
    public final int nbTasks;
    public final int maxStations;        // 给定的工位数上限 m
    public final int totalRobots;        // 可用机器人总数 Q
    
    private final Map<Integer, List<Integer>> predecessors;
    private final Map<Integer, List<Integer>> successors;
    
    // 任务的三种执行时间
    public final int[] humanDurations;
    public final int[] robotDurations;
    public final int[] collaborationDurations;
    
    // 执行模式常量
    public static final int MODE_HUMAN = 0;
    public static final int MODE_ROBOT = 1;
    public static final int MODE_COLLABORATION = 2;
    
    // 动作常量（决策编码）
    public static final int ACTION_ADD_HUMAN = 0;           // 加入当前工位，Human
    public static final int ACTION_ADD_ROBOT = 1;           // 加入当前工位，Robot
    public static final int ACTION_ADD_COLLAB = 2;          // 加入当前工位，Collaboration
    public static final int ACTION_NEW_HUMAN = 3;           // 开新工位（无机器人），Human
    public static final int ACTION_NEW_ROBOT = 4;           // 开新工位（有机器人），Robot
    public static final int ACTION_NEW_COLLAB = 5;          // 开新工位（有机器人），Collaboration
    
    // 统计信息
    private static long domainCallCount = 0;
    private static long lastLogTime = System.currentTimeMillis();
    
    public Type2Problem(String dataFile, int maxStations, int totalRobots) throws IOException {
        SSALBRBProblem innerProblem = new SSALBRBProblem(dataFile);
        this.nbTasks = innerProblem.nbTasks;
        this.maxStations = maxStations;
        this.totalRobots = totalRobots;
        this.predecessors = innerProblem.predecessors;
        this.successors = innerProblem.successors;
        this.humanDurations = innerProblem.humanDurations;
        this.robotDurations = innerProblem.robotDurations;
        this.collaborationDurations = innerProblem.collaborationDurations;
        
        System.out.println("\n=== 二型 ALBP-HRC 问题 ===");
        System.out.printf("任务数: %d, 最大工位数: %d, 机器人数: %d%n", 
                         nbTasks, maxStations, totalRobots);
        System.out.println();
    }
    
    @Override
    public Type2State initialState() {
        // 初始状态：没有完成任何任务，还未开始第一个工位
        return new Type2State(
            Collections.emptySet(),  // completedTasks
            0,                       // stationIndex (还未开始)
            0,                       // currentStationLoad
            false,                   // currentStationHasRobot (占位)
            0,                       // usedRobots
            0                        // maxLoadSoFar
        );
    }
    
    @Override
    public Iterator<Integer> domain(Type2State state, int var) {
        domainCallCount++;
        
        long now = System.currentTimeMillis();
        if (domainCallCount == 1 || now - lastLogTime > 5000) {
            System.out.printf("[Type2] domain=%d, completed=%d/%d, station=%d/%d, maxLoad=%d%n",
                             domainCallCount, state.completedTasks().size(), nbTasks,
                             state.stationIndex(), maxStations, state.maxLoadSoFar());
            lastLogTime = now;
        }
        
        if (state.isComplete(nbTasks)) {
            return Collections.emptyIterator();
        }
        
        // 检查工位数约束
        if (state.stationIndex() > maxStations) {
            return Collections.emptyIterator();
        }
        
        List<Integer> decisions = new ArrayList<>();
        Set<Integer> remaining = state.getRemainingTasks(nbTasks);
        Set<Integer> completed = state.completedTasks();
        
        // 计算剩余机器人数
        int remainingRobots = state.remainingRobots(totalRobots);
        
        // 计算需要机器人的剩余任务数
        int criticalTasksCount = 0;
        for (int t : remaining) {
            if (isCriticalTask(t)) {
                criticalTasksCount++;
            }
        }
        
        boolean robotsAreScarce = (remainingRobots <= criticalTasksCount);
        
        // 枚举 eligible 任务
        for (int task : remaining) {
            if (!isTaskEligible(task, remaining, completed)) {
                continue;
            }
            
            boolean isCritical = isCriticalTask(task);
            
            // 情况1：当前工位还未开始（stationIndex == 0）
            if (state.stationIndex() == 0) {
                // 必须开启第一个工位
                if (isCritical) {
                    // 关键任务必须有机器人
                    if (remainingRobots > 0) {
                        decisions.add(task * 6 + ACTION_NEW_ROBOT);
                        decisions.add(task * 6 + ACTION_NEW_COLLAB);
                    }
                } else {
                    // 非关键任务
                    decisions.add(task * 6 + ACTION_NEW_HUMAN);
                    if (remainingRobots > 0 && !robotsAreScarce) {
                        decisions.add(task * 6 + ACTION_NEW_ROBOT);
                        decisions.add(task * 6 + ACTION_NEW_COLLAB);
                    }
                }
                continue;
            }
            
            // 情况2：当前工位已开启
            boolean currentHasRobot = state.currentStationHasRobot();
            
            // 选项A：加入当前工位
            for (int mode = 0; mode < 3; mode++) {
                if (!isModeAvailable(mode, currentHasRobot, isCritical)) {
                    continue;
                }
                decisions.add(task * 6 + mode);  // ACTION_ADD_*
            }
            
            // 选项B：开新工位（如果还有工位可用）
            if (state.stationIndex() < maxStations) {
                if (isCritical) {
                    // 关键任务必须有机器人
                    if (remainingRobots > 0) {
                        decisions.add(task * 6 + ACTION_NEW_ROBOT);
                        decisions.add(task * 6 + ACTION_NEW_COLLAB);
                    }
                } else {
                    // 非关键任务
                    decisions.add(task * 6 + ACTION_NEW_HUMAN);
                    if (remainingRobots > 0 && !robotsAreScarce) {
                        decisions.add(task * 6 + ACTION_NEW_ROBOT);
                        decisions.add(task * 6 + ACTION_NEW_COLLAB);
                    }
                }
            }
        }
        
        return decisions.iterator();
    }
    
    /**
     * 检查任务是否 eligible（前继都已完成）
     */
    private boolean isTaskEligible(int task, Set<Integer> remaining, Set<Integer> completed) {
        for (int pred : predecessors.get(task)) {
            if (remaining.contains(pred)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查任务是否为关键任务（所有模式的时间都很长，需要特殊处理）
     * 这里简化为：如果 humanDuration 明显大于其他模式，则认为需要机器人
     */
    private boolean isCriticalTask(int task) {
        int tH = humanDurations[task];
        int tR = robotDurations[task];
        int tC = collaborationDurations[task];
        // 如果人工时间是机器人/协作时间的2倍以上，认为是关键任务
        return tH > 2 * Math.min(tR, tC);
    }
    
    /**
     * 检查模式是否可用
     */
    private boolean isModeAvailable(int mode, boolean hasRobot, boolean isCritical) {
        if (mode == MODE_HUMAN) {
            return !isCritical;  // 关键任务不能只用人工
        }
        if (mode == MODE_ROBOT || mode == MODE_COLLABORATION) {
            return hasRobot;  // 需要机器人
        }
        return true;
    }
    
    /**
     * 获取任务在指定模式下的执行时间
     */
    private int getDuration(int task, int mode) {
        switch (mode) {
            case MODE_HUMAN: return humanDurations[task];
            case MODE_ROBOT: return robotDurations[task];
            case MODE_COLLABORATION: return collaborationDurations[task];
            default: throw new IllegalArgumentException("Invalid mode: " + mode);
        }
    }
    
    @Override
    public Type2State transition(Type2State state, Decision decision) {
        int decisionVal = decision.val();
        int task = decisionVal / 6;
        int action = decisionVal % 6;
        
        // 解码动作
        boolean openNewStation = (action >= ACTION_NEW_HUMAN);
        int mode;
        if (action == ACTION_ADD_HUMAN || action == ACTION_NEW_HUMAN) {
            mode = MODE_HUMAN;
        } else if (action == ACTION_ADD_ROBOT || action == ACTION_NEW_ROBOT) {
            mode = MODE_ROBOT;
        } else {
            mode = MODE_COLLABORATION;
        }
        
        int duration = getDuration(task, mode);
        
        // 情况1：还未开始第一个工位
        if (state.stationIndex() == 0) {
            boolean needsRobot = (mode == MODE_ROBOT || mode == MODE_COLLABORATION);
            return new Type2State(
                Set.of(task),
                1,                    // 开启第一个工位
                duration,
                needsRobot,
                0,
                duration              // maxLoadSoFar = 第一个任务的时间
            );
        }
        
        // 情况2：加入当前工位
        if (!openNewStation) {
            Set<Integer> newCompleted = new LinkedHashSet<>(state.completedTasks());
            newCompleted.add(task);
            
            int newLoad = state.currentStationLoad() + duration;
            int newMaxLoad = Math.max(state.maxLoadSoFar(), newLoad);
            
            return new Type2State(
                newCompleted,
                state.stationIndex(),
                newLoad,
                state.currentStationHasRobot(),
                state.usedRobots(),
                newMaxLoad
            );
        }
        
        // 情况3：开新工位
        Set<Integer> newCompleted = new LinkedHashSet<>(state.completedTasks());
        newCompleted.add(task);
        
        int newUsedRobots = state.usedRobots();
        if (state.currentStationHasRobot()) {
            newUsedRobots++;
        }
        
        boolean needsRobot = (mode == MODE_ROBOT || mode == MODE_COLLABORATION);
        int newMaxLoad = Math.max(state.maxLoadSoFar(), duration);
        
        return new Type2State(
            newCompleted,
            state.stationIndex() + 1,
            duration,
            needsRobot,
            newUsedRobots,
            newMaxLoad
        );
    }
    
    @Override
    public double transitionCost(Type2State state, Decision decision) {
        // 关键：代价 = newMaxLoad - oldMaxLoad
        Type2State nextState = transition(state, decision);
        return nextState.maxLoadSoFar() - state.maxLoadSoFar();
    }
    
    @Override
    public double initialValue() {
        return 0.0;  // 初始状态的 maxLoadSoFar = 0
    }
    
    @Override
    public int nbVars() {
        return nbTasks;
    }
    
    @Override
    public Optional<Double> optimalValue() {
        return Optional.empty();  // 二型问题通常没有已知最优值
    }
    
    @Override
    public double evaluate(int[] solution) throws org.ddolib.modeling.InvalidSolutionException {
        Type2State state = initialState();
        
        for (int i = 0; i < solution.length; i++) {
            int decisionVal = solution[i];
            state = transition(state, new Decision(i, decisionVal));
        }
        
        // 最终的 maxLoadSoFar 就是节拍时间
        // 但需要考虑最后一个工位
        int finalMaxLoad = state.maxLoadSoFar();
        if (state.stationIndex() > 0) {
            finalMaxLoad = Math.max(finalMaxLoad, state.currentStationLoad());
        }
        
        return finalMaxLoad;
    }
}
