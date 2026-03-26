package org.ddolib.examples.ssalbrb;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.File;
import java.io.IOException;

import java.util.*;

public class SSALBRBProblem implements Problem<SSALBRBState> {

    record TransitionInfo(int task,
                          int mode,
                          int startTime,
                          int completionTime,
                          SSALBRBState nextState) {}

    public static final int MODE_HUMAN = 0;
    public static final int MODE_ROBOT = 1;
    public static final int MODE_COLLABORATION = 2;

    public final int nbTasks;
    public final int[] humanDurations;
    public final int[] robotDurations;
    public final int[] collaborationDurations;
    public final Map<Integer, List<Integer>> successors;
    public final Map<Integer, List<Integer>> predecessors;
    private final Optional<Double> optimal;
    public final int cycleTime; // Maximum allowed makespan (Integer.MAX_VALUE means no constraint)

    public SSALBRBProblem(int nbTasks,
                          int[] humanDurations,
                          int[] robotDurations,
                          int[] collaborationDurations,
                          Map<Integer, List<Integer>> successors,
                          Optional<Double> optimal) {
        this(nbTasks, humanDurations, robotDurations, collaborationDurations, successors, optimal, Integer.MAX_VALUE);
    }

    public SSALBRBProblem(int nbTasks,
                          int[] humanDurations,
                          int[] robotDurations,
                          int[] collaborationDurations,
                          Map<Integer, List<Integer>> successors,
                          Optional<Double> optimal,
                          int cycleTime) {
        this.nbTasks = nbTasks;
        this.humanDurations = humanDurations;
        this.robotDurations = robotDurations;
        this.collaborationDurations = collaborationDurations;
        this.cycleTime = cycleTime;
        this.successors = new HashMap<>();

        for (int task = 0; task < nbTasks; task++) {
            this.successors.put(task, new ArrayList<>(successors.getOrDefault(task, List.of())));
        }

        this.predecessors = buildPredecessors(this.successors, nbTasks);
        this.optimal = optimal;
    }

    public SSALBRBProblem(int nbTasks,
                          int[] humanDurations,
                          int[] robotDurations,
                          int[] collaborationDurations,
                          Map<Integer, List<Integer>> successors) {
        this(nbTasks, humanDurations, robotDurations, collaborationDurations, successors, Integer.MAX_VALUE);
    }

    public SSALBRBProblem(int nbTasks,
                          int[] humanDurations,
                          int[] robotDurations,
                          int[] collaborationDurations,
                          Map<Integer, List<Integer>> successors,
                          int cycleTime) {
        this.nbTasks = nbTasks;
        this.humanDurations = humanDurations;
        this.robotDurations = robotDurations;
        this.collaborationDurations = collaborationDurations;
        this.cycleTime = cycleTime;
        this.successors = new HashMap<>();

        for (int task = 0; task < nbTasks; task++) {
            this.successors.put(task, new ArrayList<>(successors.getOrDefault(task, List.of())));
        }

        this.predecessors = buildPredecessors(this.successors, nbTasks);
        this.optimal = Optional.empty();
    }


    /**
     * 从文件读取数据（支持CSV和ALB两种格式）
     * - CSV格式：task,th,tr,tc,successor
     * - ALB格式：传统的装配线平衡问题格式
     */
    public SSALBRBProblem(final String file) throws IOException {
        // 检查文件扩展名，决定使用哪种读取方式
        DataHolder data;
        if (file.endsWith(".csv")) {
            // 读取CSV格式文件
            data = readFromCSV(file);
        } else {
            // 读取原来的.alb格式文件
            data = readFromALB(file);
        }

        // 初始化final字段（只能在构造函数中初始化一次）
        this.nbTasks = data.nbTasks;
        this.humanDurations = data.humanDurations;
        this.robotDurations = data.robotDurations;
        this.collaborationDurations = data.collaborationDurations;
        this.successors = data.successors;
        this.predecessors = buildPredecessors(this.successors, this.nbTasks);
        this.optimal = data.optimal;
        this.cycleTime = Integer.MAX_VALUE;
    }

