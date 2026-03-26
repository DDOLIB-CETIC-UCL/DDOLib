package org.ddolib.examples.hrcps;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.*;

/**
 * Relaxation for the outer HRCPS problem: merges states.
 * <ul>
 *   <li>completedTasks: intersection (conservative).</li>
 *   <li>currentStationTasks: intersection (optimistic).</li>
 *   <li>maybeCompletedTasks: union minus intersection.</li>
 * </ul>
 */
public class HRCPSRelax implements Relaxation<HRCPSState> {

    @Override
    public HRCPSState mergeStates(Iterator<HRCPSState> states) {
        if (!states.hasNext()) throw new IllegalArgumentException("Cannot merge empty state set");

        List<HRCPSState> list = new ArrayList<>();
        states.forEachRemaining(list::add);
        if (list.size() == 1) return list.get(0);

        Set<Integer> mergedCompleted = new LinkedHashSet<>(list.get(0).completedTasks());
        for (int i = 1; i < list.size(); i++) mergedCompleted.retainAll(list.get(i).completedTasks());

        Set<Integer> mergedCurrent = new LinkedHashSet<>(list.get(0).currentStationTasks());
        for (int i = 1; i < list.size(); i++) mergedCurrent.retainAll(list.get(i).currentStationTasks());

        Set<Integer> allAssigned = new LinkedHashSet<>();
        for (HRCPSState s : list) {
            allAssigned.addAll(s.completedTasks());
            allAssigned.addAll(s.currentStationTasks());
            allAssigned.addAll(s.maybeCompletedTasks());
        }

        Set<Integer> mergedMaybe = new LinkedHashSet<>(allAssigned);
        mergedMaybe.removeAll(mergedCompleted);
        mergedMaybe.removeAll(mergedCurrent);

        return new HRCPSState(mergedCompleted, mergedCurrent, mergedMaybe);
    }

    @Override
    public double relaxEdge(HRCPSState from, HRCPSState to, HRCPSState merged,
                            Decision decision, double originalCost) {
        return originalCost;
    }
}

