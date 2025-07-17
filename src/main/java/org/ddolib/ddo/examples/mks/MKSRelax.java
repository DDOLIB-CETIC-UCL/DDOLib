package org.ddolib.ddo.examples.mks;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Iterator;
import java.util.Set;

import static java.lang.Math.max;

public class MKSRelax implements Relaxation<MKSState> {

    @Override
    public MKSState mergeStates(Iterator<MKSState> states) {
        assert states.hasNext();
        MKSState state = states.next();
        double[] capa = state.capacities.clone();
        while (states.hasNext()) {
            state = states.next();
            for (int dim = 0; dim < capa.length; dim++) {
                capa[dim] = max(capa[dim], state.capacities[dim]);
            }
        }
        return new MKSState(capa);
    }

    @Override
    public double relaxEdge(MKSState from, MKSState to, MKSState merged, Decision d, double cost) {
        return cost;
    }
}
