package org.ddolib.examples.ddo.pdp;

import org.ddolib.modeling.StateRanking;

public class PDPRanking implements StateRanking<PDPState> {
    @Override
    public int compare(final PDPState o1, final PDPState o2){
        //children of merged are good candidates for merged as well
        //there are the one with imprecise min and maxContent
        return 0; //Integer.compare(o1.uncertaintyOnContent(), o2.uncertaintyOnContent());
    }
}
