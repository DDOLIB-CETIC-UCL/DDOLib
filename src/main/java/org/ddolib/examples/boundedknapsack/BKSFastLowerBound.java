package org.ddolib.examples.boundedknapsack;

import org.ddolib.modeling.FastLowerBound;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class BKSFastLowerBound implements FastLowerBound<Integer> {
    private final BKSProblem problem;

    public BKSFastLowerBound(BKSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(Integer state, Set<Integer> variables) {
        double[] ratio = new double[problem.nbVars()];
        int capacity = state;
        for (int v : variables) {
            ratio[v] = ((double) problem.instance.values[v] / problem.instance.weights[v]);
        }

        class RatioComparator implements Comparator<Integer> {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Double.compare(ratio[o1], ratio[o2]);
            }
        }

        Integer[] sorted = variables.toArray(new Integer[0]);
        Arrays.sort(sorted, new RatioComparator().reversed());

        int currentSolutionValue = 0;
        int currentTotalWeight = 0;
        Iterator<Integer> itemIterator = Arrays.stream(sorted).iterator();
        while (itemIterator.hasNext()) {
            int item = itemIterator.next();
            if (currentTotalWeight + problem.instance.weights[item] < capacity) {
                int x = Math.min(problem.instance.quantity[item], (capacity - currentTotalWeight)/problem.instance.weights[item]);
                currentSolutionValue += x * problem.instance.weights[item];
                currentSolutionValue += x * problem.instance.values[item];
            }
        }
        return -currentSolutionValue;
    }
}
