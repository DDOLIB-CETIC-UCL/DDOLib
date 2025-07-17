package org.ddolib.ddo.examples.mks;

import org.ddolib.ddo.heuristics.StateRanking;

public class MKSRanking implements StateRanking<MKSState> {
    @Override
    public int compare(MKSState o1, MKSState o2) {
        double avgCapa1 = 0;
        double avgCapa2 = 0;
        for (int dim = 0; dim < o1.capacities.length; dim++) {
            avgCapa1 += o1.capacities[dim];
            avgCapa2 += o2.capacities[dim];
        }
        avgCapa1 /= o1.capacities.length;
        avgCapa2 /= o2.capacities.length;

        return Double.compare(avgCapa1, avgCapa2);
    }
}
