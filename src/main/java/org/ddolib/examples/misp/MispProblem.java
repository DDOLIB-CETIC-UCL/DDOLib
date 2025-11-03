package org.ddolib.examples.misp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Represents an instance of the Maximum Independent Set Problem (MISP) as a {@link Problem}.
 * <p>
 * The problem is defined on a weighted undirected graph. Each node can either be included
 * in the independent set or not, and selected nodes cannot be adjacent.
 * </p>
 * <p>
 * The state of the problem is represented by a {@link BitSet} indicating which nodes
 * can still be selected. The solver explores decisions for each node to build an
 * independent set of maximum weight.
 * </p>
 */
public class MispProblem implements Problem<BitSet> {

    /**
     * The remaining nodes that can be selected in the current independent set.
     * Considered as the state of the decision diagram.
     */
    public final BitSet remainingNodes;

    /**
     * For each node {@code i}, {@code neighbors[i]} contains the adjacency list of {@code i}.
     */
    public final BitSet[] neighbors;

    /**
     * For each node {@code i}, {@code weight[i]} contains the weight associated with {@code i}.
     */
    public final int[] weight;

    /**
     * Optional value of the optimal solution, if known.
     */
    private Optional<Double> optimal = Optional.empty();

    /**
     * Optional name for readability of tests and outputs.
     */
    private Optional<String> name = Optional.empty();

    /**
     * Constructs a MISP problem with a given state, adjacency lists, weights, and known optimal value.
     *
     * @param remainingNodes the initial set of selectable nodes
     * @param neighbors      adjacency lists for each node
     * @param weight         weights of each node
     * @param optimal        known optimal solution value
     */
    public MispProblem(BitSet remainingNodes, BitSet[] neighbors, int[] weight, double optimal) {
        this.remainingNodes = remainingNodes;
        this.neighbors = neighbors;
        this.weight = weight;
        this.optimal = Optional.of(optimal);
    }

    /**
     * Constructs a MISP problem with a given state, adjacency lists, and weights.
     * The optimal solution is unknown.
     *
     * @param remainingNodes the initial set of selectable nodes
     * @param neighbors      adjacency lists for each node
     * @param weight         weights of each node
     */
    public MispProblem(BitSet remainingNodes, BitSet[] neighbors, int[] weight) {
        this.remainingNodes = remainingNodes;
        this.neighbors = neighbors;
        this.weight = weight;
    }


    /**
     * Loads a MISP problem from a DOT file.
     * <p>
     * The file must contain the list of nodes and edges. Node weights can be specified
     * with {@code [weight=w]} (default weight is 1). If known, the optimal solution
     * should be specified in the second line as {@code optimal=x}.
     * </p>
     *
     * @param fname path to the DOT file describing the graph
     * @throws IOException if an error occurs while reading the file
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

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars()));
        }

        List<Integer> independentSet = new ArrayList<>();
        int value = 0;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == 1) {
                independentSet.add(i);
                value += weight[i];
            }
        }

        for (int i = 0; i < independentSet.size(); i++) {
            for (int j = i + 1; j < independentSet.size(); j++) {
                int from = independentSet.get(i);
                int to = independentSet.get(j);
                if (neighbors[from].get(to)) {
                    String msg = String.format("The solution %s is not an independant set. Nodes " +
                            "%d and %d are adjacent", independentSet, from, to);
                    throw new InvalidSolutionException(msg);
                }
            }
        }

        return value;
    }
}