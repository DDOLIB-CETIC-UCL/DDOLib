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
 * Nested Dynamic Programming Problem: Type I Assembly Line Balancing + Human-Robot Collaborative Scheduling
 *
 * ============================================================================
 * Problem Description:
 * ============================================================================
 * Outer DDO: Determines task-to-station assignment, objective is to minimize number of stations
 * Inner DDO: For each station performs human-robot collaborative scheduling, objective is to minimize makespan (ensuring not exceeding cycle time)
 *
 * ============================================================================
 * Core Design Principles:
 * ============================================================================
 * 1. [Robot Reservation Strategy]
 *    - Identify bottleneck tasks: tasks with manual time > cycle time must use robot
 *    - Reservation mechanism: When remaining robots <= tasks needing robots, prioritize robot reservation for bottleneck tasks
 *    - Purpose: Ensure problem feasibility, avoid bottleneck tasks unable to complete
 *
 * 2. [Feasibility Check]
 *    - Problem level: Check if any task exceeds cycle time even in fastest mode
 *    - Station level: Check if task set in station can complete within cycle time
 *    - Precedence constraints: Ensure task assignment satisfies precedence relationships
 *
 * 3. [Lazy Evaluation]
 *    - Optimistic estimation: Use sumMinDurations in domain() for quick feasibility check
 *    - Exact computation: Call inner DDO in transition() for precise verification
 *    - Caching mechanism: Cache makespan and solution sequences to avoid recomputation
 *
 * 4. [Optimization Strategies] (Configurable switches)
 *    - Capacity Cut: Prune infeasible stations based on minimum times
 *    - Infeasibility Cache: Record known infeasible task subsets
 *    - Bound Propagation: Use known optimal solution for pruning
 *    - Symmetry Breaking: Force stations sorted by first task number
 *
 * ============================================================================
 * Decision Encoding:
 * ============================================================================
 * decision = task * 2 + robotFlag
 * - task: Index of task to assign (0 to nbTasks-1)
 * - robotFlag: 0 = Join current station or open new station without robot
 *              1 = Open new station and assign robot
 *
 * ============================================================================
 * State Representation:
 * ============================================================================
 * NestedSALBPState contains:
 * - completedTasks: Set of completed tasks (tasks in closed stations)
 * - currentStationTasks: Set of tasks in current station
 * - currentStationHasRobot: Whether current station has robot
 * - usedRobots: Number of robots used (robots in closed stations)
 */
public class NestedSALBPProblem implements Problem<NestedSALBPState> {

    // ==================== Problem Parameters ====================
    public final int nbTasks;
    public final int cycleTime;
    public final int totalRobots;
    private final Map<Integer, List<Integer>> predecessors;
    private final Map<Integer, List<Integer>> successors;

    // ==================== Inner Problem ====================
    public final SSALBRBProblem innerProblem;

    // ==================== Cache System ====================
    /**
     * Makespan cache: task subset -> (has robot -> makespan)
     * Purpose: Avoid repeated calls to inner DDO solver for same station makespan computation
     */
    private final Map<Set<Integer>, Map<Boolean, Integer>> makespanCache;

    /**
     * Solution sequence cache: task subset -> (has robot -> solution sequence)
     * Purpose: Cache complete solution from inner DDO for detailed scheduling plan printing
     */
    private final Map<Set<Integer>, Map<Boolean, int[]>> solutionCache;

    // ==================== Optimization Components ====================
    /**
     * Infeasibility cache: Records known infeasible task subsets
     * Principle: If task set S is infeasible, then any superset containing S is also infeasible
     * Effect: Fast pruning, avoid checking known infeasible subsets repeatedly
     */
    private final InfeasibilityCache infeasibilityCache;

    /**
     * Bound propagation: Use known optimal solution for pruning
     * Principle: If current state's lower bound >= known upper bound, can prune
     * Effect: Reduce search space, terminate unpromising branches early
     */
    private final BoundPropagation boundPropagation;

    /** Switch: Enable infeasibility cache */
    private final boolean useInfeasibilityCache;

    /** Switch: Enable capacity cut (fast pruning based on minimum time sum) */
    private final boolean useCapacityCut;

    /** Switch: Enable bound propagation */
    private final boolean useBoundPropagation;

    /** Switch: Enable symmetry breaking (force stations sorted by first task number) */
    private final boolean useSymmetryBreaking;

    // ==================== Debug Statistics ====================

    /** Statistics: Total calls to domain() method */
    private static long domainCallCount = 0;
    /** Statistics: Total calls to inner DDO solver */
    private static long innerDdoCallCount = 0;
    /** Statistics: Cache hits (successfully retrieved from cache) */
    private static long cacheHitCount = 0;
    /** Statistics: Cache misses (need recomputation) */
    private static long cacheMissCount = 0;
    /** Timestamp of last log print */
    private static long lastLogTime = System.currentTimeMillis();
    /** Solve start timestamp */
    private static long startTime = System.currentTimeMillis();
    /** Capacity cut pruning statistics: check count */
    private long capacityCutChecks = 0;
    /** Capacity cut pruning statistics: successful prune count */
    private long capacityCutPrunes = 0;
    /** Symmetry breaking statistics: check count */
    private long symmetryBreakingChecks = 0;
    /** Symmetry breaking statistics: successful prune count */
    private long symmetryBreakingPrunes = 0;

    // ==================== Constructors ====================

    /**
     * Constructor (using default optimization configuration)
     *
     * @param dataFile Data file path
     * @param cycleTime Cycle time
     * @param totalRobots Total available robots
     * @throws IOException File read exception
     */
    public NestedSALBPProblem(String dataFile, int cycleTime, int totalRobots) throws IOException {
        this(dataFile, cycleTime, totalRobots, true, true, true, true);  // Enable all optimizations by default
    }

