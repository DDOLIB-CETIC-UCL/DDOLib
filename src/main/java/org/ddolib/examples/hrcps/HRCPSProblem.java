package org.ddolib.examples.hrcps;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.*;
import org.ddolib.examples.hrc.*;
import org.ddolib.examples.hrcp.*;

import java.io.IOException;
import java.util.*;

/**
 * Nested Dynamic Programming Problem: Type-I Assembly Line Balancing + HRCP Scheduling.
 * <p>
 * <b>Outer DDO</b> assigns tasks to stations, minimising the number of stations.<br>
 * <b>Inner A* solver</b> (HRCP) verifies, for each candidate station, that the
 * optimal makespan does not exceed the cycle time.
 * <p>
 * Every station has both a human worker and a robot – there is no separate
 * robot-allocation decision.
 * <p>
 * <b>Decision encoding:</b> {@code decision = task} (index 0 … n−1).
 * <p>
 * <b>Optimisation strategies (configurable):</b>
 * <ol>
 *   <li>Capacity Cut – prune infeasible stations based on minimum work.</li>
 *   <li>Infeasibility Cache – record known infeasible task subsets.</li>
 *   <li>Bound Propagation – prune using known incumbent solution.</li>
 *   <li>Symmetry Breaking – force stations sorted by first task number.</li>
 * </ol>
 */
public class HRCPSProblem implements Problem<HRCPSState> {

    // ==================== Problem Parameters ====================
    public final int nbTasks;
    public final int cycleTime;

    /** Inner HRCP problem (provides task durations and precedences). */
    public final HRCPProblem innerProblem;

    private final List<Integer>[] predecessors;
    private final List<Integer>[] successors;

    // ==================== Cache System ====================
    private final Map<Set<Integer>, Boolean> feasibilityCache;
    private final Map<Set<Integer>, int[]> solutionCache;

    // ==================== Optimisation Components ====================
    private final InfeasibilityCache infeasibilityCache;
    private final BoundPropagation boundPropagation;

    private final boolean useInfeasibilityCache;
    private final boolean useCapacityCut;
    private final boolean useBoundPropagation;
    private final boolean useSymmetryBreaking;

    // ==================== Statistics ====================
    private long domainCallCount = 0;
    private long innerSolverCallCount = 0;
    private long hrcSolverCallCount = 0;
    private long hrcPrunedCount = 0;
    private long cacheHitCount = 0;
    private long cacheMissCount = 0;
    private long lastLogTime = System.currentTimeMillis();
    private long startTime = System.currentTimeMillis();
    private long capacityCutChecks = 0;
    private long capacityCutPrunes = 0;
    private long symmetryBreakingChecks = 0;
    private long symmetryBreakingPrunes = 0;

    // ==================== Constructors ====================

    public HRCPSProblem(String dataFile, int cycleTime) throws IOException {
        this(dataFile, cycleTime, true, true, true, true);
    }

    @SuppressWarnings("unchecked")
    public HRCPSProblem(String dataFile, int cycleTime,
                        boolean useInfeasibilityCache, boolean useCapacityCut,
                        boolean useBoundPropagation, boolean useSymmetryBreaking) throws IOException {
        this.innerProblem = new HRCPProblem(dataFile);
        this.nbTasks = innerProblem.n;
        this.cycleTime = cycleTime;
        this.predecessors = innerProblem.predecessors;
        this.successors = innerProblem.successors;

        this.feasibilityCache = new HashMap<>();
        this.solutionCache = new HashMap<>();

        this.useInfeasibilityCache = useInfeasibilityCache;
        this.useCapacityCut = useCapacityCut;
        this.useBoundPropagation = useBoundPropagation;
        this.useSymmetryBreaking = useSymmetryBreaking;
        this.infeasibilityCache = new InfeasibilityCache();
        this.boundPropagation = new BoundPropagation();

        System.out.println("=== HRCPS Optimisation Configuration ===");
        System.out.println("Capacity Cut:        " + (useCapacityCut ? "Enabled ✓" : "Disabled ✗"));
        System.out.println("InfeasibilityCache:  " + (useInfeasibilityCache ? "Enabled ✓" : "Disabled ✗"));
        System.out.println("BoundPropagation:    " + (useBoundPropagation ? "Enabled ✓" : "Disabled ✗"));
        System.out.println("SymmetryBreaking:    " + (useSymmetryBreaking ? "Enabled ✓" : "Disabled ✗"));
        System.out.println();

        checkProblemFeasibility();
    }

