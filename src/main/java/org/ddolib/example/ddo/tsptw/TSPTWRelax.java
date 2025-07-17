package org.ddolib.example.ddo.tsptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TSPTWRelax implements Relaxation<TSPTWState> {

    private static final int INFINITY = Integer.MAX_VALUE;

    private final int numVar;

    public TSPTWRelax(TSPTWProblem problem) {
        this.numVar = problem.nbVars();
    }

    @Override
    public TSPTWState mergeStates(Iterator<TSPTWState> states) {
        Set<Integer> mergedPos = new HashSet<>();
        int mergedTime = INFINITY;
        BitSet mergedMust = new BitSet(numVar);
        mergedMust.set(0, numVar, true);
        BitSet mergedPossibly = new BitSet(numVar);
        int mergedDepth = 0;
        while (states.hasNext()) {
            TSPTWState current = states.next();
            //The merged position is the union of all the position
            switch (current.position()) {
                case TSPNode(int value) -> mergedPos.add(value);
                case VirtualNodes(Set<Integer> nodes) -> mergedPos.addAll(nodes);
            }
            // The merged must is the intersection of all must set
            mergedMust.and(current.mustVisit());
            // The merged possibly is the union of the all the must sets and all the possibly sets
            mergedPossibly.or(current.mustVisit());
            mergedPossibly.or(current.possiblyVisit());
            // The arrival time of the merged node is the min of all the arrival times
            mergedTime = Integer.min(mergedTime, current.time());
            mergedDepth = current.depth();
        }
        // We exclude the intersection of the must from the merged possibly
        mergedPossibly.andNot(mergedMust);

        return new TSPTWState(new VirtualNodes(mergedPos), mergedTime, mergedMust, mergedPossibly, mergedDepth);
    }

    @Override
    public double relaxEdge(TSPTWState from, TSPTWState to, TSPTWState merged, Decision d, double cost) {
        return cost;
    }

}
