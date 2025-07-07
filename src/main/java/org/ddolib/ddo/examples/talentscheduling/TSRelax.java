package org.ddolib.ddo.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

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
        mergedRemaining.set(0, problem.nbVars(), true);
        BitSet mergedMaybe = new BitSet(problem.nbVars());

        while (states.hasNext()) {
            TSState state = states.next();
            mergedRemaining.and(state.remainingScenes());
            mergedMaybe.or(state.remainingScenes());
            mergedMaybe.or(state.maybeScenes());
        }
        mergedMaybe.andNot(mergedRemaining);

        return new TSState(mergedRemaining, mergedMaybe);
    }

    @Override
    public double relaxEdge(TSState from, TSState to, TSState merged, Decision d, double cost) {
        return cost;
    }

}
