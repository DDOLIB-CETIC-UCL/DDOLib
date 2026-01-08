package org.ddolib.examples.tsalt;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

public class TSRelax implements Relaxation<TSState> {
    private final TSProblem problem;

    public TSRelax(TSProblem problem) {
        this.problem = problem;
    }

    @Override
    public TSState mergeStates(Iterator<TSState> states) {
        BitSet mergedRemaining = new BitSet(problem.nbVars());
        BitSet mergedActors = new BitSet(problem.nbActors);
        mergedActors.set(0, problem.nbActors, true);

        while (states.hasNext()) {
            TSState state = states.next();
            mergedRemaining.or(state.remainingScenes());
            mergedActors.and(state.onLocationActors());
        }

        return new TSState(mergedRemaining, mergedActors);
    }

    @Override
    public double relaxEdge(TSState from, TSState to, TSState merged, Decision d, double cost) {
        return cost;
    }
}
