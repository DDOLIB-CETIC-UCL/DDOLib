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


    @Override
    public boolean isDominatedOrEqual(CSState state1, CSState state2) {
        return dominatedPreviousBlocks(state1, state2) && dominatedCarsToBuild(state1, state2);
    }


    // Check if state1 is harder than state2 for previousBlocks
    private boolean dominatedPreviousBlocks(CSState state1, CSState state2) {
        for (int i = 0; i < problem.nOptions(); i++) { // Check for each option
            long previous1 = state1.previousBlocks[i], previous2 = state2.previousBlocks[i];
            int nPrevious1 = 0, nPrevious2 = 0;
            for (int j = 0; j < problem.blockSize[i] - 1; j++) { // Harder if previous1[0:j] >= previous2[0:j] for all j
                if ((previous1 & (1L << j)) != 0) nPrevious1++;
                if ((previous2 & (1L << j)) != 0) nPrevious2++;
                if (nPrevious1 < nPrevious2) return false;
            }
        }
        return true;
    }


    // Check if state1 is harder than state2 for carsToBuild
    private boolean dominatedCarsToBuild(CSState state1, CSState state2) {
        // Compute sources and sinks capacity
        int[] diff = new int[problem.nClasses() + 1];
        for (int i = 0; i < problem.nClasses() + 1; i++) {
            diff[i] = state1.carsToBuild[i] - state2.carsToBuild[i];
        }

        // Approximate max flow
        for (int i = 0; i < problem.nClasses() + 1; i++) {
            int node = order[i];
            if (diff[node] > 0) { // Found source -> try to assign its flow to reachable sources
                for (int child : reachable[node]) {
                    if (diff[child] < 0) { // Found sink -> add flow from source to sink
                        int flow = Math.min(diff[node], -diff[child]);
                        diff[node] -= flow;
                        diff[child] += flow;
                        if (diff[node] == 0) break; // Source is saturated
                    }
                }
                if (diff[node] > 0) return false; // Source couldn't be saturated
            }
        }
        return true;
    }


    // Build class dominance graph
    private void buildGraph() {
        // Find classes dominated by each class
        HashSet<Integer>[] children = new HashSet[problem.nClasses() + 1];
        ArrayList<Integer>[] allReachable = new ArrayList[problem.nClasses() + 1];
        for (int i = 0; i < problem.nClasses() + 1; i++) {
            children[i] = new HashSet<>();
            allReachable[i] = new ArrayList<>();
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
                if (dominates && Arrays.equals(problem.carOptions[i], problem.carOptions[j])) { // Tie-breaker
                    dominates = i < j;
                }
                if (dominates) {
                    children[i].add(j);
                    allReachable[i].add(j);
                }
            }
        }

        // Order nodes by number of reachable nodes
        order = IntStream.range(0, problem.nClasses() + 1).boxed()
                .sorted(Comparator.comparingInt(i -> allReachable[i].size()))
                .mapToInt(Integer::valueOf).toArray();

        // Remove shortcuts to build graph
        for (int i = 0; i < problem.nClasses() + 1; i++) {
            // DFS from children to find shortcuts
            boolean[] visited = new boolean[problem.nClasses() + 1];
            visited[i] = true;
            Stack<Integer> stack = new Stack<>();
            for (int child : children[i]) {
                visited[child] = true;
                stack.push(child);
            }
            while (!stack.empty()) {
                int node = stack.pop();
                for (int child : children[node]) {
                    children[i].remove(child); // Remove shortcut from i to child if there is one
                    if (!visited[child]) {
                        visited[child] = true;
                        stack.push(child);
                    }
                }
            }
        }

        // Order reachable nodes by their layer in the graph
        reachable = new int[problem.nClasses() + 1][];
        for (int i = 0; i < problem.nClasses() + 1; i++) {
            // BFS to order reachable nodes
            reachable[i] = new int[allReachable[i].size()];
            int j = 0;
            boolean[] visited = new boolean[problem.nClasses() + 1];
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(i);
            do {
                int node = queue.pop();
                for (int child : children[node]) {
                    if (visited[child]) continue;
                    visited[child] = true;
                    reachable[i][j++] = child;
                    queue.add(child);
                }
            } while (!queue.isEmpty());
        }
    }
}