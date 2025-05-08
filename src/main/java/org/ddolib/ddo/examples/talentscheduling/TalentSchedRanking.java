package org.ddolib.ddo.examples.talentscheduling;

import org.ddolib.ddo.heuristics.StateRanking;

public class TalentSchedRanking implements StateRanking<TalentSchedState> {
    @Override
    public int compare(TalentSchedState o1, TalentSchedState o2) {
        int totalO1 = o1.remainingScenes().cardinality() + o1.maybeScenes().cardinality();
        int totalO2 = o2.remainingScenes().cardinality() + o2.maybeScenes().cardinality();
        return Integer.compare(totalO1, totalO2);
    }
}
