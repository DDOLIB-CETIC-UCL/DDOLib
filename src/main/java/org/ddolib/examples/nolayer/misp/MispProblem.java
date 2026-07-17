package org.ddolib.examples.nolayer.misp;

import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.common.util.InvalidSolutionException;

import java.util.*;

/**
 * The Maximum Independent Set Problem (MISP), in the no-layer modeling API.
 * <p>
 * The problem is represented as an undirected graph where each node has a weight, and the
 * goal is to select a subset of non-adjacent nodes (an independent set) that maximizes the
 * total weight. Since the library only minimizes, weights are negated in
 * {@link #transitionCost} and {@link #evaluate}.
 */
public class MispProblem implements Problem<MispState> {

    /** For each node, the set of nodes it is adjacent to. */
    public final BitSet[] neighbors;
    /** The weight of each node. */
    public final int[] weight;
    /** The number of nodes in the graph. */
    public final int nbVars;
    private final Optional<String> name;

    /**
     * Creates a new MISP instance with the given adjacency and weights.
     *
     * @param neighbors for each node, the set of nodes it is adjacent to
     * @param weight    the weight of each node
     * @param name      a name identifying this instance, typically the file it was loaded from
     */
    public MispProblem(BitSet[] neighbors, int[] weight, String name) {
        this.neighbors = neighbors;
        this.weight = weight;
        this.nbVars = weight.length;
        this.name = Optional.of(name);
    }


    /**
     * Creates a new, unnamed MISP instance with the given adjacency and weights.
     *
     * @param neighbors for each node, the set of nodes it is adjacent to
     * @param weight    the weight of each node
     */
    public MispProblem(BitSet[] neighbors, int[] weight) {
        this.neighbors = neighbors;
        this.weight = weight;
        this.nbVars = weight.length;
        this.name = Optional.empty();
    }

    /**
     * Reads a MISP instance from a DOT file, where each node line may carry a
     * {@code weight=} attribute (defaulting to {@code 1} when absent) and edges are listed
     * as {@code source -- target} lines.
     *
     * @param fname the path to the DOT file describing the instance
     * @return the MISP instance read from the file
     * @throws java.io.IOException if the file cannot be read
     */
    public static MispProblem fromFile(String fname) throws java.io.IOException {
        java.util.ArrayList<Integer> weight = new java.util.ArrayList<>();
        BitSet[] neighbor;
        int n;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(fname))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null && !line.contains("--")) {
                if (line.isEmpty()) continue;

                if (line.contains("optimal")) {
                    // Ignore optimal
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
            java.util.Arrays.setAll(neighbor, i -> new BitSet(n));
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
        return new MispProblem(neighbor, weight.stream().mapToInt(x -> x).toArray(), fname);
    }

    @Override
    public MispState initialState() {
        BitSet remaining = new BitSet(nbVars);
        remaining.set(0, nbVars);
        return new MispState(remaining);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public boolean isTarget(MispState state) {
        return state.remainingNodes().isEmpty();
    }

    @Override
    public Iterator<Integer> domain(MispState state) {
        List<Integer> dom = new ArrayList<>();
        int i = state.remainingNodes().nextSetBit(0);
        while (i >= 0) {
            dom.add(i);
            i = state.remainingNodes().nextSetBit(i + 1);
        }
        return dom.iterator();
    }

    @Override
    public MispState transition(MispState state, int label) {
        BitSet nextRemaining = (BitSet) state.remainingNodes().clone();
        nextRemaining.andNot(neighbors[label]);
        nextRemaining.clear(0, label + 1);
        return new MispState(nextRemaining);
    }

    @Override
    public double transitionCost(MispState state, int label) {
        return -weight[label];
    }

    @Override
    public double evaluate(List<Integer> solution) throws InvalidSolutionException {
        int[] binarySol = new int[nbVars];
        for (int v : solution) {
            binarySol[v] = 1;
        }

        for (int i = 0; i < nbVars; i++) {
            if (binarySol[i] == 1) {
                for (int j = neighbors[i].nextSetBit(0); j >= 0; j = neighbors[i].nextSetBit(j + 1)) {
                    if (binarySol[j] == 1) {
                        String msg = String.format("The solution %s contains adjacent nodes (%d, %d)",
                                solution, i, j);
                        throw new InvalidSolutionException(msg);
                    }
                }
            }
        }

        double val = 0;
        for (int i = 0; i < nbVars; i++) {
            if (binarySol[i] == 1) {
                val += weight[i];
            }
        }

        return -val;
    }

    @Override
    public String toString() {
        return name.orElse("MispProblem(nbVars:" + nbVars + ")");
    }
}
