package org.ddolib.examples.salbp1;

import org.ddolib.modeling.StateRanking;

public class SALBPRanking implements StateRanking<SALBPState> {
    @Override
    public int compare(SALBPState s1, SALBPState s2) {
        return  Double.compare(s1.remainingTasks().size(), s2.remainingTasks().size());
    }
}
