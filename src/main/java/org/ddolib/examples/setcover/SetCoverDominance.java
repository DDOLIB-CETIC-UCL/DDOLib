package org.ddolib.examples.setcover;

import org.ddolib.modeling.Dominance;

import java.util.BitSet;

public class SetCoverDominance implements Dominance<SetCoverState> {
    @Override
    public Object getKey(SetCoverState state) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(SetCoverState state1, SetCoverState state2) {
        BitSet uncoveredItems1 = state1.uncoveredItems();
        BitSet uncoveredItems2 = state2.uncoveredItems();

        BitSet temp = (BitSet) uncoveredItems2.clone();
        temp.andNot(uncoveredItems1);
        return temp.isEmpty();
    }
}
