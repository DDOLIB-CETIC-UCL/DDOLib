package org.ddolib.ddo.examples.pigmentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.modeling.Relaxation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.ddolib.ddo.examples.pigmentscheduling.PSProblem.IDLE;

public class PSRelax implements Relaxation<PSState> {


    PSInstance instance;

    public PSRelax(PSInstance instance) {
        this.instance = instance;
    }

    // Set of item types that have an unsatisfied demand,
    // plus the next item type produced if any
    private static Set<Integer> members(PSState state) {
        Set<Integer> mem = new HashSet<>();
        for (int i = 0; i < state.previousDemands.length; i++) {
            if (state.previousDemands[i] >= 0) {
                mem.add(i);
            }
        }
        if (state.next != -1) {
            mem.add(state.next);
        }
        return mem;
    }

    @Override
    public PSState mergeStates(final Iterator<PSState> states) {
        PSState currState = states.next();
        int[] prevDemands = Arrays.copyOf(currState.previousDemands, currState.previousDemands.length);
        int time = currState.t;
        while (states.hasNext()) {
            PSState state = states.next();
            time = Math.min(time, state.t);
            for (int i = 0; i < prevDemands.length; i++) {
                prevDemands[i] = Math.min(prevDemands[i], state.previousDemands[i]);
            }
        }
        return new PSState(time, IDLE, prevDemands);

    }


    @Override
    public double relaxEdge(PSState from, PSState to, PSState merged, Decision d, double cost) {
        return cost;
    }

    private long[] computeMST(int[][] changeover) {
        int n = changeover.length;
        long[] minEdge = new long[n];
        boolean[] inMST = new boolean[n];
        Arrays.fill(minEdge, Long.MAX_VALUE);
        minEdge[0] = 0; // Start from the first item
        long[] mstCost = new long[1 << n]; // To store the MST cost for each subset of nodes
        for (int i = 0; i < n; i++) {
            int u = -1;
            for (int j = 0; j < n; j++) {
                if (!inMST[j] && (u == -1 || minEdge[j] < minEdge[u])) {
                    u = j;
                }
            }
            inMST[u] = true;
            for (int v = 0; v < n; v++) {
                if (changeover[u][v] < minEdge[v]) {
                    minEdge[v] = changeover[u][v];
                }
            }
            // Update the MST cost for the current subset
            for (int mask = 0; mask < (1 << n); mask++) {
                if ((mask & (1 << u)) == 0) {
                    mstCost[mask | (1 << u)] = Math.min(mstCost[mask | (1 << u)], mstCost[mask] + minEdge[u]);
                }
            }
        }
        return mstCost;
    }

}