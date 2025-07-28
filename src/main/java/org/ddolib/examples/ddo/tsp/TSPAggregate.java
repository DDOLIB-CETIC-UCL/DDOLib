package org.ddolib.examples.ddo.tsp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Aggregate;
import org.ddolib.modeling.SolverInput;

import java.util.Arrays;


public class TSPAggregate implements Aggregate<TSPState, Integer> {
    private final TSPProblem problem;
    private SolverInput<TSPState, Integer> input;
    private int[] map; // Node in the initial problem -> node in the aggregated problem

    private final int N = 12;

    public TSPAggregate(TSPProblem problem) {
        this.problem = problem;
        aggregateProblem();
    }


    @Override
    public SolverInput<TSPState, Integer> getProblem() {
        return input;
    }

    @Override
    public Decision mapDecision(Decision decision) {
        return new Decision(decision.var(), map[decision.val()]);
    }


    private void aggregateProblem() {
        double[][] distances = new double[problem.n][];
        for (int i = 0; i < problem.n; i++) {
            distances[i] = Arrays.copyOf(problem.distanceMatrix[i], problem.n);
        }
        int[] merged = new int[problem.n];
        Arrays.fill(merged, -1);
        int currentN = problem.n;

        // Merge nodes together until required number of nodes
        while (currentN > N) {
            // Find 2 nodes as close as possible to merge
            double minDist = Integer.MAX_VALUE;
            int minNode1 = 0, minNode2 = 0;
            for (int i = 0; i < problem.n; i++) {
                if (merged[i] != -1) continue;
                for (int j = 0; j < problem.n; j++) {
                    if (merged[j] != -1 || i == j) continue;
                    double dist = distances[i][j];
                    if (dist < minDist) {
                        minDist = dist;
                        minNode1 = i;
                        minNode2 = j;
                    }
                }
            }

            // Merge node 2 into node 1
            merged[minNode1] = minNode2;
            distances[minNode1][minNode2] = 0;
            distances[minNode2][minNode1] = 0;
            for (int i = 0; i < problem.n; i++) {
                if (merged[i] == -1 && i != minNode1 && i != minNode2) {
                    double newDist = Math.min(distances[minNode1][i], distances[minNode2][i]);
                    distances[minNode1][i] = distances[i][minNode1] = newDist;
                    distances[minNode2][i] = distances[i][minNode2] = newDist;
                }
            }
            currentN--;
        }

        // Create new problem
        double[][] aggregatedDistances = new double[N][N];
        map = new int[problem.n];
        int i = 0;
        for (int j = 0; j < problem.n; j++) {
            if (merged[j] != -1) continue;
            for (int k = 0; k < j; k++) {
                if (merged[k] != -1) continue;
                aggregatedDistances[i][map[k]] = aggregatedDistances[map[k]][i] = distances[j][k];
            }
            map[j] = i++;
        }
        for (int j = 0; j < problem.n; j++) {
            if (merged[j] != -1) continue;
            int index = j, parent = merged[j];
            while (parent != -1) {
                index = parent;
                parent = merged[parent];
            }
            map[j] = map[index];
        }

        TSPProblem problem = new TSPProblem(aggregatedDistances);
        input = SolverInput.defaultInput(problem, new TSPRelax(problem));
        input.fub = new TSPFastUpperBound(problem);
    }
}
