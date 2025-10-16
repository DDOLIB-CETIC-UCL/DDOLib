package org.ddolib.examples.pigmentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;
import org.ddolib.util.io.InputReader;

import java.util.*;
import java.util.stream.IntStream;

public class PSProblem implements Problem<PSState> {

    final int nItems;
    final int horizon;
    // dim = nItems
    final int[] stockingCost; // cost of stocking item i

    // dim = nItems x nItems
    final int[][] changeoverCost; // cost of changing from item i to item j

    // dim = nItems x (horizon+1)
    final int[][] previousDemands; // previousDemands[i][t] = the largest time slot < t where a demand for item i occurs

    // dim = nItems x horizon
    int[][] remainingDemands; // remainingDemands[i][t] = the total demand for item i on [0..t]

    private Optional<Double> optimal; // optimal objective value

    public PSProblem(final int nItems, final int horizon, final int[] stockingCost, final int[][] changeoverCost, final int[][] previousDemands, final Optional<Double> optimal) {
        this.nItems = nItems;
        this.horizon = horizon;
        this.stockingCost = stockingCost;
        this.changeoverCost = changeoverCost;
        this.previousDemands = previousDemands;
        this.optimal = optimal;
    }

    public PSProblem(final int nItems, final int horizon, final int[] stockingCost, final int[][] changeoverCost, final int[][] previousDemands) {
        this.nItems = nItems;
        this.horizon = horizon;
        this.stockingCost = stockingCost;
        this.changeoverCost = changeoverCost;
        this.previousDemands = previousDemands;
        this.optimal = Optional.empty();
    }

    public PSProblem(final String filename) {
        InputReader reader = new InputReader(filename);
        horizon = reader.getInt();
        nItems = reader.getInt();
        int nOrders = reader.getInt();

        changeoverCost = new int[nItems][nItems];
        for (int i = 0; i < nItems; i++) {
            for (int j = 0; j < nItems; j++) {
                changeoverCost[i][j] = reader.getInt();
            }
        }

        stockingCost = new int[nItems];
        for (int i = 0; i < nItems; i++) {
            stockingCost[i] = reader.getInt();
        }

        int[][] demands = new int[nItems][horizon];
        for (int i = 0; i < nItems; i++) {
            for (int j = 0; j < horizon; j++) {
                demands[i][j] = reader.getInt();
            }
        }

        optimal = Optional.of((double) reader.getInt());

        previousDemands = new int[nItems][horizon + 1];
        for (int i = 0; i < nItems; i++) {
            Arrays.fill(previousDemands[i], -1);
        }
        for (int t = 1; t <= horizon; t++) {
            for (int i = 0; i < nItems; i++) {
                if (demands[i][t - 1] > 0) {
                    previousDemands[i][t] = t - 1;
                } else {
                    previousDemands[i][t] = previousDemands[i][t - 1];
                }
            }
        }
        remainingDemands = new int[nItems][horizon];
        for (int i = 0; i < nItems; i++) {
            for (int t = 0; t < horizon; t++) {
                if (t == 0) {
                    remainingDemands[i][t] = demands[i][t];
                } else {
                    remainingDemands[i][t] = remainingDemands[i][t - 1] + demands[i][t];
                }
            }
        }
    }






    public static final int IDLE = -1; // represent the idle state of the machine i.e. no production








//    public final PSInstance instance;

    private Optional<String> name = Optional.empty();

//    public PSProblem(PSInstance instance) {
//        this.instance = instance;
//    }

    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public String toString() {
        return name.orElse(super.toString());
    }

    @Override
    public int nbVars() {
        return horizon;
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public PSState initialState() {
        int[] prevDemands = new int[nItems];
        for (int i = 0; i < nItems; i++) {
            prevDemands[i] = previousDemands[i][horizon];
        }
        return new PSState(horizon, IDLE, prevDemands);
    }

    /**
     * @param state the state from which the transitions should be applicable
     * @param depth the variable whose domain in being queried
     * @return
     */
    @Override
    public Iterator<Integer> domain(PSState state, int depth) {

        int t = horizon - depth - 1;
        IntStream dom = IntStream.range(0, nItems)
                .filter(i -> state.previousDemands[i] >= t);

        int[] dom2 = IntStream.range(0, nItems)
                .filter(i -> state.previousDemands[i] >= t).toArray();


        // total number of remaining demands <= t
        int remDemands = IntStream.range(0, nItems)
                .filter(i -> state.previousDemands[i] >= 0)
                .map(i -> remainingDemands[i][state.previousDemands[i]])
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
            ret.previousDemands[item] = previousDemands[item][state.previousDemands[item]];
        }
        return ret;
    }

    @Override
    public double transitionCost(PSState state, Decision decision) {
        int item = decision.val();
        if (item == IDLE) {
            return 0;
        } else {
            int t = (horizon - decision.var() - 1);
            int duration = state.previousDemands[item] - t;
            int stocking = stockingCost[item] * duration;
            int changeover = state.next != -1 ? changeoverCost[item][state.next] : 0;
            // stocking cost + changeover cost
            return changeover + stocking;
        }
    }
}

