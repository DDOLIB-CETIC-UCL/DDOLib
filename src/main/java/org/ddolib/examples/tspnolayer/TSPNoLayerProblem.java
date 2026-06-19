package org.ddolib.examples.tspnolayer;

import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.nolayer.NoLayerProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class TSPNoLayerProblem implements NoLayerProblem<TSPNoLayerState> {

    public final double[][] distanceMatrix;
    private final int n;

    public TSPNoLayerProblem(final double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
    }

    @Override
    public TSPNoLayerState initialState() {
        BitSet toVisit = new BitSet(n);
        toVisit.set(1, n);
        return new TSPNoLayerState(singleton(0), toVisit);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public boolean isTarget(TSPNoLayerState state) {
        // Target is reached when no more cities to visit and current is 0
        return state.toVisit.isEmpty() && state.current.get(0);
    }

    @Override
    public Iterator<Integer> domain(TSPNoLayerState state) {
        if (state.toVisit.isEmpty()) {
            if (!state.current.get(0)) {
                return singleton(0).stream().iterator();
            }
            return new ArrayList<Integer>().iterator(); // Already at target
        } else {
            ArrayList<Integer> domain = new ArrayList<>(state.toVisit.stream().boxed().toList());
            return domain.iterator();
        }
    }

    @Override
    public TSPNoLayerState transition(TSPNoLayerState state, int label) {
        BitSet newToVisit = (BitSet) state.toVisit.clone();
        newToVisit.clear(label);
        return new TSPNoLayerState(singleton(label), newToVisit);
    }

    @Override
    public double transitionCost(TSPNoLayerState state, int label) {
        return state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != label)
                .mapToDouble(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][label])
                .min()
                .orElse(0.0);
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != n) {
            throw new InvalidSolutionException(String.format("The solution %s does not match " +
                    "the number %d variables", Arrays.toString(solution), n));
        }

        Map<Integer, Long> count = Arrays.stream(solution)
                .boxed()
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        if (count.values().stream().anyMatch(x -> x != 1)) {
            String msg = "The solution has duplicated nodes and does not reach each node exactly once";
            throw new InvalidSolutionException(msg);
        }

        if (solution[n - 1] != 0) {
            throw new InvalidSolutionException("The solution does not return to the depot (node 0)");
        }

        double value = distanceMatrix[0][solution[0]];
        for (int i = 1; i < n; i++) {
            value += distanceMatrix[solution[i - 1]][solution[i]];
        }

        return value;
    }

    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(n);
        toReturn.set(singletonValue);
        return toReturn;
    }

    @Override
    public String toString() {
        return "TSPNoLayer(n:" + n + ")";
    }
}