    /**
     * Constructor (full configuration)
     *
     * @param dataFile Data file path
     * @param cycleTime Cycle time
     * @param totalRobots Total available robots
     * @param useInfeasibilityCache Whether to enable infeasibility cache
     * @param useCapacityCut Whether to enable capacity cut
     * @param useBoundPropagation Whether to enable bound propagation
     * @param useSymmetryBreaking Whether to enable symmetry breaking
     * @throws IOException File read exception
     */
    public NestedSALBPProblem(String dataFile, int cycleTime, int totalRobots,
                              boolean useInfeasibilityCache, boolean useCapacityCut,
                              boolean useBoundPropagation, boolean useSymmetryBreaking) throws IOException {
        // Initialize inner problem
        this.innerProblem = new SSALBRBProblem(dataFile);
        this.nbTasks = innerProblem.nbTasks;
        this.cycleTime = cycleTime;
        this.totalRobots = totalRobots;
        this.predecessors = innerProblem.predecessors;
        this.successors = innerProblem.successors;

        // Initialize caches
        this.makespanCache = new HashMap<>();
        this.solutionCache = new HashMap<>();

        // Initialize optimization components and switches
        this.useInfeasibilityCache = useInfeasibilityCache;
        this.useCapacityCut = useCapacityCut;
        this.useBoundPropagation = useBoundPropagation;
        this.useSymmetryBreaking = useSymmetryBreaking;
        this.infeasibilityCache = new InfeasibilityCache();
        this.boundPropagation = new BoundPropagation();

        // Print optimization configuration
        System.out.println("=== Optimization Configuration ===");
        System.out.println("Capacity Cut: " + (useCapacityCut ? "Enabled ✓" : "Disabled ✗"));
        System.out.println("InfeasibilityCache: " + (useInfeasibilityCache ? "Enabled ✓" : "Disabled ✗"));
        System.out.println("BoundPropagation: " + (useBoundPropagation ? "Enabled ✓" : "Disabled ✗"));
        System.out.println("SymmetryBreaking: " + (useSymmetryBreaking ? "Enabled ✓" : "Disabled ✗"));
        System.out.println();

        // Problem feasibility check: check if any task exceeds cycle time even in fastest mode
        checkProblemFeasibility();

        // Print robot requirement analysis
        printRobotRequirementAnalysis();
    }

    // ==================== Problem Feasibility Check ====================
    /**
     * Check problem feasibility: whether any task exceeds cycle time even in fastest mode
     *
     * Check logic:
     * - For each task, compute minimum processing time among three modes (manual, robot, cooperative)
     * - If minimum time > cycle time, the task cannot be completed in any station
     * - If so, problem is infeasible, throw exception immediately
     *
     * @throws IllegalArgumentException if problem is infeasible
     */
    private void checkProblemFeasibility() {
        for (int task = 0; task < nbTasks; task++) {
            int minDuration = getMinDuration(task);
            if (minDuration > cycleTime) {
                throw new IllegalArgumentException(
                        String.format("Problem infeasible: Task %d's minimum processing time %d exceeds cycle time %d. " +
                                        "(Human=%d, Robot=%d, Collab=%d)",
                                task + 1, minDuration, cycleTime,
                                innerProblem.humanDurations[task],
                                innerProblem.robotDurations[task],
                                innerProblem.collaborationDurations[task]));
            }
        }
    }

    /**
     * Get task's minimum processing time (smallest among three modes)
     *
     * @param task Task index
     * @return Minimum processing time
     */
    private int getMinDuration(int task) {
        return Math.min(innerProblem.humanDurations[task],
                Math.min(innerProblem.robotDurations[task],
                        innerProblem.collaborationDurations[task]));
    }

    /**
     * [Core of robot reservation strategy] Check if task must use robot
     *
     * Judgment criterion: human time > cycle time
     *
     * Principle:
     * - If human time > cycleTime, this task cannot complete alone in station without robot
     * - Such tasks called "bottleneck tasks", must assign to station with robot
     * - This is foundation of robot reservation strategy: when robots scarce, prioritize reserving for these tasks
     *
     * Note: This method is key to robot reservation strategy, used extensively in domain()
     *
     * @param task Task index
     * @return true if task must use robot
     */
    public boolean taskRequiresRobot(int task) {
        return innerProblem.humanDurations[task] > cycleTime;
    }

    /**
     * Print robot requirement analysis
     *
     * Purpose: Help understand problem characteristics, judge if robot resources are sufficient
     */
    private void printRobotRequirementAnalysis() {
        System.out.println("\n=== Task Analysis ===");
        int robotRequiredCount = 0;
        for (int task = 0; task < nbTasks; task++) {
            if (taskRequiresRobot(task)) {
                robotRequiredCount++;
                System.out.printf("Task %d must use robot: humanDur=%d > cycleTime=%d, minDur=%d%n",
                        task + 1, innerProblem.humanDurations[task], cycleTime,
                        getMinDuration(task));
            }
        }
        System.out.printf("Total %d tasks must use robot, available robots: %d%n", robotRequiredCount, totalRobots);
        System.out.println();
    }

    // ==================== Problem Interface Implementation ====================

    /**
     * Return initial state
     *
     * Initial state characteristics:
     * - No tasks completed
     * - Current station is empty
     * - maybeCompletedTasks is empty (only produced during Relax merge)
     * - No robots used
     */
    @Override
    public NestedSALBPState initialState() {
        return new NestedSALBPState(
                Collections.emptySet(),         // completedTasks: empty
                Collections.emptySet(),         // currentStationTasks: empty
                Collections.emptySet(),         // maybeCompletedTasks: empty
                false,                          // currentStationHasRobot: false
                0);                             // usedRobots: 0
    }

    /**
     * Return number of variables (equals number of tasks)
     */
    @Override
    public int nbVars() {
        return nbTasks;
    }

    /**
     * Return initial value (no stations in initial state)
     */
    @Override
    public double initialValue() {
        return 0;  // Initial state has no stations
    }

    /**
     * Return optimal value (if known)
     */
    @Override
    public Optional<Double> optimalValue() {
        return innerProblem.optimalValue();
    }

    // ==================== Decision Domain Generation (Core Logic) ====================

