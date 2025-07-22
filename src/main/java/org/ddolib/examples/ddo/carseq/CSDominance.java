package org.ddolib.examples.ddo.carseq;

import org.ddolib.modeling.Dominance;

import java.util.*;
import java.util.stream.IntStream;


public class CSDominance implements Dominance<CSState, Integer> {
    private final CSProblem problem;
    private int[][] reachable; // All nodes reachable from each node in the class dominance graph
    private int[] order; // Node ordering

    public CSDominance(CSProblem problem) {
        this.problem = problem;
        buildGraph();
    }


    @Override
    public Integer getKey(CSState state) {
        return 0;
    }

    static int debugCount = 0;
    @Override
    public boolean isDominatedOrEqual(CSState state1, CSState state2) {
        return dominatedPreviousBlocks(state1, state2) && dominatedCarsToBuild(state1, state2);
    }


    // Check if state1 is easier than state2 for previousBlocks
    private boolean dominatedPreviousBlocks(CSState state1, CSState state2) {
        for (int i = 0; i < problem.nOptions(); i++) { // Check for each option
            long previous1 = state1.previousBlocks[i], previous2 = state2.previousBlocks[i];
            int nPrevious1 = 0, nPrevious2 = 0;
            for (int j = 0; j < problem.blockSize[i] - 1; j++) { // Easier if previous1[0:j] <= previous2[0:j] for all j
                if ((previous1 & (1L << j)) != 0) nPrevious1++;
                if ((previous2 & (1L << j)) != 0) nPrevious2++;
                if (nPrevious1 > nPrevious2) return false;
            }
        }
        return true;
    }


    // Check if state1 is easier than state2 for carsToBuild
    private boolean dominatedCarsToBuild(CSState state1, CSState state2) {
        // Compute sources and sinks capacity
        int[] diff = new int[problem.nClasses() + 1];
        for (int i = 0; i < problem.nClasses() + 1; i++) {
            diff[i] = state2.carsToBuild[i] - state1.carsToBuild[i];
        }

        // Approximate max flow
        for (int i = 0; i < problem.nClasses() + 1; i++) {
            int node = order[i];
            if (diff[node] > 0) { // Found source
                for (int child : reachable[node]) {
                    if (diff[child] < 0) {
                        int flow = Math.min(diff[node], -diff[child]);
                        diff[node] -= flow;
                        diff[child] += flow;
                        if (diff[node] == 0) break;
                    }
                }
                if (diff[node] > 0) return false;
            }
        }
        return true;
    }


    // Build class dominance graph
    private void buildGraph() {
        // Find classes dominated by each class
        ArrayList<Integer>[] dominations = new ArrayList[problem.nClasses() + 1];
        for (int i = 0; i < problem.nClasses() + 1; i++) {
            dominations[i] = new ArrayList<>();
            for (int j = 0; j < problem.nClasses() + 1; j++) {
                if (i == j) continue;

                // Check if carOptions[i] is a superset of carOptions[j]
                boolean dominates = true;
                for (int k = 0; k < problem.nOptions(); k++) {
                    if (problem.carOptions[j][k] && !problem.carOptions[i][k]) {
                        dominates = false;
                        break;
                    }
                }
                if (dominates) dominations[i].add(j);
            }
        }

        // Create reachable arrays and sort by number of children
        reachable = new int[problem.nClasses() + 1][];
        for (int i = 0; i < problem.nClasses() + 1; i++) {
            reachable[i] = dominations[i].stream()
                    .sorted(Comparator.comparingInt(j -> -dominations[j].size()))
                    .mapToInt(Integer::valueOf).toArray();
        }

        // Order nodes by number of children
        order = IntStream.range(0, problem.nClasses() + 1).boxed()
                .sorted(Comparator.comparingInt(i -> dominations[i].size()))
                .mapToInt(Integer::valueOf).toArray();
    }
}