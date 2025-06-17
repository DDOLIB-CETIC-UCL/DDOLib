package org.ddolib.ddo.examples.pdp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

class PDPRelax implements Relaxation<PDPState> {
    private final PDPProblem problem;

    public PDPRelax(PDPProblem problem) {
        this.problem = problem;
    }

    @Override
    public PDPState mergeStates(final Iterator<PDPState> states) {
        //NB: the current node is normally the same in all states
        BitSet openToVisit = new BitSet(problem.n);
        BitSet current = new BitSet(problem.n);
        BitSet allToVisit = new BitSet(problem.n);

        while (states.hasNext()) {
            PDPState state = states.next();
            //take the union; loose precision here
            openToVisit.or(state.openToVisit);
            allToVisit.or(state.allToVisit);
            current.or(state.current);
        }
        //the heuristics is reset to the initial sorted edges and will be filtered again from scratch
        return new PDPState(current, openToVisit, allToVisit, problem.sortedAdjacents.initialHeuristics());
    }

    @Override
    public int relaxEdge(PDPState from, PDPState to, PDPState merged, Decision d, int cost) {
        return cost;
    }

    @Override
    public int fastUpperBound(PDPState state, Set<Integer> variables) {
        if (state.current.cardinality() != 1) {
            throw new Error("no fast upper bound when no current");
        } else {
            int nbHopsToDo = variables.size();
            int lb = state.getHeuristics(nbHopsToDo, this.problem.sortedAdjacents);
            return -lb;
        }
    }
}
