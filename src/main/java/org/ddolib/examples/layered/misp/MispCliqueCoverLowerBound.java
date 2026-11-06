package org.ddolib.examples.layered.misp;

import java.util.BitSet;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.IntStream;
import org.ddolib.layered.modeling.FastLowerBound;

/**
 * Fast lower bound for the Maximum Independent Set Problem (MISP) based on a clique cover of the
 * graph (i.e. a coloring of its complement).
 *
 * <p>The costs of the MISP are the negated weights of the selected vertices. An independent set
 * contains at most one vertex of each clique, so that the weight that can still be selected from a
 * state is at most the sum, over the cliques of the cover, of the largest weight of a vertex of the
 * clique still selectable in the state. The bound is minus this sum. It is admissible, and at least
 * as tight as {@link MispFastLowerBound}, which sums the weights of all the selectable vertices
 * (it is the bound of the cover made of single vertices).
 *
 * <p>The cover is computed once, greedily: the vertices are taken by decreasing weight, and each
 * one is added to the first clique all of whose vertices are its neighbors, or starts a new
 * clique. The bound of a state is then computed in a time linear in its number of vertices, like
 * {@link MispFastLowerBound}.
 */
public class MispCliqueCoverLowerBound implements FastLowerBound<BitSet> {

    private final int[] weight;

    /** For each vertex, the index of its clique in the cover. */
    private final int[] cliqueOf;

    /** The number of cliques of the cover. */
    private final int nbCliques;

    /**
     * Computes the clique cover of the graph of the given problem.
     *
     * @param problem the MISP instance
     */
    public MispCliqueCoverLowerBound(MispProblem problem) {
        int n = problem.weight.length;
        this.weight = problem.weight;
        this.cliqueOf = new int[n];
        Integer[] order =
                IntStream.range(0, n)
                        .boxed()
                        .sorted(Comparator.comparingInt((Integer v) -> -weight[v]))
                        .toArray(Integer[]::new);
        // for each clique, the vertices that are adjacent to all its vertices
        BitSet[] candidates = new BitSet[n];
        int k = 0;
        for (int v : order) {
            int clique = -1;
            for (int c = 0; c < k && clique < 0; c++) {
                if (candidates[c].get(v)) {
                    clique = c;
                }
            }
            if (clique < 0) {
                clique = k++;
                candidates[clique] = (BitSet) problem.neighbors[v].clone();
            } else {
                candidates[clique].and(problem.neighbors[v]);
            }
            cliqueOf[v] = clique;
        }
        this.nbCliques = k;
    }

    /**
     * Returns the number of cliques of the cover.
     *
     * @return the number of cliques
     */
    public int nbCliques() {
        return nbCliques;
    }

    /**
     * Returns minus the sum, over the cliques of the cover, of the largest weight of a vertex of
     * the clique still selectable in the state.
     *
     * @param state the vertices that can still be selected
     * @param variables the unassigned variables (unused: the selectable vertices are unassigned)
     * @return a lower bound on the cost of the remaining decisions
     */
    @Override
    public double fastLowerBound(BitSet state, Set<Integer> variables) {
        int[] best = new int[nbCliques];
        for (int v = state.nextSetBit(0); v >= 0; v = state.nextSetBit(v + 1)) {
            best[cliqueOf[v]] = Math.max(best[cliqueOf[v]], weight[v]);
        }
        long sum = 0;
        for (int w : best) {
            sum += w;
        }
        return -sum;
    }
}