    /**
     * Generate decision domain - this is the core method of entire algorithm
     *
     * ============================================================================
     * Decision encoding: decision = task * 2 + robotFlag
     * ============================================================================
     * - task: Index of task to assign (0 to nbTasks-1)
     * - robotFlag: 0 = Join current station or open new station without robot
     *              1 = Open new station and assign robot
     *
     * ============================================================================
     * Core strategies:
     * ============================================================================
     *
     * 1. [Robot Reservation Strategy]
     *    - Calculate how many remaining tasks must use robots (bottleneck tasks)
     *    - When remaining robots <= tasks needing robots:
     *      * robotsCriticallyShort: truly not enough, only process tasks needing robots
     *      * robotsAreScarce: just enough, limit non-essential tasks from using robots
     *    - Purpose: Ensure bottleneck tasks get robots, avoid infeasibility
     *
     * 2. [Precedence Constraints]
     *    - Only consider eligible tasks (all predecessors completed or in current station)
     *    - Ensure task assignment satisfies precedence relationships
     *
     * 3. [Lazy Evaluation]
     *    - Use optimistic estimation (sumMinDurations) for quick feasibility check
     *    - Reduce inner DDO calls, improve efficiency
     *
     * 4. [Bound Propagation Pruning]
     *    - Calculate current state lower bound (completed stations + lower bound of unfinished tasks)
     *    - If lower bound >= known upper bound, prune (generate no decisions)
     *
     * 5. [Symmetry Breaking]
     *    - Force stations sorted by "number of first task"
     *    - Avoid exploring symmetric station arrangements (e.g., [(3,4), (1,2)] and [(1,2), (3,4)])
     *
     * @param state Current state
     * @param var Current variable index (unused, reserved for interface compatibility)
     * @return Iterator of feasible decisions
     */
    @Override
    public Iterator<Integer> domain(NestedSALBPState state, int var) {
        domainCallCount++;
        logProgress(state, var);

        // If all tasks completed, return empty decisions
        if (state.isComplete(nbTasks)) {
            return Collections.emptyIterator();
        }

        // ========== Optimization strategy 1: Bound propagation pruning ==========
        if (useBoundPropagation) {
            // Calculate completed stations (conservative estimate)
            int completedStations = computeCompletedStations(state);

            // Calculate lower bound of unfinished tasks (current station + remaining tasks)
            // Key: calculate current station tasks and remaining tasks together, avoid overestimation
            double unfinishedStationsLB = computeLowerBoundForUnfinishedTasks(state);

            // Total lower bound = completed stations + unfinished tasks' lower bound
            double totalLowerBound = completedStations + unfinishedStationsLB;

//            // Safety margin: since DDO state merging may cause overestimation, use safety margin
//            double safetyMargin = 1.0;  // Base safety margin
//
//            // If current station not empty, increase margin (state info may be inaccurate)
//            if (!state.currentStationTasks().isEmpty()) {
//                safetyMargin += 0.5;
//            }
//
//            // If robot usage high, increase margin (robot info may be lost during merge)
//            double robotUsageRatio = (double) state.usedRobots() / totalRobots;
//            if (robotUsageRatio > 0.7) {
//                safetyMargin += 0.5;
//            }

            // Use calculated lower bound for pruning
            double relaxedLowerBound = totalLowerBound;
//            double relaxedLowerBound = totalLowerBound - safetyMargin;

            // Check if can prune
            if (boundPropagation.canPrune(0, relaxedLowerBound)) {
                return Collections.emptyIterator();  // Prune: current branch unpromising
            }
        }

        // ========== Prepare decision generation ==========
        List<Integer> decisions = new ArrayList<>();
        Set<Integer> remaining = state.getRemainingTasks(nbTasks);
        Set<Integer> currentStationTasks = state.currentStationTasks();
        int remainingRobots = state.remainingRobots(totalRobots);

        // ========== Robot reservation strategy: calculate tasks requiring robots ==========
        int tasksRequiringRobot = 0;
        for (int t : remaining) {
            if (taskRequiresRobot(t)) {
                tasksRequiringRobot++;
            }
        }

        // Determine if robots are scarce
        boolean robotsCriticallyShort = (remainingRobots < tasksRequiringRobot);  // truly not enough
        boolean robotsAreScarce = (remainingRobots <= tasksRequiringRobot);       // just enough

        // ========== Classify eligible tasks ==========
        List<Integer> eligibleRobotTasks = new ArrayList<>();    // Tasks requiring robot
        List<Integer> eligibleNormalTasks = new ArrayList<>();   // Tasks not requiring robot
        for (int task : remaining) {
            if (isTaskEligible(task, remaining, currentStationTasks)) {
                if (taskRequiresRobot(task)) {
                    eligibleRobotTasks.add(task);
                } else {
                    eligibleNormalTasks.add(task);
                }
            }
        }

        // ========== Decide which tasks to process ==========
        List<Integer> tasksToProcess;
        if (!eligibleRobotTasks.isEmpty() && robotsCriticallyShort) {
            // Robots truly not enough, only process tasks needing robots
            tasksToProcess = eligibleRobotTasks;
        } else {
            // Process all eligible tasks
            tasksToProcess = new ArrayList<>();
            tasksToProcess.addAll(eligibleRobotTasks);
            tasksToProcess.addAll(eligibleNormalTasks);
        }

        // ========== Generate decisions ==========
        for (int task : tasksToProcess) {
            boolean requiresRobot = taskRequiresRobot(task);

            if (currentStationTasks.isEmpty()) {
                // Case 1: New station (current station empty)
                generateDecisionsForNewStation(decisions, task, requiresRobot, remainingRobots, robotsAreScarce);
            } else {
                // Case 2: Current station opened, check if can continue adding
                generateDecisionsForExistingStation(decisions, task, requiresRobot,
                        state, remainingRobots, robotsAreScarce, currentStationTasks);
            }
        }

        // ========== Optimization strategy 2: Symmetry breaking ==========
        if (useSymmetryBreaking) {
            applySymmetryBreaking(state, decisions, remaining, currentStationTasks);
        }

        // Print debug info
        logDecisions(state, decisions, tasksRequiringRobot, remainingRobots, robotsAreScarce);

        return decisions.iterator();
    }

    /**
     * Apply symmetry breaking constraint
     *
     * ============================================================================
     * Rule: Unassigned task with smallest number must assign to next new station
     * ============================================================================
     *
     * Principle:
     * - Force stations sorted by "number of first task"
     * - Avoid exploring symmetric station arrangements
     *
     * Example:
     * - If task 1 still unassigned, then task 1 must be in next new station
     * - This avoids situations like [(3,4), (1,2)] (since 1 must assign first)
     * - Only keep arrangement like [(1,2), (3,4)]
     *
     * Implementation:
     * - Find smallest unassigned and eligible task
     * - Remove "open new station" decisions for other tasks (robotFlag=1)
     * - Keep all decisions for smallest task + "join current station" decisions for others
     *
     * @param state Current state
     * @param decisions Decision list (will be modified)
     * @param remaining Remaining task set
     * @param currentStationTasks Current station task set
     */
    private void applySymmetryBreaking(NestedSALBPState state, List<Integer> decisions,
                                       Set<Integer> remaining, Set<Integer> currentStationTasks) {
        // Only apply when opening new station (current station empty and not first station)
        if (!currentStationTasks.isEmpty() || state.completedTasks().isEmpty()) {
            return;
        }

        symmetryBreakingChecks++;

        // Find smallest unassigned and eligible task
        int minRemainingTask = remaining.stream()
                .filter(t -> isTaskEligible(t, remaining, currentStationTasks))
                .min(Integer::compareTo)
                .orElse(-1);

        if (minRemainingTask == -1) {
            return;  // No eligible tasks
        }

        // Count decisions before pruning
        int beforeSize = decisions.size();

        // Only allow smallest task to open new station
        // Remove "open new station" decisions for other tasks
        decisions.removeIf(d -> {
            int task = d / 2;
            int robotFlag = d % 2;
            // Keep: all decisions for smallest task + "join current station" decisions for others
            // Remove: "open new station" decisions for other tasks
            return task != minRemainingTask && robotFlag == 1;
        });

        // Count pruned decisions
        int afterSize = decisions.size();
        if (afterSize < beforeSize) {
            symmetryBreakingPrunes += (beforeSize - afterSize);
        }
    }

    // ==================== Decision Generation Helper Methods ====================

