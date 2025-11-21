package org.ddolib.examples.tsalt;

import org.ddolib.modeling.StateRanking;

public class TSRanking implements StateRanking<TSState> {
    @Override
    public int compare(TSState o1, TSState o2) {
        int totalO1 = o1.remainingScenes().cardinality() + o1.onLocationActors().cardinality();
        int totalO2 = o2.remainingScenes().cardinality() + o2.onLocationActors().cardinality();
        return Integer.compare(totalO1, totalO2);
    }
}
