package org.ddolib.examples.ddo.mks;

import org.ddolib.modeling.Dominance;

public class MKSDominance implements Dominance<MKSState, Integer> {

    @Override
    public Integer getKey(MKSState state) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(MKSState state1, MKSState state2) {
        for (int dim = 0; dim < state1.capacities.length; dim++) {
            if (state1.capacities[dim] > state2.capacities[dim]) {
                return false;
            }
        }

        return true;
    }
}
