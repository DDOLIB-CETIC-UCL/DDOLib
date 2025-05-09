package org.ddolib.ddo.examples.srflp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Iterator;

public class SRFLPRelax implements Relaxation<SRFLPState> {
    @Override
    public SRFLPState mergeStates(Iterator<SRFLPState> states) {
        return null;
    }

    @Override
    public int relaxEdge(SRFLPState from, SRFLPState to, SRFLPState merged, Decision d, int cost) {
        return 0;
    }
}
