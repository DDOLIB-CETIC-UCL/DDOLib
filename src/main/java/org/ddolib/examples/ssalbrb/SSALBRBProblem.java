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
     * Constructs an SSALBRBProblem from an HRCP file.
     * <p>
     * File format (lines starting with {@code #} are comments):
     * <ul>
     *   <li>First data line: {@code n [optimal]}</li>
     *   <li>Next {@code n} lines: {@code humanDur robotDur collabDur numPreds [pred0 pred1 ...]}</li>
     * </ul>
     * Tasks are 0-indexed and precedences are given as <em>predecessors</em>.
     *
     * @param file path to the instance file
     * @throws IOException if the file cannot be read
     */
    public SSALBRBProblem(final String file) throws IOException {
        // Read all non-empty, non-comment lines
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        }

        // Parse header: n [optimal]
        String[] header = lines.get(0).split("\\s+");
        int n = Integer.parseInt(header[0]);
        this.optimal = header.length >= 2
                ? Optional.of(Double.parseDouble(header[1]))
                : Optional.empty();

        this.nbTasks = n;
        this.humanDurations = new int[n];
        this.robotDurations = new int[n];
        this.collaborationDurations = new int[n];
        this.cycleTime = Integer.MAX_VALUE;

        // Initialise successor map
        Map<Integer, List<Integer>> succ = new HashMap<>();
        for (int i = 0; i < n; i++) {
            succ.put(i, new ArrayList<>());
        }

        // Parse each task line: humanDur robotDur collabDur numPreds [pred0 pred1 ...]
        for (int i = 0; i < n; i++) {
            String[] tok = lines.get(1 + i).split("\\s+");
            humanDurations[i] = Integer.parseInt(tok[0]);
            robotDurations[i] = Integer.parseInt(tok[1]);
            collaborationDurations[i] = Integer.parseInt(tok[2]);
            int numPreds = Integer.parseInt(tok[3]);
            for (int p = 0; p < numPreds; p++) {
                int pred = Integer.parseInt(tok[4 + p]);
                // predecessor → current task  ⟹  succ[pred] contains i
                succ.get(pred).add(i);
            }
        }

        this.successors = succ;
        this.predecessors = buildPredecessors(this.successors, this.nbTasks);
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
        return simulateTransition(state, decision.value()).nextState();
    }

    @Override
    public double transitionCost(SSALBRBState state, Decision decision) {
        TransitionInfo info = simulateTransition(state, decision.value());
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
