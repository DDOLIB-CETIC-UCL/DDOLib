package org.ddolib.examples.salbp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

public class SALBRelax implements Relaxation<SALBState> {
    final SALBProblem problem;
    public SALBRelax(SALBProblem problem) {
        this.problem = problem;
    }

    @Override
    public SALBState mergeStates(final Iterator<SALBState> states) {
        return new SALBState(new BitSet[problem.nbTasks], new double[problem.nbTasks]);
    }

    @Override
    public double relaxEdge(SALBState from, SALBState to, SALBState merged, Decision decisin, double cost) {
        return cost;
    }
}
