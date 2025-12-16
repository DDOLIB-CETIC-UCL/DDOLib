package org.ddolib.examples.ssalbrb;

import org.ddolib.modeling.FastLowerBound;

import java.util.HashSet;
import java.util.Set;

public class SSALBRBFastLowerBound implements FastLowerBound<SSALBRBState> {

    private final SSALBRBProblem problem;

    public SSALBRBFastLowerBound(SSALBRBProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(SSALBRBState state, Set<Integer> variables) {
        // Lower bound based on the workload balancing strategy from the 1126 model.
        // Tasks are partitioned into (virtual) sets I1/I2/I3; in our instances all tasks
        // can be processed by any mode, so I1 is empty while I2/I3 contain the same tasks.

        Set<Integer> tasks;
        if (variables.isEmpty()) {
            // Extract unassigned tasks from state (E_t >= 0)
            tasks = new HashSet<>();
            for (int i = 0; i < state.earliestStartTimes().size(); i++) {
                if (state.isUnassigned(i)) {
                    tasks.add(i);
                }
            }
        } else {
            tasks = variables;
        }

        // If no tasks remaining, lower bound is 0 (no additional time needed)
        if (tasks.isEmpty()) {
            return 0.0;
        }

        double t1 = 0.0; // Sum of processing times for human-only tasks (none here, kept for completeness)
        double t2 = 0.0; // Sum of robot processing times for tasks in I2
        double t3 = 0.0; // Sum of collaboration processing times for tasks in I3

        for (Integer task : tasks) {
            t2 += problem.robotDurations[task];
            t3 += problem.collaborationDurations[task];
        }

        double NS = 1.0; // Single station (one human + one robot team)
        double lowerBoundIncrement;
        if (t1 < t2) {
            lowerBoundIncrement = (t1 + t3 + (t2 - t1) / 3.0) / NS;
        } else {
            lowerBoundIncrement = (t1 + t3) / NS;
        }

        return lowerBoundIncrement;
    }
}