    // ==================== Problem Feasibility Check ====================

    private void checkProblemFeasibility() {
        for (int task = 0; task < nbTasks; task++) {
            int minDur = getMinDuration(task);
            if (minDur > cycleTime) {
                throw new IllegalArgumentException(
                        String.format("Problem infeasible: Task %d min duration %d > cycle time %d "
                                        + "(Human=%d, Robot=%d, Collab=%d)",
                                task, minDur, cycleTime,
                                innerProblem.humanDurations[task],
                                innerProblem.robotDurations[task],
                                innerProblem.collaborationDurations[task]));
            }
        }
    }

    private int getMinDuration(int task) {
        return Math.min(innerProblem.humanDurations[task],
                Math.min(innerProblem.robotDurations[task],
                        innerProblem.collaborationDurations[task]));
    }

    // ==================== Problem Interface ====================

    @Override
    public HRCPSState initialState() {
        return new HRCPSState(Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
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
    public Optional<Double> optimalValue() {
        return Optional.empty();
    }

    // ==================== Domain Generation ====================

    @Override
    public Iterator<Integer> domain(HRCPSState state, int var) {
        domainCallCount++;
        logProgress(state, var);

        if (state.isComplete(nbTasks)) {
            return Collections.emptyIterator();
        }

        // Bound propagation pruning
        if (useBoundPropagation) {
            double totalLB = computeTotalLowerBound(state);
            if (boundPropagation.canPrune(0, totalLB)) {
                return Collections.emptyIterator();
            }
        }

        Set<Integer> remaining = state.getRemainingTasks(nbTasks);
        Set<Integer> currentStationTasks = state.currentStationTasks();

        // Collect eligible tasks
        List<Integer> eligible = new ArrayList<>();
        for (int task : remaining) {
            if (isTaskEligible(task, remaining, currentStationTasks)) {
                eligible.add(task);
            }
        }

        List<Integer> decisions = new ArrayList<>();

        for (int task : eligible) {
            if (currentStationTasks.isEmpty()) {
                // Opening a new station – always allow
                decisions.add(task);
            } else {
                // Current station open – check if task can join
                Set<Integer> testTasks = new LinkedHashSet<>(currentStationTasks);
                testTasks.add(task);
                if (isStationFeasibleOptimistic(testTasks)) {
                    decisions.add(task);
                } else {
                    // Optimistic says infeasible; check if station is truly full
                    boolean canAddMore = canAddMoreTasksToCurrentStation(state);
                    if (!canAddMore) {
                        decisions.add(task); // will open new station in transition
                    } else {
                        decisions.add(task); // let transition do exact check
                    }
                }
            }
        }

        // Symmetry breaking
        if (useSymmetryBreaking) {
            applySymmetryBreaking(state, decisions, remaining, currentStationTasks);
        }

        logDecisions(state, decisions);
        return decisions.iterator();
    }

    // ==================== Symmetry Breaking ====================

    private void applySymmetryBreaking(HRCPSState state, List<Integer> decisions,
                                       Set<Integer> remaining, Set<Integer> currentStationTasks) {
        if (!currentStationTasks.isEmpty() || state.completedTasks().isEmpty()) {
            return;
        }
        symmetryBreakingChecks++;

        int minTask = remaining.stream()
                .filter(t -> isTaskEligible(t, remaining, currentStationTasks))
                .min(Integer::compareTo)
                .orElse(-1);

        if (minTask == -1) return;

        int before = decisions.size();
        // When opening new station, only the smallest eligible task is allowed
        decisions.removeIf(d -> d != minTask);
        int after = decisions.size();
        if (after < before) {
            symmetryBreakingPrunes += (before - after);
        }
    }

    // ==================== Eligibility ====================

    private boolean isTaskEligible(int task, Set<Integer> remaining, Set<Integer> currentStationTasks) {
        for (int pred : predecessors[task]) {
            if (remaining.contains(pred) && !currentStationTasks.contains(pred)) {
                return false;
            }
        }
        return true;
    }

    private boolean canAddMoreTasksToCurrentStation(HRCPSState state) {
        if (state.currentStationTasks().isEmpty()) return false;
        Set<Integer> remaining = state.getRemainingTasks(nbTasks);
        Set<Integer> currentTasks = state.currentStationTasks();
        for (int task : remaining) {
            if (!isTaskEligible(task, remaining, currentTasks)) continue;
            Set<Integer> testTasks = new HashSet<>(currentTasks);
            testTasks.add(task);
            if (isStationFeasibleOptimistic(testTasks)) return true;
        }
        return false;
    }

    // ==================== Transition ====================

    @Override
    public HRCPSState transition(HRCPSState state, Decision decision) {
        int task = decision.value();
        boolean taskIsMaybeCompleted = state.maybeCompletedTasks().contains(task);

        boolean needNewStation;
        if (state.currentStationTasks().isEmpty()) {
            needNewStation = true;
        } else {
            Set<Integer> testTasks = new LinkedHashSet<>(state.currentStationTasks());
            testTasks.add(task);
            needNewStation = !isStationFeasible(testTasks);
        }

        if (!needNewStation) {
            Set<Integer> newStationTasks = new LinkedHashSet<>(state.currentStationTasks());
            newStationTasks.add(task);
            Set<Integer> newMaybe = new LinkedHashSet<>(state.maybeCompletedTasks());
            if (taskIsMaybeCompleted) newMaybe.remove(task);
            return new HRCPSState(state.completedTasks(), newStationTasks, newMaybe);
        } else {
            Set<Integer> newCompleted = new LinkedHashSet<>(state.completedTasks());
            Set<Integer> newMaybe = new LinkedHashSet<>(state.maybeCompletedTasks());
            if (!state.currentStationTasks().isEmpty()) {
                newCompleted.addAll(state.currentStationTasks());
            }
            Set<Integer> freshStation = new LinkedHashSet<>();
            freshStation.add(task);
            if (taskIsMaybeCompleted) newMaybe.remove(task);
            return new HRCPSState(newCompleted, freshStation, newMaybe);
        }
    }

    @Override
    public double transitionCost(HRCPSState state, Decision decision) {
        int task = decision.value();

        if (state.maybeCompletedTasks().contains(task)) {
            return 0.0;
        }
        if (state.currentStationTasks().isEmpty()) {
            return 1.0;
        }
        Set<Integer> testTasks = new LinkedHashSet<>(state.currentStationTasks());
        testTasks.add(task);
        return isStationFeasible(testTasks) ? 0.0 : 1.0;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        HRCPSState state = initialState();
        int stationCount = 0;

        for (int task : solution) {
            boolean needNewStation;
            if (state.currentStationTasks().isEmpty()) {
                needNewStation = true;
            } else {
                Set<Integer> testTasks = new LinkedHashSet<>(state.currentStationTasks());
                testTasks.add(task);
                needNewStation = !isStationFeasible(testTasks);
            }

            if (!needNewStation) {
                Set<Integer> newStation = new LinkedHashSet<>(state.currentStationTasks());
                newStation.add(task);
                state = new HRCPSState(state.completedTasks(), newStation, state.maybeCompletedTasks());
            } else {
                Set<Integer> newCompleted = new LinkedHashSet<>(state.completedTasks());
                if (!state.currentStationTasks().isEmpty()) {
                    newCompleted.addAll(state.currentStationTasks());
                    stationCount++;
                }
                state = new HRCPSState(newCompleted, Set.of(task), state.maybeCompletedTasks());
            }
        }
        if (!state.currentStationTasks().isEmpty()) stationCount++;
        return stationCount;
    }

    // ==================== Station Feasibility ====================

    /**
     * Optimistic check (for domain generation): uses minimum-work estimate.
     */
    private boolean isStationFeasibleOptimistic(Set<Integer> tasks) {
        int sumHuman = sumHumanDurations(tasks);
        if (sumHuman <= cycleTime) return true;
        // With human + robot parallelism, total work capacity ≈ 2 × cycleTime
        return sumMinWork(tasks) <= 2.0 * cycleTime;
    }

    /**
     * Exact feasibility check (for transition and transitionCost).
     */
    private boolean isStationFeasible(Set<Integer> tasks) {
        // Quick optimistic pass
        int sumHuman = sumHumanDurations(tasks);
        if (sumHuman <= cycleTime) return true;

        // Capacity cut
        if (useCapacityCut) {
            capacityCutChecks++;
            if (sumMinWork(tasks) > 2.0 * cycleTime) {
                capacityCutPrunes++;
                return false;
            }
        }

        // Infeasibility cache
        if (useInfeasibilityCache && infeasibilityCache.containsInfeasibleSubset(tasks)) {
            return false;
        }

        // Call inner HRCP solver
        boolean feasible = isStationSchedulable(tasks);

        if (!feasible && useInfeasibilityCache) {
            infeasibilityCache.recordInfeasible(tasks, true);
        }
        return feasible;
    }

    private int sumHumanDurations(Set<Integer> tasks) {
        int sum = 0;
        for (int t : tasks) sum += innerProblem.humanDurations[t];
        return sum;
    }

    /** Minimum total work: min(h, r, 2c) per task. */
    private double sumMinWork(Set<Integer> tasks) {
        double sum = 0;
        for (int t : tasks) {
            sum += Math.min(innerProblem.humanDurations[t],
                    Math.min(innerProblem.robotDurations[t],
                            2.0 * innerProblem.collaborationDurations[t]));
        }
        return sum;
    }

    // ==================== Inner HRCP Solver ====================

    /**
     * Solve the HRCP sub-problem for the given task set and check whether the
     * optimal makespan ≤ cycleTime.
     * <p>
     * Two-phase approach for speed:
     * <ol>
     *   <li><b>Phase 1 (HRC, no precedences)</b> – solve the relaxed problem
     *       that ignores precedence constraints.  Because removing precedences
     *       can only decrease the makespan, this gives a lower bound.  If the
     *       lower bound already exceeds the cycle time the station is
     *       infeasible and we skip the expensive Phase 2.</li>
     *   <li><b>Phase 2 (HRCP, with precedences)</b> – solve the full problem.
     *       Only reached when Phase 1 reports feasible.</li>
     * </ol>
     */
    public boolean isStationSchedulable(Set<Integer> tasks) {
        if (feasibilityCache.containsKey(tasks)) {
            cacheHitCount++;
            return feasibilityCache.get(tasks);
        }
        cacheMissCount++;

        // ---- Phase 1: HRC (no precedences) – quick lower-bound check ----
        hrcSolverCallCount++;
        HRCProblem hrcProblem = createHRCSubProblem(tasks);
        Model<HRCState> hrcModel = buildHRCModel(hrcProblem);
        Solution hrcSolution = Solvers.minimizeAstar(hrcModel);
        double hrcMakespan = hrcSolution.statistics().incumbent();

        if (hrcMakespan > cycleTime) {
            // Precedence-free lower bound already exceeds cycle time → infeasible
            hrcPrunedCount++;
            feasibilityCache.put(Set.copyOf(tasks), false);
            return false;
        }

        // ---- Phase 2: HRCP (with precedences) – exact check ----
        innerSolverCallCount++;
        HRCPProblem subProblem = createSubProblem(tasks);
        Model<HRCPState> model = buildInnerModel(subProblem);
        Solution solution = Solvers.minimizeAstar(model);
        boolean feasible = solution.statistics().incumbent() <= cycleTime;

        feasibilityCache.put(Set.copyOf(tasks), feasible);
        return feasible;
    }

    /** Create a precedence-free HRC sub-problem for the given tasks. */
    private HRCProblem createHRCSubProblem(Set<Integer> tasks) {
        List<Integer> taskList = new ArrayList<>(tasks);
        int subN = taskList.size();
        int[] subH = new int[subN];
        int[] subR = new int[subN];
        int[] subC = new int[subN];
        for (int i = 0; i < subN; i++) {
            int orig = taskList.get(i);
            subH[i] = innerProblem.humanDurations[orig];
            subR[i] = innerProblem.robotDurations[orig];
            subC[i] = innerProblem.collaborationDurations[orig];
        }
        return new HRCProblem(subH, subR, subC);
    }

    /** Build an A* model for the precedence-free HRC problem. */
    private Model<HRCState> buildHRCModel(HRCProblem problem) {
        return new Model<>() {
            @Override public Problem<HRCState> problem() { return problem; }
            @Override public FastLowerBound<HRCState> lowerBound() { return new HRCFastLowerBound(problem); }
            @Override public DominanceChecker<HRCState> dominance() {
                return new SimpleDominanceChecker<>(new HRCDominance(), problem.nbVars());
            }
        };
    }

    @SuppressWarnings("unchecked")
    private HRCPProblem createSubProblem(Set<Integer> tasks) {
        List<Integer> taskList = new ArrayList<>(tasks);
        int subN = taskList.size();

        int[] subH = new int[subN];
        int[] subR = new int[subN];
        int[] subC = new int[subN];
        List<Integer>[] subPreds = new List[subN];

        Map<Integer, Integer> origToSub = new HashMap<>();
        for (int i = 0; i < subN; i++) origToSub.put(taskList.get(i), i);

        for (int i = 0; i < subN; i++) {
            int orig = taskList.get(i);
            subH[i] = innerProblem.humanDurations[orig];
            subR[i] = innerProblem.robotDurations[orig];
            subC[i] = innerProblem.collaborationDurations[orig];
            subPreds[i] = new ArrayList<>();
            for (int pred : innerProblem.predecessors[orig]) {
                Integer subIdx = origToSub.get(pred);
                if (subIdx != null) subPreds[i].add(subIdx);
            }
        }
        return new HRCPProblem(subH, subR, subC, subPreds);
    }

    private Model<HRCPState> buildInnerModel(HRCPProblem problem) {
        return new Model<>() {
            @Override
            public Problem<HRCPState> problem() { return problem; }

            @Override
            public FastLowerBound<HRCPState> lowerBound() { return new HRCPFastLowerBound(problem); }

            @Override
            public DominanceChecker<HRCPState> dominance() {
                return new SimpleDominanceChecker<>(new HRCPDominance(), problem.nbVars());
            }
        };
    }

    // ==================== Inner Solution Details ====================

    public static class InnerSolution {
        public final int[] tasks;
        public final int[] modes;
        public InnerSolution(int[] tasks, int[] modes) {
            this.tasks = tasks;
            this.modes = modes;
        }
    }

    public InnerSolution solveInnerProblemWithModes(Set<Integer> stationTasks) {
        List<Integer> taskList = new ArrayList<>(stationTasks);
        int[] subSolution = solutionCache.get(stationTasks);

        if (subSolution == null) {
            HRCPProblem subProblem = createSubProblem(stationTasks);
            Model<HRCPState> model = buildInnerModel(subProblem);
            final int[][] holder = {null};
            Solvers.minimizeAstar(model, (sol, stats) -> {
                if (sol != null && sol.length > 0) holder[0] = sol;
            });
            subSolution = holder[0];
            if (subSolution != null) {
                solutionCache.put(Set.copyOf(stationTasks), subSolution);
            }
        }

        if (subSolution == null || subSolution.length == 0) return null;

        // The HRCP solution has n+1 entries; last is the dummy variable
        int realLen = Math.min(subSolution.length, taskList.size());
        int[] originalTasks = new int[realLen];
        int[] modes = new int[realLen];
        for (int i = 0; i < realLen; i++) {
            int dec = subSolution[i];
            int subTask = dec / 3;
            int mode = dec % 3;
            originalTasks[i] = taskList.get(subTask);
            modes[i] = mode;
        }
        return new InnerSolution(originalTasks, modes);
    }

    // ==================== Lower Bound Helpers ====================

    private double computeTotalLowerBound(HRCPSState state) {
        int completedStations = computeCompletedStations(state);
        double unfinishedLB = computeUnfinishedStationsLB(state);
        return completedStations + unfinishedLB;
    }

    private int computeCompletedStations(HRCPSState state) {
        if (state.completedTasks().isEmpty()) return 0;
        HRCPSFastLowerBound lb = new HRCPSFastLowerBound(this);
        double val = lb.computeLowerBound(state.completedTasks());
        return (int) Math.ceil(val);
    }

    private double computeUnfinishedStationsLB(HRCPSState state) {
        Set<Integer> remaining = state.getRemainingTasks(nbTasks);
        Set<Integer> current = state.currentStationTasks();
        if (remaining.isEmpty() && current.isEmpty()) return 0;

        HRCPSFastLowerBound lb = new HRCPSFastLowerBound(this);

        if (current.isEmpty()) {
            return lb.computeLowerBound(remaining);
        }

        Set<Integer> allUnfinished = new HashSet<>(current);
        allUnfinished.addAll(remaining);
        double total = lb.computeLowerBound(allUnfinished);
        return Math.max(0, total - 1);
    }

    // ==================== Bound Propagation ====================

    public void updateBestSolution(int solutionValue) {
        if (useBoundPropagation) boundPropagation.updateBestSolution(solutionValue);
    }

    // ==================== Statistics ====================

    public void printCacheStatistics() {
        System.out.println("\n=== Cache Statistics ===");
        System.out.printf("Feasibility cache entries: %d%n", feasibilityCache.size());
        System.out.printf("Solution cache entries: %d%n", solutionCache.size());
        System.out.printf("Cache hits: %d, misses: %d%n", cacheHitCount, cacheMissCount);
        System.out.printf("HRC solver calls (no precedences): %d, pruned: %d%n", hrcSolverCallCount, hrcPrunedCount);
        System.out.printf("HRCP solver calls (with precedences): %d%n", innerSolverCallCount);
    }

    public void printOptimizationStatistics() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Optimisation Strategy Statistics");
        System.out.println("=".repeat(60));

        System.out.println("CapacityCut: " + (useCapacityCut ? "Enabled ✓" : "Disabled ✗"));
        if (useCapacityCut) {
            double rate = capacityCutChecks > 0 ? (100.0 * capacityCutPrunes / capacityCutChecks) : 0;
            System.out.printf("  checks=%d, prunes=%d, rate=%.2f%%%n", capacityCutChecks, capacityCutPrunes, rate);
        }

        System.out.println("InfeasibilityCache: " + (useInfeasibilityCache ? "Enabled ✓" : "Disabled ✗"));
        if (useInfeasibilityCache) System.out.println("  " + infeasibilityCache.getStatistics());

        System.out.println("BoundPropagation: " + (useBoundPropagation ? "Enabled ✓" : "Disabled ✗"));
        if (useBoundPropagation) System.out.println("  " + boundPropagation.getStatistics());

        System.out.println("SymmetryBreaking: " + (useSymmetryBreaking ? "Enabled ✓" : "Disabled ✗"));
        if (useSymmetryBreaking) {
            double rate = symmetryBreakingChecks > 0 ? (100.0 * symmetryBreakingPrunes / symmetryBreakingChecks) : 0;
            System.out.printf("  checks=%d, prunes=%d, rate=%.2f%%%n", symmetryBreakingChecks, symmetryBreakingPrunes, rate);
        }
        System.out.println("=".repeat(60));
    }

    private void logProgress(HRCPSState state, int var) {
        if (domainCallCount == 1) {
            startTime = System.currentTimeMillis();
            System.out.printf("[HRCPS] First domain call: var=%d, remaining=%d%n",
                    var, state.getRemainingTasks(nbTasks).size());
        }
        long now = System.currentTimeMillis();
        if (now - lastLogTime > 5000) {
            double elapsed = (now - startTime) / 1000.0;
            System.out.printf("[HRCPS] %.1fs | domain=%d, inner=%d (hit=%d, miss=%d), var=%d, remaining=%d%n",
                    elapsed, domainCallCount, innerSolverCallCount, cacheHitCount, cacheMissCount,
                    var, state.getRemainingTasks(nbTasks).size());
            lastLogTime = now;
        }
    }

    private void logDecisions(HRCPSState state, List<Integer> decisions) {
        if (decisions.isEmpty() && !state.isComplete(nbTasks)) {
            System.out.printf("[WARNING] domain() empty! remaining=%d, currentStation=%s%n",
                    state.getRemainingTasks(nbTasks).size(), state.currentStationTasks());
        }
    }
}

