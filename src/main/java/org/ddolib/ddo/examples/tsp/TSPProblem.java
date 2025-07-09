package org.ddolib.ddo.examples.tsp;


import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.modeling.Problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

public class TSPProblem implements Problem<TSPState> {

    final int n;
    final double[][] distanceMatrix;

    @Override
    public String toString() {
        return "TSP(n:" + n + "\n" +
                "\t" + Arrays.stream(distanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList() + "\n)";
    }

    public double eval(int[] solution) {
        double toReturn = 0;
        for (int i = 1; i < solution.length; i++) {
            toReturn = toReturn + distanceMatrix[solution[i - 1]][solution[i]];
        }
        toReturn = toReturn + distanceMatrix[solution[solution.length - 1]][0]; //final come back
        return toReturn;
    }

    public TSPProblem(final double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
    }

    @Override
    public int nbVars() {
        return n; //the last decision will be to come back to point zero
    }

    @Override
    public TSPState initialState() {
        BitSet toVisit = new BitSet(n);
        toVisit.set(1, n);

        return new TSPState(singleton(0), toVisit);
    }

    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(n);
        toReturn.set(singletonValue);
        return toReturn;
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(TSPState state, int var) {
        if (var == n - 1) {
            //the final decision is to come back to node zero
            return singleton(0).stream().iterator();
        } else {
            ArrayList<Integer> domain = new ArrayList<>(state.toVisit.stream().boxed().toList());
            return domain.iterator();
        }
    }

    @Override
    public TSPState transition(TSPState state, Decision decision) {
        int node = decision.val();


        BitSet newToVisit = (BitSet) state.toVisit.clone();
        newToVisit.clear(node);

        return new TSPState(state.singleton(node), newToVisit);
    }

    @Override
    public double transitionCost(TSPState state, Decision decision) {
        return -state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                .mapToDouble(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsDouble();
    }
}
