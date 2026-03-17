package org.ddolib.examples.binpacking;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Arrays;
import java.util.Iterator;

public class BPPRelax implements Relaxation<BPPState> {

    BPPProblem problem;

    public BPPRelax(BPPProblem problem) {
        this.problem = problem;
    }

    @Override
    public BPPState mergeStates(Iterator<BPPState> states) {

        int[] nbus = new int[problem.nbItems];
        Arrays.fill(nbus, problem.binMaxSpace+1);

        while (states.hasNext()) {
            BPPState state = states.next();
            int[] bus = state.binsUsedSpace.clone();
            Arrays.sort(bus);
            for (int i = 0; i < problem.nbItems; i++) {
                if(i < bus.length) {
                    nbus[i] = Math.min(bus[i],nbus[i]);
                } else {
                    nbus[i] = 0;
                }
            }
        }
        return new BPPState(Arrays.stream(nbus).takeWhile(b -> b > 0).toArray(), problem.binMaxSpace);
    }

    @Override
    public double relaxEdge(BPPState from, BPPState to, BPPState merged, Decision d, double cost) {
        return cost;
    }
}
