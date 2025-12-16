package org.ddolib.examples.ssalbrb;

import org.ddolib.modeling.StateRanking;

public class SSALBRBRanking implements StateRanking<SSALBRBState> {

    @Override
    public int compare(SSALBRBState first, SSALBRBState second) {
        // Primary: compare makespan (lower is better)
        int makespanCmp = Integer.compare(first.makespan(), second.makespan());
        if (makespanCmp != 0) {
            return makespanCmp;
        }

        // Secondary: compare number of assigned tasks (more is better)
        int firstAssigned = 0;
        int secondAssigned = 0;
        for (int i = 0; i < first.earliestStartTimes().size(); i++) {
            if (first.earliestStartTimes().get(i) < 0) firstAssigned++;
            if (second.earliestStartTimes().get(i) < 0) secondAssigned++;
        }
        int assignedCmp = Integer.compare(secondAssigned, firstAssigned); // reversed: more is better
        if (assignedCmp != 0) {
            return assignedCmp;
        }

        // Tertiary: compare resource balance (more balanced is better)
        int firstBalance = Math.abs(first.humanAvailable() - first.robotAvailable());
        int secondBalance = Math.abs(second.humanAvailable() - second.robotAvailable());
        return Integer.compare(firstBalance, secondBalance); // less imbalance is better
    }
}
