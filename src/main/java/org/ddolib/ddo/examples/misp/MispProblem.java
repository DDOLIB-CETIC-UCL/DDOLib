package org.ddolib.ddo.examples.misp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.modeling.Problem;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class MispProblem implements Problem<BitSet> {

    /**
     * The remaining node that can be selected in the current independent set. Considered
     * as the state of the MDD.
     */
    public final BitSet remainingNodes;
    /**
     * For each node {@code i}, {@code neighbors[i]} returns the adjacency list of {@code i}.
     */
    public final BitSet[] neighbors;
    /**
     * For each node {@code i}, {@code weight[i]} returns the weight associated to {@code i}
     * in the problem instance.
     */
    public final int[] weight;

    /**
     * The value of the optimal solution if known.
     */
    public Optional<Double> optimal = Optional.empty();


    /**
     * String to ease the tests' readability.
     */
    private Optional<String> name = Optional.empty();

    /**
     * @param remainingNodes The remaining node that can be selected in the current independent set. Considered
     *                       as the state of the MDD.
     * @param neighbors      For each node {@code i}, {@code neighbors[i]} returns the adjacency list of {@code i}.
     * @param weight         For each node {@code i}, {@code weight[i]} returns the weight associated to {@code i}
     *                       in the problem instance.
     */
    public MispProblem(BitSet remainingNodes, BitSet[] neighbors, int[] weight, double optimal) {
        this.remainingNodes = remainingNodes;
        this.neighbors = neighbors;
        this.weight = weight;
        this.optimal = Optional.of(optimal);
    }

    public MispProblem(BitSet remainingNodes, BitSet[] neighbors, int[] weight) {
        this.remainingNodes = remainingNodes;
        this.neighbors = neighbors;
        this.weight = weight;
    }

    /**
     * Sets the name of the instance. The name will override the default toString.
     *
     * @param name The new string that will override the default toString.
     */
    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public String toString() {
        if (name.isPresent()) {
            return name.get();
        } else {
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
    public double initialValue() {
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
    public double transitionCost(BitSet state, Decision decision) {
        return weight[decision.var()] * decision.val();
    }
}