package org.ddolib.nolayer.solving.ddo.core.heuristics.cluster;

import java.util.List;

public interface ReductionStrategy<T> {
    List<List<T>> defineClusters(List<T> states, List<Double> values, int maxWidth);
}
