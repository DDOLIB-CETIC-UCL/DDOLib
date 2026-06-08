package org.ddolib.examples.pdptw;

import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.mdd.NodeSubProblem;
import org.ddolib.modeling.StateRanking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PDPTWReductionStrategy implements ReductionStrategy<PDPTWState> {

    private PDPTWProblem problem;
    private NodeSubroblemComparator<PDPTWState> comparator;

    public PDPTWReductionStrategy(PDPTWProblem problem){
        this.problem = problem;
        this.comparator = new NodeSubroblemComparator<>(new PDPTWRanking());
    }

    @Override
    public List<NodeSubProblem<PDPTWState>>[] defineClusters(List<NodeSubProblem<PDPTWState>> layer, int maxWidth) {
        //save the best nodes based on their past history into singleton clusters
        //cluster the other according to their currentNode

        int nbPossibleCurrent = problem.n;
        int nbNonMergedStates = maxWidth - nbPossibleCurrent;

        layer.sort(comparator);
        int nbClusters = Math.min(layer.size(), maxWidth);
        List<NodeSubProblem<PDPTWState>>[] cluster = new List[nbClusters];
        // create the clusters
        for (int i = 0; i < maxWidth; i++) {
            cluster[i] = new ArrayList<>(1);
        }

        //put the best states into singleton clusters
        for (int i = 0; i < nbNonMergedStates; i++) {
            cluster[i].add(layer.get(i));
        }

        // put the rest in clusters by currentNodes
        for (int i = nbNonMergedStates; i < maxWidth; i++) {
            NodeSubProblem<PDPTWState> subProblem =   layer.get(i);
            int currentNode = subProblem.state.current.nextSetBit(0);
            cluster[currentNode].add(subProblem);
        }

        return cluster;
    }

    /**
     * This utility class implements a decorator pattern to sort NodeSubProblems by their value then state
     */
    private static final class NodeSubroblemComparator<T> implements Comparator<NodeSubProblem<T>> {
        /**
         * This is the decorated ranking
         */
        private final StateRanking<T> delegate;

        /**
         * Creates a new instance
         *
         * @param delegate the decorated ranking
         */
        public NodeSubroblemComparator(final StateRanking<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(NodeSubProblem<T> o1, NodeSubProblem<T> o2) {
            double cmp = o1.getValue() - o2.getValue();
            if (cmp == 0 && delegate != null) {
                return delegate.compare(o1.state, o2.state);
            } else {
                return Double.compare(o1.getValue(), o2.getValue());
            }
        }
    }
}
