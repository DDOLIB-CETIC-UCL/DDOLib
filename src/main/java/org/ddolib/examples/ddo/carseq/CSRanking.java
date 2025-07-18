package org.ddolib.examples.ddo.carseq;

import org.ddolib.modeling.StateRanking;

public class CSRanking implements StateRanking<CSState> {
    @Override
    public int compare(final CSState state1, final CSState state2) {
        return 0;
    }
}
