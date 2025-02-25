package org.ddolib.ddo.examples.TSPTW;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TSPTWRelax implements Relaxation<TSPTWState> {

    private final int numVar;

    public TSPTWRelax(int numVar) {
        this.numVar = numVar;
    }

    @Override
    public TSPTWState mergeStates(Iterator<TSPTWState> states) {
        Set<Integer> mergedPos = new HashSet<>();
        int mergedTime = Integer.MAX_VALUE;
        BitSet mergedMust = new BitSet(numVar);
        mergedMust.set(0, numVar, true);
        BitSet mergedPossibly = new BitSet(numVar);
        int mergedDepth = 0;
        while (states.hasNext()) {
            TSPTWState current = states.next();
            switch (current.position()) {
                case TSPNode(int value) -> mergedPos.add(value);
                case Virtual(Set<Integer> nodes) -> mergedPos.addAll(nodes);
            }
            mergedMust.and(current.mustVisit());
            mergedPossibly.or(current.mustVisit());
            mergedPossibly.or(current.possiblyVisit());
            mergedTime = Integer.min(mergedTime, current.time());
            mergedDepth = current.depth();
        }
        mergedPossibly.andNot(mergedMust);

        return new TSPTWState(new Virtual(mergedPos), mergedTime, mergedMust, mergedPossibly, mergedDepth);
    }

    @Override
    public int relaxEdge(TSPTWState from, TSPTWState to, TSPTWState merged, Decision d, int cost) {
        return cost;
    }
}