    /**
     * Generate decisions for new station
     *
     * Scenario: Current station empty, need to open new station
     *
     * Decision logic:
     * 1. If task must use robot (requiresRobot=true):
     *    - Only generate "new station + assign robot" decision (robotFlag=1)
     *    - Prerequisite: still have remaining robots
     *
     * 2. If task not forced to need robot (requiresRobot=false):
     *    - If robots not scarce: generate both "new station + assign robot" and "new station + no robot" decisions
     *    - If robots scarce: only generate "new station + no robot" decision (reserve robots for bottleneck tasks)
     *
     * @param decisions Decision list (will be modified)
     * @param task Task index
     * @param requiresRobot Whether task must use robot
     * @param remainingRobots Number of remaining robots
     * @param robotsAreScarce Whether robots are scarce
     */
    private void generateDecisionsForNewStation(List<Integer> decisions, int task,
                                                boolean requiresRobot, int remainingRobots, boolean robotsAreScarce) {
        if (requiresRobot) {
            // Task must use robot
            if (remainingRobots > 0) {
                decisions.add(task * 2 + 1);  // New station, must assign robot
            }
        } else {
            // Task not forced to need robot
            if (remainingRobots > 0 && !robotsAreScarce) {
                decisions.add(task * 2 + 1);  // New station, assign robot (option when robots sufficient)
            }
            decisions.add(task * 2 + 0);  // New station, no robot
        }
    }

    /**
     * Generate decisions for existing station
     *
     * Scenario: Current station already has tasks, check if can continue adding
     *
     * Decision logic:
     * 1. If task must use robot but current station has no robot:
     *    - Must open new station and assign robot
     *
     * 2. Use optimistic estimation to check if can join current station:
     *    - Feasible: generate "join current station" decision (robotFlag=0)
     *    - Infeasible: need to open new station
     *      * Use Maximum Load Rule to check if current station can add more tasks
     *      * If station not full, only allow "join current station" (let transition() do exact check)
     *      * If station full, allow "open new station"
     *
     * @param decisions Decision list (will be modified)
     * @param task Task index
     * @param requiresRobot Whether task must use robot
     * @param state Current state
     * @param remainingRobots Number of remaining robots
     * @param robotsAreScarce Whether robots are scarce
     * @param currentStationTasks Current station task set
     */
    private void generateDecisionsForExistingStation(List<Integer> decisions, int task,
                                                     boolean requiresRobot, NestedSALBPState state, int remainingRobots,
                                                     boolean robotsAreScarce, Set<Integer> currentStationTasks) {

        // If task must use robot but current station has no robot, must open new station
        if (requiresRobot && !state.currentStationHasRobot()) {
            if (remainingRobots > 0) {
                decisions.add(task * 2 + 1);  // New station, must assign robot
            }
            return;
        }

        // Build test task set (current station + new task)
        Set<Integer> testTasks = new LinkedHashSet<>(currentStationTasks);
        testTasks.add(task);

        // Use optimistic estimation (reduce inner DDO calls)
        if (isStationFeasibleOptimistic(testTasks, state.currentStationHasRobot())) {
            decisions.add(task * 2 + 0);  // Join current station
        } else {
            // Optimistic estimation says infeasible, need to open new station

            // Maximum Load Rule: check if can add more tasks to current station
            boolean canAddMore = canAddMoreTasksToCurrentStation(state);

            if (requiresRobot) {
                // Only open new station if current really full
                if (!canAddMore && remainingRobots > 0) {
                    decisions.add(task * 2 + 1);  // New station, must assign robot
                }
                // If current station has robot, still try to join (let transition() do exact check)
                if (state.currentStationHasRobot()) {
                    decisions.add(task * 2 + 0);  // Try joining current station with robot
                }
            } else {
                // Only open new station if current really full
                if (!canAddMore) {
                    if (remainingRobots > 0 && !robotsAreScarce) {
                        decisions.add(task * 2 + 1);  // New station, assign robot
                    }
                    decisions.add(task * 2 + 0);  // Open station without robot
                } else {
                    // Station not full, only allow joining current
                    decisions.add(task * 2 + 0);  // Try joining current station
                }
            }
        }
    }

    /**
     * Check if task is eligible (all predecessors either completed or in current station)
     *
     * Precedence constraint: task can only be assigned after all predecessor tasks assigned
     *
     * @param task Task index
     * @param remaining Remaining task set
     * @param currentStationTasks Current station task set
     * @return true if task eligible
     */
    private boolean isTaskEligible(int task, Set<Integer> remaining, Set<Integer> currentStationTasks) {
        for (int pred : predecessors.get(task)) {
            if (remaining.contains(pred) && !currentStationTasks.contains(pred)) {
                return false;  // Exist unfinished predecessor not in current station
            }
        }
        return true;
    }

    /**
     * Maximum Load Rule: Check if current station can add more tasks
     *
     * ============================================================================
     * Core idea: If current station still has remaining capacity and tasks can be added,
     *            should not close current station to open new one.
     * ============================================================================
     *
     * Purpose:
     * - Avoid closing station too early, causing low utilization
     * - Ensure fitting as many tasks as possible before closing station
     *
     * Check logic:
     * 1. Iterate all remaining tasks
     * 2. Check precedence constraints (whether eligible)
     * 3. Check capacity constraints (use optimistic estimation, avoid calling inner DDO)
     * 4. If exist tasks can be added, return true
     *
     * @param state Current state
     * @return true if more tasks can be added to current station
     */
    private boolean canAddMoreTasksToCurrentStation(NestedSALBPState state) {
        if (state.currentStationTasks().isEmpty()) {
            return false; // Current station empty, not applicable
        }

        Set<Integer> remaining = state.getRemainingTasks(nbTasks);
        Set<Integer> currentTasks = state.currentStationTasks();

        // Check if there are eligible tasks can be added
        for (int task : remaining) {
            // Check precedence constraints
            if (!isTaskEligible(task, remaining, currentTasks)) {
                continue;
            }

            // Check capacity constraints (use optimistic estimation, avoid calling inner DDO)
            Set<Integer> testTasks = new HashSet<>(currentTasks);
            testTasks.add(task);

            if (isStationFeasibleOptimistic(testTasks, state.currentStationHasRobot())) {
                return true; // Exist tasks that can be added
            }
        }

        return false; // No tasks can be added, station full
    }

    // ==================== State Transition ====================