    /**
     * 临时数据持有类，用于从文件读取数据后返回
     * 因为final字段只能在构造函数中初始化一次，所以需要这个辅助类
     */
    private static class DataHolder {
        int nbTasks;
        int[] humanDurations;
        int[] robotDurations;
        int[] collaborationDurations;
        Map<Integer, List<Integer>> successors;
        Optional<Double> optimal;
    }

    /**
     * 从CSV文件读取数据
     * CSV格式：task,th,tr,tc,successor
     * 例如：1,132,264,100000,[]
     */
    private static DataHolder readFromCSV(String file) throws IOException {
        try (Scanner scanner = new Scanner(new File(file))) {
            // 跳过表头
            scanner.nextLine();

            // 先读取所有数据到临时列表
            List<int[]> taskData = new ArrayList<>();
            List<String> successorStrings = new ArrayList<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                // 解析CSV行：task,th,tr,tc,successor
                String[] parts = line.split(",", 5);  // 最多分割成5部分
                if (parts.length < 5) {
                    continue;
                }

                int taskId = Integer.parseInt(parts[0].trim());
                int th = Integer.parseInt(parts[1].trim());
                int tr = Integer.parseInt(parts[2].trim());
                int tc = Integer.parseInt(parts[3].trim());
                String successorStr = parts[4].trim();

                taskData.add(new int[]{taskId, th, tr, tc});
                successorStrings.add(successorStr);
            }

            // 初始化数据结构
            int n = taskData.size();
            int[] hDurations = new int[n];
            int[] rDurations = new int[n];
            int[] cDurations = new int[n];
            Map<Integer, List<Integer>> succ = new HashMap<>();

            for (int i = 0; i < n; i++) {
                succ.put(i, new ArrayList<>());
            }

            // 填充时间数据
            for (int i = 0; i < n; i++) {
                int[] data = taskData.get(i);
                int taskId = data[0];
                int taskIndex = taskId - 1;  // 任务ID从1开始，索引从0开始

                hDurations[taskIndex] = data[1];
                rDurations[taskIndex] = data[2];
                cDurations[taskIndex] = data[3];
            }

            // 解析后继关系
            for (int i = 0; i < n; i++) {
                int[] data = taskData.get(i);
                int taskId = data[0];
                int taskIndex = taskId - 1;

                String successorStr = successorStrings.get(i);
                // 解析后继列表，格式如：[] 或 [5, 7, 8, 9] 或 "[5, 7, 8, 9]"
                successorStr = successorStr.replace("\"", "").trim();

                if (!successorStr.equals("[]")) {
                    // 移除方括号
                    successorStr = successorStr.substring(1, successorStr.length() - 1);
                    String[] successorIds = successorStr.split(",");

                    for (String succId : successorIds) {
                        succId = succId.trim();
                        if (!succId.isEmpty()) {
                            int successorTaskId = Integer.parseInt(succId);
                            int successorIndex = successorTaskId - 1;
                            succ.get(taskIndex).add(successorIndex);
                        }
                    }
                }
            }

            // 创建并返回数据持有对象
            DataHolder holder = new DataHolder();
            holder.nbTasks = n;
            holder.humanDurations = hDurations;
            holder.robotDurations = rDurations;
            holder.collaborationDurations = cDurations;
            holder.successors = succ;
            holder.optimal = Optional.empty();  // CSV文件中没有最优解信息

            return holder;
        }
    }

    /**
     * 从ALB格式文件读取数据
     * ALB格式是传统的装配线平衡问题格式
     */
    private static DataHolder readFromALB(String file) throws IOException {
        try (Scanner scanner = new Scanner(new File(file))) {
            scanner.nextLine();
            int n = scanner.nextInt();
            scanner.nextLine();
            scanner.nextLine();

            scanner.nextLine();
            scanner.nextInt();
            scanner.nextLine();
            scanner.nextLine();

            scanner.nextLine();
            scanner.nextLine();
            scanner.nextLine();
            scanner.nextLine();
            scanner.nextLine();  // Extra empty line before <task times>

            int[] hDurations = new int[n];
            int[] rDurations = new int[n];
            int[] cDurations = new int[n];
            Map<Integer, List<Integer>> succ = new HashMap<>();
            for (int i = 0; i < n; i++) {
                succ.put(i, new ArrayList<>());
            }

            // <task times> header already read, start reading task data directly
            for (int i = 0; i < n; i++) {
                String[] line = scanner.nextLine().trim().split("\\s+");
                int baseTime = Integer.parseInt(line[1]);
                hDurations[i] = baseTime;
                rDurations[i] = (int) Math.round(baseTime * 2.0);
                cDurations[i] = (int) Math.round(baseTime * 0.7);
            }

            scanner.nextLine();
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    break;
                }

                String[] parts = line.split(",");
                if (parts.length == 2) {
                    int a = Integer.parseInt(parts[0].trim()) - 1;
                    int b = Integer.parseInt(parts[1].trim()) - 1;
                    succ.get(a).add(b);
                }
            }

            Optional<Double> optimalValue = Optional.empty();
            if (scanner.hasNextLine()) {
                String li = scanner.nextLine().trim();
                if (!li.equals("<end>") && scanner.hasNextLine()) {
                    String lin = scanner.nextLine().trim();
                    if (!lin.isEmpty()) {
                        double optimal = Integer.parseInt(lin);
                        optimalValue = Optional.of(optimal);
                    }
                }
            }

            // 创建并返回数据持有对象
            DataHolder holder = new DataHolder();
            holder.nbTasks = n;
            holder.humanDurations = hDurations;
            holder.robotDurations = rDurations;
            holder.collaborationDurations = cDurations;
            holder.successors = succ;
            holder.optimal = optimalValue;

            return holder;
        }
    }

    private static Map<Integer, List<Integer>> buildPredecessors(Map<Integer, List<Integer>> successors,
                                                                 int nbTasks) {
        Map<Integer, List<Integer>> preds = new HashMap<>();
        for (int task = 0; task < nbTasks; task++) {
            preds.put(task, new ArrayList<>());
        }
        for (Map.Entry<Integer, List<Integer>> entry : successors.entrySet()) {
            int task = entry.getKey();
            for (int successor : entry.getValue()) {
                preds.get(successor).add(task);
            }
        }
        return preds;
    }

    @Override
    public int nbVars() {
        return nbTasks;
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public SSALBRBState initialState() {
        // All tasks start with E_t = 0 (unassigned with earliest start time 0)
        List<Integer> earliestStartTimes = new ArrayList<>(Collections.nCopies(nbTasks, 0));
        return new SSALBRBState(0, 0, earliestStartTimes);
    }

    @Override
    public Iterator<Integer> domain(SSALBRBState state, int var) {
        List<Integer> domain = new ArrayList<>();

        // Check all tasks for eligibility
        for (int task = 0; task < nbTasks; task++) {
            if (isEligible(task, state)) {
                // For each eligible task, only add modes whose completion does not exceed cycleTime
                int taskEarliestStart = state.earliestStartTimes().get(task);

                int humanStart = Math.max(state.humanAvailable(), taskEarliestStart);
                if (humanStart + humanDurations[task] <= cycleTime) {
                    domain.add(task * 3 + MODE_HUMAN);
                }

                int robotStart = Math.max(state.robotAvailable(), taskEarliestStart);
                if (robotStart + robotDurations[task] <= cycleTime) {
                    domain.add(task * 3 + MODE_ROBOT);
                }

                int collabStart = Math.max(Math.max(state.humanAvailable(), state.robotAvailable()), taskEarliestStart);
                if (collabStart + collaborationDurations[task] <= cycleTime) {
                    domain.add(task * 3 + MODE_COLLABORATION);
                }
            }
        }
        return domain.iterator();
    }

    /**
     * A task is eligible if E_t >= 0 (unassigned) and all predecessors have E_u < 0 (assigned).
     */
    private boolean isEligible(int task, SSALBRBState state) {
        // Check if task is unassigned
        if (!state.isUnassigned(task)) {
            return false;
        }
        // Check if all predecessors are assigned
        for (int predecessor : predecessors.get(task)) {
            if (state.isUnassigned(predecessor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public SSALBRBState transition(SSALBRBState state, Decision decision) {
        return simulateTransition(state, decision.val()).nextState();
    }

    @Override
    public double transitionCost(SSALBRBState state, Decision decision) {
        TransitionInfo info = simulateTransition(state, decision.val());
        return info.nextState().makespan() - state.makespan();
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(
                    "Solution length " + solution.length + " does not match number of variables " + nbVars());
        }
        // Reconstruct the schedule from the solution and return the final makespan
        SSALBRBState state = initialState();
        for (int decisionVal : solution) {
            state = simulateTransition(state, decisionVal).nextState();
        }
        return state.makespan();
    }

    public TransitionInfo simulateTransition(SSALBRBState state, int decisionVal) {
        int task = decisionVal / 3;
        int mode = decisionVal % 3;

        int humanReady = state.humanAvailable();
        int robotReady = state.robotAvailable();

        // Get E_t for the task (earliest start time from precedence constraints)
        int taskEarliestStart = state.earliestStartTimes().get(task);
        if (taskEarliestStart < 0) {
            throw new IllegalStateException("Task " + task + " is already assigned");
        }

        // Compute actual start time based on mode
        int startTime;

        if (mode == MODE_HUMAN) {
            startTime = Math.max(humanReady, taskEarliestStart);
        } else if (mode == MODE_ROBOT) {
            startTime = Math.max(robotReady, taskEarliestStart);
        } else if (mode == MODE_COLLABORATION) {
            startTime = Math.max(Math.max(humanReady, robotReady), taskEarliestStart);
        } else {
            throw new IllegalArgumentException("Unsupported mode: " + mode);
        }

        int processingTime = switch (mode) {
            case MODE_HUMAN -> humanDurations[task];
            case MODE_ROBOT -> robotDurations[task];
            case MODE_COLLABORATION -> collaborationDurations[task];
            default -> throw new IllegalStateException("Unexpected mode: " + mode);
        };

        int completion = startTime + processingTime;

        // Update resource availability
        int nextHumanReady;
        int nextRobotReady;
        if (mode == MODE_HUMAN) {
            nextHumanReady = completion;
            nextRobotReady = robotReady;
        } else if (mode == MODE_ROBOT) {
            nextHumanReady = humanReady;
            nextRobotReady = completion;
        } else {
            nextHumanReady = completion;
            nextRobotReady = completion;
        }

        // Update E vector:
        // 1. Mark task as assigned: E_t' = -C_t
        // 2. Update successors' earliest start times: E_u' = max(E_u, C_t) if (t,u) in P
        List<Integer> updatedE = new ArrayList<>(state.earliestStartTimes());
        updatedE.set(task, -completion);  // Mark as assigned

        // Update all successors
        for (int successor : successors.get(task)) {
            int currentE = updatedE.get(successor);
            if (currentE >= 0) {  // Only update if successor is still unassigned
                updatedE.set(successor, Math.max(currentE, completion));
            }
        }

        SSALBRBState nextState = new SSALBRBState(nextHumanReady, nextRobotReady, updatedE);

        return new TransitionInfo(task, mode, startTime, completion, nextState);
    }

    @Override
    public String toString() {return nbTasks + ", " + Arrays.toString(humanDurations) + ", " + Arrays.toString(robotDurations) + ", " + Arrays.toString(collaborationDurations) + optimal.get();}
}
