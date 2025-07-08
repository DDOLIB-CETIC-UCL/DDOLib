package org.ddolib.ddo.examples.knapsack;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class KSRelax implements Relaxation<Integer>  {
    private final KSProblem problem;
    public KSRelax(KSProblem problem) {this.problem = problem;}

    @Override
    public Integer mergeStates(final Iterator<Integer> states) {
        int capa = 0;
        while (states.hasNext()) {
            final Integer state = states.next();
            capa = Math.max(capa, state);
        }
        return capa;
    }

    @Override
    public double relaxEdge(Integer from, Integer to, Integer merged, Decision d, double cost) {
        return cost;
    }

    @Override
    public double fastUpperBound(Integer state, Set<Integer> variables) {
        double[] ratio = new double[problem.nbVars()];
        int capacity = state;
        for (int v : variables) {
            ratio[v] = ((double) problem.profit[v] / problem.weight[v]);
        }

        class RatioComparator implements Comparator<Integer> {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Double.compare(ratio[o1], ratio[o2]);
            }
        }

        Integer[] sorted = variables.toArray(new Integer[0]);
        Arrays.sort(sorted, new RatioComparator().reversed());
//        System.out.println(Arrays.toString(sorted));
        int maxProfit = 0;
        Iterator<Integer> itemIterator = Arrays.stream(sorted).iterator();
        while (capacity > 0 && itemIterator.hasNext()) {
            int item = itemIterator.next();
            if (capacity >= problem.weight[item]) {
                maxProfit += problem.profit[item];
                capacity -= problem.weight[item];
            } else {
                double itemProfit = ratio[item] * capacity; //problem.profit[item];
                maxProfit += (int) Math.floor(itemProfit);
                capacity = 0;
            }
        }

        return maxProfit;
    }

}