    /**
     * State transition function: generate new state based on decision
     *
     * Decision decoding:
     * - task = decision / 2: task to assign
     * - robotFlag = decision % 2: robot flag (0 or 1)
     *
     * Transition logic:
     * 1. Check if need to open new station (through exact feasibility check)
     * 2. If not need new station: add task to current station
     * 3. If need new station:
     *    - Close current station (move tasks to completedTasks, update usedRobots)
     *    - Open new station (put new task in new station, assign robot if needed)
     *
     * Key: Handle maybeCompletedTasks
     * - If assigned task is in maybeCompletedTasks, remove it
     * - maybeCompletedTasks unchanged in other cases
     *
     * @param state Current state
     * @param decision Decision
     * @return New state
     */
    @Override
    public NestedSALBPState transition(NestedSALBPState state, Decision decision) {
        int decisionVal = decision.val();
        int task = decisionVal / 2;
        int robotFlag = decisionVal % 2;
        boolean assignRobot = (robotFlag == 1);

        // Key: Check if task is in maybeCompletedTasks
        // This only happens when continuing transition from relax state
        boolean taskIsMaybeCompleted = state.maybeCompletedTasks().contains(task);

        // Check if need to open new station (use exact feasibility check)
        boolean needNewStation = false;

        if (state.currentStationTasks().isEmpty()) {
            // Current station empty, must open new station
            needNewStation = true;
        } else {
            // Current station not empty, check if can join
            Set<Integer> testTasks = new LinkedHashSet<>(state.currentStationTasks());
            testTasks.add(task);
            needNewStation = !isStationFeasible(testTasks, state.currentStationHasRobot());
        }

        if (!needNewStation) {
            // ========== Case 1: Join current station ==========
            Set<Integer> newStationTasks = new LinkedHashSet<>(state.currentStationTasks());
            newStationTasks.add(task);

            // Key: If task originally in maybeCompletedTasks, now it's confirmed in current station
            // So remove from maybeCompletedTasks
            Set<Integer> newMaybeCompleted = new LinkedHashSet<>(state.maybeCompletedTasks());
            if (taskIsMaybeCompleted) {
                newMaybeCompleted.remove(task);
            }

            return new NestedSALBPState(
                    state.completedTasks(),           // Unchanged
                    newStationTasks,                  // Add task
                    newMaybeCompleted,                // May decrease
                    state.currentStationHasRobot(),   // Unchanged
                    state.usedRobots());              // Unchanged
        } else {
            // ========== Case 2: Open new station ==========
            Set<Integer> newCompletedTasks = new LinkedHashSet<>(state.completedTasks());
            Set<Integer> newMaybeCompleted = new LinkedHashSet<>(state.maybeCompletedTasks());
            int newUsedRobots = state.usedRobots();

            // Close current station (if not empty)
            if (!state.currentStationTasks().isEmpty()) {
                // Move current station tasks to completedTasks
                newCompletedTasks.addAll(state.currentStationTasks());
                if (state.currentStationHasRobot()) {
                    newUsedRobots++;
                }
            }

            // Open new station
            Set<Integer> freshStationTasks = new LinkedHashSet<>();
            freshStationTasks.add(task);

            // Key: If task originally in maybeCompletedTasks, now it's confirmed in new station
            // So remove from maybeCompletedTasks
            if (taskIsMaybeCompleted) {
                newMaybeCompleted.remove(task);
            }

            // Determine if new station has robot
            boolean newStationHasRobot = assignRobot;
            if (taskRequiresRobot(task)) {
                int availableRobots = totalRobots - newUsedRobots;
                if (availableRobots > 0) {
                    newStationHasRobot = true;
                }
            }

            return new NestedSALBPState(
                    newCompletedTasks,                // Add current station tasks
                    freshStationTasks,                // New station has one task
                    newMaybeCompleted,                // May decrease
                    newStationHasRobot,
                    newUsedRobots);
        }
    }

    /**
     * Calculate transition cost (cost of opening new station is 1, joining current station is 0)
     *
     * Key modification: Handle maybeCompletedTasks
     * - If task is in maybeCompletedTasks, means it may already be in some station
     * - Cost should be 0 (optimistic estimation: assume it's already assigned)
     *
     * @param state Current state
     * @param decision Decision
     * @return Transition cost
     */
    @Override
    public double transitionCost(NestedSALBPState state, Decision decision) {
        int decisionVal = decision.val();
        int task = decisionVal / 2;

        // Key: If task in maybeCompletedTasks, cost is optimistically 0
        // Because it may already be in some station (in certain merged paths)
        if (state.maybeCompletedTasks().contains(task)) {
            return 0.0;
        }

        if (state.currentStationTasks().isEmpty()) {
            return 1.0;  // Open first station
        }

        Set<Integer> testTasks = new LinkedHashSet<>(state.currentStationTasks());
        testTasks.add(task);

        if (isStationFeasible(testTasks, state.currentStationHasRobot())) {
            return 0.0;  // Join current station, no station count increase
        } else {
            return 1.0;  // Open new station, station count +1
        }
    }

