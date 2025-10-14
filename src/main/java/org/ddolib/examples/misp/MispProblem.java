package org.ddolib.examples.misp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
    private Optional<Double> optimal = Optional.empty();


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
     * Creates an instance of Maximum Independent Set Problem from a .dot file.
     * If given, the expected value of the optimal solution {@code x} of the problem must in the second line written as
     * {@code optimal=x}.
     * <p>
     * To be correctly read, the file must contain first the list of the nodes and then the edges. If given the
     * optimal value must be written before the nodes.
     * <p>
     * Each node can have a weight {@code w} with by adding the parameter {@code [weight=w]}.
     * By default, of the node is {@code 1}.
     *
     * @param fname A .dot file containing a graph.
     */
    public MispProblem(String fname) throws IOException {
        ArrayList<Integer> weight = new ArrayList<>();
        BitSet[] neighbor;
        Optional<Double> optimal = Optional.empty();
        int n;
        try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null && !line.contains("--")) {
                if (line.isEmpty()) continue;

                if (line.contains("optimal")) {
                    String optiStr = line.replace(";", "");
                    String[] tokens = optiStr.split("=");
                    optimal = Optional.of(Double.parseDouble(tokens[1]));
                } else if (line.contains("weight")) {
                    String w = line.trim().split(" ")[1];
                    w = w.replace("[weight=", "").replace("];", "");
                    weight.add(Integer.parseInt(w));
                } else {
                    weight.add(1);
                }
            }
            n = weight.size();
            neighbor = new BitSet[n];
            Arrays.setAll(neighbor, i -> new BitSet(n));
            while (line != null && !line.equals("}")) {
                if (line.isEmpty()) {
                    line = br.readLine();
                    continue;
                }
                String[] tokens = line.replace(" ", "").replace(";", "").split("--");
                int source = Integer.parseInt(tokens[0]) - 1;
                int target = Integer.parseInt(tokens[1]) - 1;
                neighbor[source].set(target);
                neighbor[target].set(source);
                line = br.readLine();
            }
        }
        BitSet initialState = new BitSet(n);
        initialState.set(0, n, true);

        this.remainingNodes = initialState;
        this.neighbors = neighbor;
        this.weight = weight.stream().mapToInt(x -> x).toArray();
        this.optimal = optimal;
        this.name = Optional.of(fname);
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
    public Optional<Double> optimalValue() {
        return optimal.map(x -> -x);
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
        return -weight[decision.var()] * decision.val();
    }
}