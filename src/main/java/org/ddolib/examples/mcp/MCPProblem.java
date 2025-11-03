package org.ddolib.examples.mcp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Math.abs;

/**
 * Represents an instance of the <b>Maximum Cut Problem (MCP)</b>.
 * <p>
 * In the MCP, given a weighted undirected graph, the goal is to partition the nodes
 * into two sets (S and T) such that the sum of the weights of edges between the sets
 * is maximized.
 * </p>
 *
 * <p>
 * This class implements the {@link Problem} interface and provides methods for:
 * </p>
 * <ul>
 *     <li>Initializing the problem from a graph or a file.</li>
 *     <li>Defining the decision domain (which partition a node belongs to).</li>
 *     <li>Computing transitions and transition costs between states.</li>
 *     <li>Accessing the initial state and value, and optional optimal value.</li>
 * </ul>
 *
 * <p>
 * Constants {@link #S} and {@link #T} are used to model decisions: placing a node in
 * partition S or T, respectively.
 * </p>
 *
 * @see Graph
 * @see MCPState
 * @see Decision
 */

public class MCPProblem implements Problem<MCPState> {

    /**
     * Constant to model decision "put in partition {@code S}".
     */
    public final int S = 0;

    /**
     * Constant to model decision "put in partition {@code T}".
     */
    public final int T = 1;

    /**
     * The underlying graph representing the instance.
     */
    final Graph graph;

    /**
     * Optional known optimal value for the problem instance.
     */
    public Optional<Double> optimal = Optional.empty();

    /**
     * Optional name for the instance to ease readability in tests.
     */
    private Optional<String> name = Optional.empty();

    /**
     * Constructs an MCP problem from a given graph.
     *
     * @param graph the graph representing the instance
     */

    public MCPProblem(Graph graph) {
        this.graph = graph;
    }

    /**
     * Constructs an MCP problem from a given graph and known optimal value.
     *
     * @param graph   the graph representing the instance
     * @param optimal the known optimal value of the instance
     */
    public MCPProblem(Graph graph, Double optimal) {
        this.graph = graph;
        this.optimal = Optional.of(optimal);
    }

    /**
     * Constructs an MCP problem by reading an instance from a file.
     * <p>
     * The file should contain the number of nodes and the adjacency matrix of weights,
     * optionally including the optimal solution value.
     * </p>
     *
     * @param fname the path to the file containing the instance
     * @throws IOException if the file cannot be read
     */
    public MCPProblem(String fname) throws IOException {
        int[][] matrix = new int[0][0];
        Optional<Double> optimal = Optional.empty();

        try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
            int linesCount = 0;
            int skip = 0;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    skip++;
                } else if (linesCount == 0) {
                    String[] tokens = line.split("\\s+");
                    int n = Integer.parseInt(tokens[1]);
                    matrix = new int[n][n];
                    if (tokens.length >= 4) {
                        optimal = Optional.of(Double.parseDouble(tokens[3]));
                    }
                } else {
                    int node = linesCount - skip - 1;
                    String[] tokens = line.split("\\s+");
                    int[] row = Arrays.stream(tokens).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();
                    matrix[node] = row;
                }
                linesCount++;
            }
        }
        Graph g = new Graph(matrix);
        this.graph = g;
        this.optimal = optimal;
        this.name = Optional.of(fname);
    }

    /**
     * Sets a human-readable name for this problem instance.
     *
     * @param name the name to assign
     */
    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public String toString() {
        return name.orElseGet(graph::toString);
    }

    @Override
    public int nbVars() {
        return graph.numNodes;
    }

    @Override
    public MCPState initialState() {
        return new MCPState(new ArrayList<>(Collections.nCopies(nbVars(), 0)), 0);
    }

    @Override
    public double initialValue() {
        int val = 0;
        for (int i = 0; i < nbVars(); i++) {
            for (int j = i + 1; j < nbVars(); j++) {
                val += negativeOrNull(graph.weightOf(i, j));
            }
        }
        return -val;
    }

    @Override
    public Iterator<Integer> domain(MCPState state, int var) {
        // The first node can be arbitrary put in S
        if (state.depth() == 0) return List.of(S).iterator();
        else return List.of(S, T).iterator();
    }

    @Override
    public MCPState transition(MCPState state, Decision decision) {
        ArrayList<Integer> newBenefits = new ArrayList<>(Collections.nCopies(nbVars(), 0));
        int k = decision.var();
        if (decision.val() == S) {
            for (int l = k + 1; l < nbVars(); l++) {
                // If k is put in S, and then l is put in T, we gain the weight of the edge k -- l
                int benef = state.netBenefit().get(l) + graph.weightOf(k, l);
                newBenefits.set(l, benef);
            }
        } else {
            for (int l = k + 1; l < nbVars(); l++) {
                // If k is put in T, and then l is also put in T, we lose the weight of the edge k -- l
                int benef = state.netBenefit().get(l) - graph.weightOf(k, l);
                newBenefits.set(l, benef);
            }
        }
        return new MCPState(newBenefits, state.depth() + 1);
    }

    @Override
    public double transitionCost(MCPState state, Decision decision) {
        if (state.depth() == 0) return 0;
        else if (decision.val() == S) return -branchOnS(state, decision.var());
        else return -branchOnT(state, decision.var());
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal.map(x -> -x);
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars()));
        }

        BitSet s = new BitSet(nbVars());
        BitSet t = new BitSet(nbVars());

        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == 0) s.set(i);
            else t.set(i);
        }

        double value = 0.0;
        for (int i = s.nextSetBit(0); i >= 0; i = s.nextSetBit(i + 1)) {
            for (int j = t.nextSetBit(0); j >= 0; j = t.nextSetBit(j + 1)) {
                value += graph.weightOf(i, j);
            }
        }

        return -value;
    }

    /**
     * Computes the transition cost when placing node {@code k} in partition S.
     *
     * @param state the current state
     * @param k     the node index
     * @return the estimated cost
     */
    private int branchOnS(MCPState state, int k) {
        // If k is set to S, we gain the net benefit if it is < 0
        int cost = positiveOrNull(-state.netBenefit().get(k));

        for (int l = k + 1; l < nbVars(); l++) {
            int skl = state.netBenefit().get(l);
            int wkl = graph.weightOf(k, l);

            if (skl * wkl <= 0) cost += min(abs(skl), abs(wkl));
        }

        return cost;
    }

    /**
     * Computes the transition cost when placing node {@code k} in partition T.
     *
     * @param state the current state
     * @param k     the node index
     * @return the estimated cost
     */
    private int branchOnT(MCPState state, int k) {
        // If k is set to T, we gain the net benefit if it is > 0
        int cost = positiveOrNull(state.netBenefit().get(k));

        for (int l = k + 1; l < nbVars(); l++) {
            int skl = state.netBenefit().get(l);
            int wkl = graph.weightOf(k, l);

            if (skl * wkl >= 0) cost += min(abs(skl), abs(wkl));
        }

        return cost;
    }

    /**
     * Returns the positive part of a value, or zero if negative.
     *
     * @param x the value
     * @return max(x, 0)
     */
    private int positiveOrNull(int x) {
        return max(x, 0);
    }

    /**
     * Returns the negative part of a value, or zero if positive.
     *
     * @param x the value
     * @return min(x, 0)
     */

    private int negativeOrNull(int x) {
        return min(x, 0);
    }
}
