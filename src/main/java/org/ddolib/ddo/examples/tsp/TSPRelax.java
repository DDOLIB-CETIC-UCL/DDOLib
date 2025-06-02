package org.ddolib.ddo.examples.tsp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

public class TSPRelax implements Relaxation<TSPState> {
    private final TSPProblem problem;

    public TSPRelax(TSPProblem problem) {
        this.problem = problem;
    }

    @Override
    public TSPState mergeStates(final Iterator<TSPState> states) {
        //take the union
        //the current node is normally the same in all states
        BitSet toVisit = new BitSet(problem.n);
        BitSet current = new BitSet(problem.n);

        while (states.hasNext()) {
            TSPState state = states.next();
            toVisit.or(state.toVisit);
            current.or(state.current);
        }

        return new TSPState(current, toVisit, problem.sortedAdjacents.initialHeuristics());
    }

    @Override
    public int relaxEdge(TSPState from, TSPState to, TSPState merged, Decision d, int cost) {
        return cost;
    }

    @Override
    public int fastUpperBound(TSPState state, Set<Integer> variables) {
        int nbHopsToDo = variables.size();
        int lb = state.getHeuristics(nbHopsToDo, this.problem.sortedAdjacents);
        return -lb;
    }
}
