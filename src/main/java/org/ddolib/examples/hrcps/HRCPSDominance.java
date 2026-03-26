package org.ddolib.examples.hrcps;

import org.ddolib.modeling.Dominance;

import java.util.HashSet;
import java.util.Set;

/**
 * Dominance rule for the outer HRCPS problem.
 * <p>
 * Two states are comparable only when they have the same set of all assigned
 * tasks (completedTasks ∪ currentStationTasks ∪ maybeCompletedTasks).
 * <p>
 * Since every station has both resources (no robot allocation), states with
 * the same assigned-task set face the same remaining problem and are
 * considered equivalent. The framework's cost tracking selects the one with
 * fewer accumulated stations.
 */
public class HRCPSDominance implements Dominance<HRCPSState> {

    private record DominanceKey(Set<Integer> allAssigned) {}

    @Override
    public Object getKey(HRCPSState state) {
        Set<Integer> all = new HashSet<>(state.completedTasks());
        all.addAll(state.currentStationTasks());
        all.addAll(state.maybeCompletedTasks());
        return new DominanceKey(all);
    }

    @Override
    public boolean isDominatedOrEqual(HRCPSState s1, HRCPSState s2) {
        // Same assigned tasks → equivalent (no resource distinction)
        return true;
    }
}

