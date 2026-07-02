package org.ddolib.nolayer.solving.ddo.core.heuristics.cluster;

import org.ddolib.layered.modeling.StateRanking;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CostBased<T> implements ReductionStrategy<T> {

    private final StateRanking<T> ranking;

    public CostBased(final StateRanking<T> ranking) {
        this.ranking = ranking;
    }

    @Override
    public List<List<T>> defineClusters(List<T> states, List<Double> values, int maxWidth) {
        List<Integer> indices = IntStream.range(0, states.size()).boxed().collect(Collectors.toList());
        indices.sort((i1, i2) -> {
            double cmp = values.get(i1) - values.get(i2);
            if (cmp == 0 && ranking != null) {
                return ranking.compare(states.get(i1), states.get(i2));
            }
            return Double.compare(values.get(i1), values.get(i2));
        });

        int nbClusters = Math.min(states.size(), maxWidth);
        List<List<T>> cluster = new ArrayList<>();

        for (int i = 0; i < nbClusters - 1; i++) {
            List<T> cl = new ArrayList<>();
            cl.add(states.get(indices.get(i)));
            cluster.add(cl);
        }

        List<T> lastCl = new ArrayList<>();
        for (int i = nbClusters - 1; i < states.size(); i++) {
            lastCl.add(states.get(indices.get(i)));
        }
        if (!lastCl.isEmpty()) cluster.add(lastCl);

        return cluster;
    }
}
