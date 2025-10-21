package org.ddolib.examples.mcp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Math.abs;


public class MCPProblem implements Problem<MCPState> {

    /**
     * Constant to model decision "put in partition {@code S}"
     */
    public final int S = 0;
    /**
     * Constant to model decision "put in partition {@code T}"
     */
    public final int T = 1;

    final Graph graph;
    public Optional<Double> optimal = Optional.empty();

    /**
     * A name to ease the readability of the tests.
     */
    private Optional<String> name = Optional.empty();

    public MCPProblem(Graph graph) {
        this.graph = graph;
    }

    public MCPProblem(Graph graph, Double optimal) {
        this.graph = graph;
        this.optimal = Optional.of(optimal);
    }

    /**
     * Read a file containing an instance of MCP.
     *
     * @param fname The path to the file containing the instance
     * @throws IOException If something goes wring while reading file.
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

    private int positiveOrNull(int x) {
        return max(x, 0);
    }

    private int negativeOrNull(int x) {
        return min(x, 0);
    }
}
