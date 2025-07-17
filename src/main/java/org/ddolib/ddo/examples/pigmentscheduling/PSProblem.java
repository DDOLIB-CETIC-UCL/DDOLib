package org.ddolib.ddo.examples.pigmentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.IntStream;

public class PSProblem implements Problem<PSState> {

    public static final int IDLE = -1; // represent the idle state of the machine i.e. no production

    private final PSInstance instance;

    public PSProblem(PSInstance instance) {
        this.instance = instance;
    }

    @Override
    public int nbVars() {
        return instance.horizon;
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public PSState initialState() {
        int prevDemands[] = new int[instance.nItems];
        for (int i = 0; i < instance.nItems; i++) {
            prevDemands[i] = instance.previousDemands[i][instance.horizon];
        }
        return new PSState(instance.horizon, IDLE, prevDemands);
    }

    /**
     * @param state the state from which the transitions should be applicable
     * @param depth the variable whose domain in being queried
     * @return
     */
    @Override
    public Iterator<Integer> domain(PSState state, int depth) {

        int t = instance.horizon - depth - 1;
        IntStream dom = IntStream.range(0, instance.nItems)
                .filter(i -> state.previousDemands[i] >= t);

        int[] dom2 = IntStream.range(0, instance.nItems)
                .filter(i -> state.previousDemands[i] >= t).toArray();


        // total number of remaining demands <= t
        int remDemands = IntStream.range(0, instance.nItems)
                .filter(i -> state.previousDemands[i] >= 0)
                .map(i -> instance.remainingDemands[i][state.previousDemands[i]])
                .sum();

        //System.out.println("remDemands: " + remDemands);
        if (remDemands > t + 1) {
            // fail to produce all the remaining demands
            return Collections.emptyIterator();
        }
        if (remDemands < t + 1) {
            // ok to add IDLE, we have enough time to produce all the remaining demands
            return IntStream.concat(dom, IntStream.of(IDLE)).iterator();
        } else {
            // just enough time to produce remaining demands, no IDLE possible
            assert remDemands == t + 1;
            return dom.iterator();
        }
    }

    @Override
    public PSState transition(PSState state, Decision decision) {
        PSState ret = state.clone();
        int item = decision.val();
        if (item != IDLE) {
            ret.next = item;
            ret.previousDemands[item] = instance.previousDemands[item][state.previousDemands[item]];
        }
        return ret;
    }

    @Override
    public double transitionCost(PSState state, Decision decision) {
        int item = decision.val();
        if (item == IDLE) {
            return 0;
        } else {
            int t = (instance.horizon - decision.var() - 1);
            int duration = state.previousDemands[item] - t;
            int stocking = instance.stockingCost[item] * duration;
            int changeover = state.next != -1 ? instance.changeoverCost[item][state.next] : 0;
            // stocking cost + changeover cost
            return -(changeover + stocking);
        }
    }
}

