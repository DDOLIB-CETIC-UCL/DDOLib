package org.ddolib.ddo.examples.misp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class MispProblem implements Problem<BitSet> {

    public final BitSet remainingNodes;
    public final BitSet[] neighbors;
    public final int[] weight;
    public final Optional<Integer> optimal;

    /**
     * @param remainingNodes The remaining node that can be selected in the current independent set. Considered
     *                       as the state of the MDD.
     * @param neighbors      For each node {@code i}, {@code neighbors[i]} returns the adjacency list of {@code i}.
     * @param weight         For each node {@code i}, {@code weight[i]} returns the weight associated to {@code i}
     *                       in the problem instance.
     */
    public MispProblem(BitSet remainingNodes, BitSet[] neighbors, int[] weight, Optional<Integer> optimal) {
        this.remainingNodes = remainingNodes;
        this.neighbors = neighbors;
        this.weight = weight;
        this.optimal = optimal;
    }

    public MispProblem(BitSet remainingNodes, BitSet[] neighbors, int[] weight) {
        this.remainingNodes = remainingNodes;
        this.neighbors = neighbors;
        this.weight = weight;
        this.optimal = Optional.empty();
    }

    @Override
    public String toString() {
        StringBuilder weighStr = new StringBuilder();
        for (int i = 0; i < weight.length; i++) {
            weighStr.append(String.format("\t%d : %d%n", i, weight[i]));
        }

        StringBuilder neighStr = new StringBuilder();
        for (int i = 0; i < neighbors.length; i++) {
            neighStr.append(String.format("\t%d : %s%n", i, neighbors[i]));
        }

        return String.format("Remaining nodes: %s%nWeight: %n%s%nNeighbors: %n%s%n", remainingNodes.toString(),
                weighStr, neighStr);
    }

    @Override
    public int nbVars() {
        return weight.length;
    }

    @Override
    public BitSet initialState() {
        return remainingNodes;
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(BitSet state, int var) {
        if (state.get(var)) {
            // The node can be selected or not
            return List.of(0, 1).iterator();
        } else {
            // The node cannot be selected
            return List.of(0).iterator();
        }
    }

    @Override
    public BitSet transition(BitSet state, Decision decision) {
        var res = (BitSet) state.clone();
        // When we are selecting a node, we cannot select this node and its neighbors
        if (decision.val() == 1) {
            res.andNot(neighbors[decision.var()]);
        }
        res.set(decision.var(), false);

        return res;
    }

    @Override
    public int transitionCost(BitSet state, Decision decision) {
        return weight[decision.var()] * decision.val();
    }
}