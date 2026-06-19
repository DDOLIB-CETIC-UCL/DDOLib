package org.ddolib.solving.ddo.core.heuristics.cluster.nolayer;

import java.util.List;

public interface ReductionStrategy<T> {
    List<List<T>> defineClusters(List<T> states, List<Double> values, int maxWidth);
}
