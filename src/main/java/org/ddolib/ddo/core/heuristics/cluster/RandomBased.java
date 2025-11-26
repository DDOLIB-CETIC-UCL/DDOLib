package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomBased<T> implements ReductionStrategy<T> {

    final private Random rnd;

    public RandomBased(long seed) {
        this.rnd = new Random(seed);
    }

    @Override
    public List<NodeSubProblem<T>>[] defineClusters(List<NodeSubProblem<T>> layer, int maxWidth) {
        List<NodeSubProblem<T>>[] clusters = new List[maxWidth];
        Collections.shuffle(layer, rnd);
        List<NodeSubProblem<T>> kept = layer.subList(0, maxWidth);
        for (int i = 0; i < maxWidth; i++) {
            clusters[i] = new ArrayList<>();
            clusters[i].add(kept.get(i));
        }
        return clusters;
    }

    public void setSeed(long seed) {
        this.rnd.setSeed(seed);
    }
}
