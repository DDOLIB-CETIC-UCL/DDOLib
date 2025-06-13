package org.ddolib.ddo.examples.tsp;


import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

public class TSPProblem implements Problem<TSPState> {

    final int n;
    final int[][] distanceMatrix;

    @Override
    public String toString() {
        return "TSP(n:" + n + "\n" +
                "\t" + Arrays.stream(distanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList() + "\n)";
    }

    public int eval(int[] solution) {
        int toReturn = 0;
        for (int i = 1; i < solution.length; i++) {
            toReturn = toReturn + distanceMatrix[solution[i - 1]][solution[i]];
        }
        return toReturn;
    }

    public TSPProblem(final int[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
    }

    @Override
    public int nbVars() {
        return n - 1; //since zero is the initial point
    }

    @Override
    public TSPState initialState() {
        System.out.println("init");
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
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(TSPState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>(state.toVisit.stream().boxed().toList());
        return domain.iterator();
    }

    @Override
    public TSPState transition(TSPState state, Decision decision) {
        return state.goTo(decision.val());
    }

    @Override
    public int transitionCost(TSPState state, Decision decision) {
        return -state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                .map(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsInt();
    }
}
