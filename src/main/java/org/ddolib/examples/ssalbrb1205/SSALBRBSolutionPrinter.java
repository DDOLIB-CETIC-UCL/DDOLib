package org.ddolib.examples.ssalbrb1205;

import java.util.Locale;

/**
 * Utility to replay and display a solution sequence using the current state model
 * (\u27e8r_h, r_r, E\u27e9). It relies on {@link SSALBRBProblem#simulateTransition}
 * to ensure the printed schedule matches the real transition logic.
 */
public final class SSALBRBSolutionPrinter {

    private SSALBRBSolutionPrinter() {
    }

    public static void printSolution(SSALBRBProblem problem, int[] solution) {
        if (solution == null) {
            System.out.println("Solution is null");
            return;
        }

        SSALBRBState state = problem.initialState();
        System.out.println("=== Reconstructed schedule ===");
        for (int i = 0; i < solution.length; i++) {
            SSALBRBProblem.TransitionInfo info = problem.simulateTransition(state, solution[i]);
            
            // Extract unassigned tasks from E vector (E_t >= 0)
            int unassignedCount = 0;
            for (int t = 0; t < info.nextState().earliestStartTimes().size(); t++) {
                if (info.nextState().isUnassigned(t)) {
                    unassignedCount++;
                }
            }
            
            System.out.printf(Locale.ROOT,
                    "Step %d: task %d, mode=%s, start=%d, finish=%d, unassigned=%d%n",
                    i + 1,
                    info.task(),
                    modeLabel(info.mode()),
                    info.startTime(),
                    info.completionTime(),
                    unassignedCount);
            state = info.nextState();
        }
        System.out.println("Final state: " + state);
    }

    private static String modeLabel(int mode) {
        return switch (mode) {
            case SSALBRBProblem.MODE_HUMAN -> "H";
            case SSALBRBProblem.MODE_ROBOT -> "R";
            case SSALBRBProblem.MODE_COLLABORATION -> "C";
            default -> "?";
        };
    }
}