    /**
     * Evaluate objective value (station count) of complete solution
     *
     * Purpose: Verify solution correctness, calculate final objective value
     *
     * @param solution Complete solution (decision sequence)
     * @return Number of stations
     * @throws org.ddolib.modeling.InvalidSolutionException if solution is invalid
     */
    @Override
    public double evaluate(int[] solution) throws org.ddolib.modeling.InvalidSolutionException {
        NestedSALBPState state = initialState();
        int stationCount = 0;

        // Simulate entire decision sequence
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
                // Join current station
                Set<Integer> newStationTasks = new LinkedHashSet<>(state.currentStationTasks());
                newStationTasks.add(task);

                state = new NestedSALBPState(
                        state.completedTasks(),
                        newStationTasks,
                        state.maybeCompletedTasks(),  // Keep unchanged
                        state.currentStationHasRobot(),
                        state.usedRobots());
            } else {
                // Open new station
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
                        state.maybeCompletedTasks(),  // Keep unchanged
                        assignRobot,
                        newUsedRobots);
            }
        }

        // Last station
        if (!state.currentStationTasks().isEmpty()) {
            stationCount++;
        }
        return stationCount;
    }

    // ==================== Station Feasibility Check ====================

    /**
     * Optimistic version: for fast judgment when generating decisions in domain()
     *
     * Purpose: Reduce inner DDO calls, improve efficiency
     *
     * Judgment logic:
     * 1. If station has no robot:
     *    - Check if exist tasks that must use robot → infeasible
     *    - Check if human time sum exceeds cycle time → exceeds then infeasible
     *
     * 2. If station has robot:
     *    - Use sumMinDurations (minimum time sum) for optimistic estimation
     *    - If minimum time sum <= cycle time → possibly feasible (optimistic)
     *
     * Note: This is optimistic estimation, may misjudge as feasible (but not as infeasible)
     *
     * @param tasks Task set
     * @param hasRobot Whether has robot
     * @return true if optimistic estimation considers feasible
     */
    private boolean isStationFeasibleOptimistic(Set<Integer> tasks, boolean hasRobot) {
        // If station has no robot but tasks must use robot, infeasible
        if (!hasRobot) {
            for (int task : tasks) {
                if (taskRequiresRobot(task)) {
                    return false;
                }
            }
            return sumHumanDurations(tasks) <= cycleTime;
        }

        // Has robot: use sumMinDur for optimistic estimation
        return sumMinDurations(tasks) <= cycleTime;
    }

    /**
     * Exact version: for exact judgment in transition() and evaluate()
     *
     * Purpose: Ensure correctness of state transition, avoid generating infeasible states
     *
     * ============================================================================
     * Optimization strategy execution order (from fast to slow):
     * ============================================================================
     * 1. Capacity Cut - Check if minimum time sum exceeds capacity
     * 2. Infeasibility Cache - Check if contains known infeasible subsets
     * 3. Makespan cache - Query cached makespan
     * 4. Inner DDO - Exact calculation (slowest, call last)
     *
     * ============================================================================
     * Judgment logic:
     * ============================================================================
     * 1. [Capacity Cut] If has robot, check if minimum time sum exceeds theoretical capacity
     *    - Theoretical capacity = 1.5 × cycleTime (theoretical upper limit of human-robot parallel work)
     *    - If minimum time sum > theoretical capacity → infeasible
     *
     * 2. [Infeasibility Cache] Check if contains known infeasible subsets
     *    - If contains → infeasible
     *
     * 3. [Fast Check] If station has no robot:
     *    - Check if exist tasks that must use robot → infeasible
     *    - Check if human time sum exceeds cycle time → exceeds then infeasible
     *
     * 4. [Optimistic Estimation] If human time sum <= cycle time → feasible
     *
     * 5. [Exact Calculation] Call inner DDO to calculate makespan
     *    - If makespan <= cycle time → feasible
     *    - Otherwise → infeasible, record to infeasibility cache
     *
     * @param tasks Task set
     * @param hasRobot Whether has robot
     * @return true if exact judgment considers feasible
     */
    private boolean isStationFeasible(Set<Integer> tasks, boolean hasRobot) {
        // ========== Optimization 1: Capacity Cut (fastest check) ==========
        // Note: Capacity cut only valid for cases with robot
        // Because without robot, subsequent sumHumanDurations check is more exact
        if (useCapacityCut && hasRobot) {
            capacityCutChecks++;  // Record check count

            // Check if minimum time sum exceeds capacity
            // With robot, human and robot can work in parallel, maximum capacity achievable is 1.5 × cycleTime
            // (parallel and cooperative times are 2/3 and 0.7 respectively)
            int minTotalDuration = sumMinDurations(tasks);
            int capacity = (int) Math.round(1.5 * cycleTime);

            if (minTotalDuration > capacity) {
                capacityCutPrunes++;  // Record prune count
                return false; // Capacity cut pruning
            }
        }

        // ========== Optimization 2: Infeasibility subset pruning ==========
        if (useInfeasibilityCache && hasRobot && infeasibilityCache.containsInfeasibleSubset(tasks)) {
            return false; // Infeasibility subset pruning
        }

        // ========== Original feasibility check ==========
        // If station has no robot but tasks must use robot, infeasible
        if (!hasRobot) {
            for (int task : tasks) {
                if (taskRequiresRobot(task)) {
                    return false;
                }
            }
        }

        int sumHuman = sumHumanDurations(tasks);

        if (sumHuman <= cycleTime) {
            return true;  // Optimistic case: human time sum not exceeding
        }

        if (!hasRobot) {
            return false;  // No robot and sumHuman > cycleTime
        }

        // ========== Optimization 3: Query makespan cache, Optimization 4: Call inner DDO ==========
        // Has robot: need exact calculation
        int makespan = computeStationMakespan(tasks, true);
        boolean feasible = makespan <= cycleTime;

        // ========== Record infeasible subset ==========
        if (useInfeasibilityCache && !feasible) {
            infeasibilityCache.recordInfeasible(tasks, true);
        }

        return feasible;
    }

    /**
     * Calculate sum of human durations (optimistic lower bound)
     *
     * Purpose: Quick estimate of station load
     *
     * @param tasks Task set
     * @return Sum of human durations
     */
    private int sumHumanDurations(Set<Integer> tasks) {
        int sum = 0;
        for (int task : tasks) {
            sum += innerProblem.humanDurations[task];
        }
        return sum;
    }

    /**
     * Calculate sum of minDur (more optimistic lower bound)
     *
     * Purpose: Optimistic estimation, for fast feasibility judgment
     *
     * @param tasks Task set
     * @return Sum of minimum durations
     */
    private int sumMinDurations(Set<Integer> tasks) {
        int sum = 0;
        for (int task : tasks) {
            sum += getMinDuration(task);
        }
        return sum;
    }

    // ==================== Inner DDO Solving ====================

    /**
     * Call inner model to calculate single-station makespan
     *
     * Optimization: Cache both makespan and solution sequence simultaneously, avoid repeated DDO solver calls
     *
     * Workflow:
     * 1. Query makespan cache, if hit return directly
     * 2. If no robot: directly calculate human time sum (no need to call DDO)
     * 3. If has robot: call inner DDO solver
     *    - Create sub-problem (only include specified tasks)
     *    - Call DDO solver to get optimal scheduling scheme
     *    - Cache makespan and solution sequence simultaneously
     *
     * @param tasks Task set
     * @param hasRobot Whether has robot
     * @return makespan (if infeasible return Integer.MAX_VALUE)
     */
    public int computeStationMakespan(Set<Integer> tasks, boolean hasRobot) {
        // Query makespan cache
        if (makespanCache.containsKey(tasks) && makespanCache.get(tasks).containsKey(hasRobot)) {
            cacheHitCount++;
            return makespanCache.get(tasks).get(hasRobot);
        }
        cacheMissCount++;

        int makespan;

        if (!hasRobot) {
            // Optimization: Without robot, all tasks can only use human mode, directly calculate total time
            makespan = 0;
            for (int task : tasks) {
                makespan += innerProblem.humanDurations[task];
            }
            // No need to cache solution sequence for no-robot case (order doesn't matter)
        } else {
            // Has robot: need to call inner DDO solver
            innerDdoCallCount++;
            SSALBRBProblem subProblem = createSubProblem(tasks, true);

            // Call unified solving method, get both makespan and solution sequence
            int[] solution = solveDDOForSolution(subProblem);

            if (solution != null && solution.length > 0) {
                try {
                    makespan = (int) subProblem.evaluate(solution);
                    // Cache solution sequence simultaneously for printing use
                    solutionCache.computeIfAbsent(tasks, k -> new HashMap<>()).put(hasRobot, solution);
                } catch (Exception e) {
                    makespan = Integer.MAX_VALUE;
                }
            } else {
                makespan = Integer.MAX_VALUE;
            }
        }

        // Cache makespan result
        makespanCache.computeIfAbsent(tasks, k -> new HashMap<>()).put(hasRobot, makespan);

        return makespan;
    }

    /**
     * Create sub-problem: only include specified tasks
     *
     * Workflow:
     * 1. Convert task set to list (establish index mapping)
     * 2. Extract sub-problem task time data
     * 3. Build sub-problem precedence relationships (map to new indices)
     * 4. If no robot, disable robot and cooperation modes (set to huge value)
     *
     * @param tasks Task set
     * @param hasRobot Whether has robot
     * @return Sub-problem instance
     */
    private SSALBRBProblem createSubProblem(Set<Integer> tasks, boolean hasRobot) {
        List<Integer> taskList = new ArrayList<>(tasks);
        int subNbTasks = taskList.size();

        // Extract sub-problem task time data
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
                // No robot: disable robot and cooperation modes
                subRDurations[i] = cycleTime * 10000;
                subCDurations[i] = cycleTime * 10000;
            }
        }

        // Build sub-problem precedence relationships
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
     * Use DDO solver to solve single-station scheduling problem, return decision sequence
     *
     * This is unified solving method, used for both computing makespan and getting detailed solution
     *
     * Configuration:
     * - Relaxation: SSALBRBRelax (relaxation merge strategy)
     * - Ranking: SSALBRBRanking (state ranking strategy)
     * - FastLowerBound: SSALBRBFastLowerBound (fast lower bound)
     * - Width: FixedWidth(10) (fixed width 10)
     *
     * @param problem Inner problem instance
     * @return Solution sequence (decision array)
     */
    private int[] solveDDOForSolution(SSALBRBProblem problem) {
        // Build inner DDO model
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

        // Call DDO solver
        Solution solution = Solvers.minimizeDdo(innerModel, (sol, searchStats) -> {
            if (sol != null && sol.length > 0) {
                resultSolution[0] = sol;
            }
        });

        return resultSolution[0];
    }

    // ==================== Inner Problem Solution Details ====================

    /**
     * Solution of inner problem (including tasks and operation modes)
     *
     * Purpose: Represent detailed scheduling scheme of single station
     */
    public static class InnerSolution {
        /** Task sequence (original indices) */
        public final int[] tasks;

        /** Corresponding operation modes (0=Human, 1=Robot, 2=Collaboration) */
        public final int[] modes;

        public InnerSolution(int[] tasks, int[] modes) {
            this.tasks = tasks;
            this.modes = modes;
        }
    }

    /**
     * Solve inner problem and return task scheduling order and operation modes
     *
     * Optimization: Prioritize using cached solution sequence, avoid repeated DDO solver calls
     *
     * Workflow:
     * 1. Query solution sequence cache
     * 2. If cache miss, call DDO solver and cache result
     * 3. Decode sub-problem decision values and map back to original indices
     *
     * @param stationTasks Station task set
     * @param hasRobot Whether has robot
     * @return Inner solution (including task sequence and operation modes)
     */
    public InnerSolution solveInnerProblemWithModes(Set<Integer> stationTasks, boolean hasRobot) {
        List<Integer> taskList = new ArrayList<>(stationTasks);

        // First query solution sequence cache
        int[] subSolution = null;
        if (solutionCache.containsKey(stationTasks) &&
                solutionCache.get(stationTasks).containsKey(hasRobot)) {
            subSolution = solutionCache.get(stationTasks).get(hasRobot);
        } else {
            // Cache miss, call DDO solver
            SSALBRBProblem subProblem = createSubProblem(stationTasks, hasRobot);
            subSolution = solveDDOForSolution(subProblem);

            // Cache solution sequence
            if (subSolution != null && subSolution.length > 0) {
                solutionCache.computeIfAbsent(stationTasks, k -> new HashMap<>())
                        .put(hasRobot, subSolution);
            }
        }

        if (subSolution == null || subSolution.length == 0) {
            return null;
        }

        // Decode sub-problem decision values and map back to original indices
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
     * Solve inner problem and return task scheduling order (without operation modes)
     *
     * @param stationTasks Station task set
     * @param hasRobot Whether has robot
     * @return Task sequence
     */
    public int[] solveInnerProblem(Set<Integer> stationTasks, boolean hasRobot) {
        InnerSolution solution = solveInnerProblemWithModes(stationTasks, hasRobot);
        return solution != null ? solution.tasks : null;
    }

    // ==================== Debug and Logging ====================

    /**
     * Print cache statistics
     *
     * Including:
     * - Number of cache entries
     * - Cache hit rate
     * - Number of inner DDO calls
     * - Estimated memory usage
     */
    public void printCacheStatistics() {
        System.out.println("\n=== Cache Statistics ===");
        System.out.printf("Makespan cache entries: %d%n", makespanCache.size());
        System.out.printf("Solution cache entries: %d%n", solutionCache.size());
        System.out.printf("Cache hits: %d%n", cacheHitCount);
        System.out.printf("Cache misses: %d%n", cacheMissCount);
        System.out.printf("Inner DDO calls: %d%n", innerDdoCallCount);

        // Estimate memory usage
        long makespanMemory = makespanCache.size() * 200;  // bytes per entry
        long solutionMemory = solutionCache.size() * 300;  // bytes per entry (approximate)
        System.out.printf("Estimated memory: makespan=%.2f KB, solution=%.2f KB, total=%.2f KB%n",
                makespanMemory / 1024.0,
                solutionMemory / 1024.0,
                (makespanMemory + solutionMemory) / 1024.0);
    }

    /**
     * Calculate number of completed stations (conservative estimate, ensure it's lower bound)
     *
     * ============================================================================
     * Idea: Use professional algorithms (LB1 and LB2) from FastLowerBound to calculate minimum stations needed for completed tasks
     * ============================================================================
     *
     * Advantages:
     * - Use same lower bound algorithm (LB1 and LB2) as for unfinished tasks
     * - Consider number of robots used
     * - More accurate than simple task count or workload estimation
     *
     * Key: Must ensure it's lower bound (cannot overestimate completed stations)
     * - If overestimate completed stations, total lower bound becomes too large, may incorrectly prune optimal solution
     *
     * @param state Current state
     * @return Lower bound of completed stations
     */
    private int computeCompletedStations(NestedSALBPState state) {
        // If no tasks completed, completed stations = 0
        if (state.completedTasks().isEmpty()) {
            return 0;
        }

        // Use professional algorithm from FastLowerBound to calculate lower bound for completed tasks
        NestedSALBPFastLowerBound lbCalculator = new NestedSALBPFastLowerBound(this);

        // Directly call computeLowerBound method (now public)
        // Parameters: completed task set + robots used
        double completedStationsLB = lbCalculator.computeLowerBound(
                state.completedTasks(),
                state.usedRobots()
        );

        // Round up to ensure integer station count
        return (int) Math.ceil(completedStationsLB);
    }

    /**
     * Calculate lower bound of stations needed for unfinished tasks (current station + remaining tasks) (used for bound propagation)
     *
     * ============================================================================
     * Use professional algorithms (LB1 and LB2) from NestedSALBPFastLowerBound
     * ============================================================================
     * - LB1: Lower bound based on workload balancing
     * - LB2: Improved bin packing problem lower bound
     *
     * ============================================================================
     * Key difference: semantics of FastLowerBound and bound propagation differ
     * ============================================================================
     *
     * Semantics of FastLowerBound.fastLowerBound():
     * - Returns "number of new stations still needed" (incremental)
     * - When current station not empty, returns totalStations - 1 (current station already exists, doesn't count as "new")
     *
     * Semantics of bound propagation:
     * - Need to calculate "total stations needed for unfinished tasks" (including current station)
     * - Current station hasn't been counted in "completed stations" yet
     *
     * Therefore:
     * - If current station empty: use FastLowerBound result directly
     * - If current station not empty: need to add back the 1 subtracted by FastLowerBound
     *
     * @param currentState Current state
     * @return Lower bound of stations needed for unfinished tasks
     */
    private double computeLowerBoundForUnfinishedTasks(NestedSALBPState currentState) {
        Set<Integer> remainingTasks = currentState.getRemainingTasks(nbTasks);
        Set<Integer> currentStationTasks = currentState.currentStationTasks();

        // If no unfinished tasks, return 0
        if (remainingTasks.isEmpty() && currentStationTasks.isEmpty()) {
            return 0;
        }

        // Use professional algorithm from FastLowerBound
        NestedSALBPFastLowerBound lbCalculator = new NestedSALBPFastLowerBound(this);

        // Call fastLowerBound to get "number of new stations still needed"
        double incrementalLB = lbCalculator.fastLowerBound(currentState, remainingTasks);

        // If current station not empty, FastLowerBound already subtracted 1 (considering current station exists)
        // But in bound propagation, current station hasn't been counted in "completed stations" yet
        // So need to add back
        if (!currentStationTasks.isEmpty()) {
            return incrementalLB + 1;
        } else {
            return incrementalLB;
        }
    }

    /**
     * Update optimal solution (used for bound propagation)
     *
     * @param solutionValue Solution objective value (station count)
     */
    public void updateBestSolution(int solutionValue) {
        if (useBoundPropagation) {
            boundPropagation.updateBestSolution(solutionValue);
        }
    }

    /**
     * Print optimization statistics
     *
     * Including:
     * - Enabled status of each optimization strategy
     * - Prune counts and prune rates
     * - Total pruning effect
     */
    public void printOptimizationStatistics() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Optimization Strategy Statistics");
        System.out.println("=".repeat(60));

        // Capacity cut statistics
        System.out.println("CapacityCut: " + (useCapacityCut ? "Enabled ✓" : "Disabled ✗"));
        if (useCapacityCut) {
            double pruneRate = capacityCutChecks > 0 ? (100.0 * capacityCutPrunes / capacityCutChecks) : 0.0;
            System.out.printf("  CapacityCut: checks=%d, prunes=%d, pruneRate=%.2f%%%n",
                    capacityCutChecks, capacityCutPrunes, pruneRate);
        }

        // InfeasibilityCache statistics
        System.out.println("InfeasibilityCache: " + (useInfeasibilityCache ? "Enabled ✓" : "Disabled ✗"));
        if (useInfeasibilityCache) {
            System.out.println("  " + infeasibilityCache.getStatistics());
        }

        // BoundPropagation statistics
        System.out.println("BoundPropagation: " + (useBoundPropagation ? "Enabled ✓" : "Disabled ✗"));
        if (useBoundPropagation) {
            System.out.println("  " + boundPropagation.getStatistics());
        }

        // SymmetryBreaking statistics
        System.out.println("SymmetryBreaking: " + (useSymmetryBreaking ? "Enabled ✓" : "Disabled ✗"));
        if (useSymmetryBreaking) {
            double pruneRate = symmetryBreakingChecks > 0 ?
                    (100.0 * symmetryBreakingPrunes / symmetryBreakingChecks) : 0.0;
            System.out.printf("  SymmetryBreaking: checks=%d, prunes=%d, pruneRate=%.2f%%%n",
                    symmetryBreakingChecks, symmetryBreakingPrunes, pruneRate);
        }

        System.out.println("=".repeat(60));

        // Inner DDO call statistics
        System.out.printf("Inner DDO calls: %d (cache hits: %d, misses: %d)%n",
                innerDdoCallCount, cacheHitCount, cacheMissCount);

        // Total pruning effect
        long totalPrunes = capacityCutPrunes +
                (useInfeasibilityCache ? infeasibilityCache.getPruneCount() : 0) +
                (useBoundPropagation ? boundPropagation.getPruneCount() : 0) +
                (useSymmetryBreaking ? symmetryBreakingPrunes : 0);
        System.out.printf("Total prunes: %d (CapacityCut: %d, InfeasibilityCache: %d, BoundPropagation: %d, SymmetryBreaking: %d)%n",
                totalPrunes, capacityCutPrunes,
                useInfeasibilityCache ? infeasibilityCache.getPruneCount() : 0,
                useBoundPropagation ? boundPropagation.getPruneCount() : 0,
                useSymmetryBreaking ? symmetryBreakingPrunes : 0);

        System.out.println("=".repeat(60));
    }

    /**
     * Log progress information (print periodically)
     *
     * @param state Current state
     * @param var Current variable index
     */
    private void logProgress(NestedSALBPState state, int var) {
        if (domainCallCount == 1) {
            startTime = System.currentTimeMillis();
            System.out.printf("[DEBUG-INIT] First domain call: var=%d, state=%s%n", var, state);
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
     * Log decision generation information (print detailed info on first call)
     *
     * @param state Current state
     * @param decisions Generated decision list
     * @param tasksRequiringRobot Number of tasks requiring robot
     * @param remainingRobots Number of remaining robots
     * @param robotsAreScarce Whether robots are scarce
     */
    private void logDecisions(NestedSALBPState state, List<Integer> decisions,
                              int tasksRequiringRobot, int remainingRobots, boolean robotsAreScarce) {
        if (domainCallCount == 1) {
            System.out.printf("[DEBUG-INIT] Robot reservation: tasksRequiringRobot=%d, remainingRobots=%d, robotsAreScarce=%s%n",
                    tasksRequiringRobot, remainingRobots, robotsAreScarce);
            System.out.printf("[DEBUG-INIT] Generated decisions: %d, decisions=%s%n", decisions.size(), decisions);
        }

        if (decisions.isEmpty() && !state.isComplete(nbTasks)) {
            Set<Integer> remaining = state.getRemainingTasks(nbTasks);
            Set<Integer> currentStationTasks = state.currentStationTasks();
            System.out.printf("[WARNING] domain() returns empty decisions! remaining=%d, currentStation=%s, hasRobot=%s, remainingRobots=%d%n",
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
