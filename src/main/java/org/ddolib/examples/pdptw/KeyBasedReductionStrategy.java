package org.ddolib.examples.pdptw;

import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.mdd.NodeSubProblem;
import org.ddolib.modeling.StateRanking;

import java.util.*;

abstract class KeyBasedReductionStrategy<T> implements ReductionStrategy<T> {

    abstract Object getKey(T state);

    public KeyBasedReductionStrategy(){
    }

    @Override
    public List<NodeSubProblem<T>>[] defineClusters(List<NodeSubProblem<T>> layer, int maxWidth) {

        HashMap<Object,List<NodeSubProblem<T>>> clusterDef = new HashMap<Object,List<NodeSubProblem<T>>>();

        int layerWidth = layer.size();

        layer.sort(new NodeSubroblemComparator<>());

        int nbRemainingNodes = layerWidth;

        Iterator<NodeSubProblem<T>> layerIterator = layer.iterator();

        while(layerIterator.hasNext() && clusterDef.size() + nbRemainingNodes < maxWidth){
            NodeSubProblem<T> current = layerIterator.next();
            Object key = getKey(current.state);
            clusterDef.computeIfAbsent(key, k -> new ArrayList<>()).add(current);
            nbRemainingNodes --;
        }

        int nbClusters = clusterDef.size() + nbRemainingNodes;

        List<NodeSubProblem<T>>[] cluster = new List[nbClusters];

        // create the clusters
        int nextCluster = 0;
        while(layerIterator.hasNext()){
            cluster[nextCluster] = new ArrayList<>(1);
            cluster[nextCluster].add(layerIterator.next());
            nextCluster++;
        }

        for(List<NodeSubProblem<T>> x : clusterDef.values()){
            cluster[nextCluster] = x;
            nextCluster++;
        }

        return cluster;
    }

    /**
     * This utility class implements a decorator pattern to sort NodeSubProblems by their value then state
     */
    private static final class NodeSubroblemComparator<T> implements Comparator<NodeSubProblem<T>> {
        public NodeSubroblemComparator(){
        }

        @Override
        public int compare(NodeSubProblem<T> o1, NodeSubProblem<T> o2) {
            return -Double.compare(o1.getValue(),o2.getValue());
        }
    }
}
