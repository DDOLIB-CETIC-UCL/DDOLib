package org.ddolib.examples.srflp;


import org.ddolib.modeling.StateRanking;

/**
 * Class that define a ranking between two {@link SRFLPState}
 */
public class SRFLPRanking implements StateRanking<SRFLPState> {

    @Override
    public int compare(SRFLPState o1, SRFLPState o2) {
        int total1 = o1.must().cardinality() + o1.maybe().cardinality();
        int total2 = o2.must().cardinality() + o2.maybe().cardinality();
        return Integer.compare(total1, total2);
    }
}
