package org.ddolib.examples.hrcps;

import org.ddolib.modeling.StateRanking;

/**
 * Ranking for the outer HRCPS problem.
 * Prefer states that have made more progress with fewer stations.
 */
public class HRCPSRanking implements StateRanking<HRCPSState> {

    @Override
    public int compare(HRCPSState first, HRCPSState second) {
        // Fewer completed tasks → fewer closed stations (better)
        int cmp = Integer.compare(first.completedTasks().size(), second.completedTasks().size());
        if (cmp != 0) return cmp;

        // More tasks in current station → packing more tightly (better)
        cmp = Integer.compare(second.currentStationTasks().size(), first.currentStationTasks().size());
        if (cmp != 0) return cmp;

        // More maybe-completed → more tasks potentially handled (better)
        return Integer.compare(second.maybeCompletedTasks().size(), first.maybeCompletedTasks().size());
    }
}

