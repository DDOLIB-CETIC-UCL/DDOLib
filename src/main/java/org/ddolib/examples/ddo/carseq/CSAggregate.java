package org.ddolib.examples.ddo.carseq;

import org.ddolib.ddo.core.Decision;

import java.util.Arrays;


/**
 * Mapping between an initial problem and an aggregated problem.
 * Allows mapping a decision in the initial problem to a decision in the aggregated problem.
 * It also provides the required classes for solving the aggregated problem (problem, relaxation, fast upper bound, ...)
 */
public class CSAggregate {
    private final CSProblem problem;
    private CSProblem aggregated;
    private CSRelax relax;
    private int[] map; // Class in the initial problem -> class in the aggregated problem

    private final int N_CLASSES = 3;

    public CSAggregate(CSProblem problem) {
        this.problem = problem;
        aggregateProblem();
    }


    /**
     * Get the aggregated problem
     */
    public CSProblem getProblem() {
        return aggregated;
    }

    /**
     * Get the relaxation operator for the aggregated problem
     */
    public CSRelax getRelax() {
        return relax;
    }

    /**
     * Map a decision
     * @param decision Decision in the initial problem
     * @return Decision in the aggregated problem
     */
    public Decision mapDecision(Decision decision) {
        return new Decision(decision.var(), map[decision.val()]);
    }


    private void aggregateProblem() {
        boolean[][] options = new boolean[problem.nClasses()][];
        for (int i = 0; i < problem.nClasses(); i++) {
            options[i] = Arrays.copyOf(problem.carOptions[i], problem.nOptions());
        }
        int[] nCars = Arrays.copyOf(problem.classSize, problem.nClasses());
        int[] merged = new int[problem.nClasses()];
        Arrays.fill(merged, -1);
        int currentNClasses = problem.nClasses();

        // Merge classes together until
        while (currentNClasses > N_CLASSES) {
            // Find 2 classes as close as possible to merge
            int minCost = Integer.MAX_VALUE;
            int minClass1 = 0, minClass2 = 0;
            for (int i = 0; i < problem.nClasses(); i++) {
                if (merged[i] != -1) continue;
                for (int j = 0; j < problem.nClasses(); j++) {
                    if (merged[j] != -1 || i == j) continue;
                    int dist1 = 0, dist2 = 0;
                    for (int k = 0; k < problem.nOptions(); k++) {
                        if (options[i][k] && !options[j][k]) dist1++;
                        if (options[j][k] && !options[i][k]) dist2++;
                    }
                    int cost = nCars[i] * dist1 + nCars[j] * dist2;
                    if (cost < minCost) {
                        minCost = cost;
                        minClass1 = i;
                        minClass2 = j;
                    }
                }
            }

            // Merge class 2 into class 1
            nCars[minClass1] += nCars[minClass2];
            for (int i = 0; i < problem.nOptions(); i++) {
                if (!options[minClass2][i]) options[minClass1][i] = false;
            }
            merged[minClass2] = minClass1;
            currentNClasses--;
        }

        // Create new problem
        boolean[][] aggregatedOptions = new boolean[currentNClasses][];
        int[] aggregatedNCars = new int[currentNClasses];
        map = new int[problem.nClasses() + 1];
        map[problem.nClasses()] = currentNClasses;
        int i = 0;
        for (int j = 0; j < problem.nClasses(); j++) {
            if (merged[j] == -1) {
                aggregatedOptions[i] = options[j];
                aggregatedNCars[i] = nCars[j];
                map[j] = i;
                i++;
            }
        }
        for (int j = 0; j < problem.nClasses(); j++) {
            if (merged[j] != -1) {
                int classIndex = j, parent = merged[j];
                while (parent != -1) {
                    classIndex = parent;
                    parent = merged[parent];
                }
                map[j] = map[classIndex];
            }
        }

        aggregated = new CSProblem(aggregatedNCars, problem.blockSize, problem.blockMax, aggregatedOptions);
        relax = new CSRelax(aggregated);
    }
}
