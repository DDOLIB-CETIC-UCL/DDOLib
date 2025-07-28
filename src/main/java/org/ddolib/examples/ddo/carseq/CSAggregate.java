package org.ddolib.examples.ddo.carseq;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Aggregate;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.SolverInput;

import java.util.Arrays;
import java.util.Iterator;


public class CSAggregate implements Aggregate<CSState, Integer> {
    private final CSProblem problem;
    private SolverInput<CSState, Integer> input;
    private int[] map; // Class in the initial problem -> class in the aggregated problem

    private final int N_CLASSES = 3;

    public CSAggregate(CSProblem problem) {
        this.problem = problem;
        aggregateProblem();
    }


    @Override
    public SolverInput<CSState, Integer> getProblem() {
        return input;
    }

    @Override
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

        CSProblem aggregatedProblem = new CSProblem(aggregatedNCars, problem.blockSize, problem.blockMax, aggregatedOptions);
        input = SolverInput.defaultInput(aggregatedProblem, new NoRelaxation(aggregatedProblem));
        input.fub = new CSFastUpperBound(aggregatedProblem);
        input.ranking = new CSRanking();
    }


    // Aggregated nodes should not be relaxed because they could be less relaxed than the initial node
    private static class NoRelaxation implements Relaxation<CSState> {
        private final CSProblem problem;

        public NoRelaxation(CSProblem problem) {
            this.problem = problem;
        }

        @Override
        public CSState mergeStates(Iterator<CSState> states) {
            int nToBuild = states.next().nToBuild;
            int[] carsToBuild = new int[problem.nClasses() + 1];
            carsToBuild[problem.nClasses()] = nToBuild;
            return new CSState(problem, carsToBuild, new long[problem.nOptions()], new int[problem.nOptions()], nToBuild);
        }

        @Override
        public double relaxEdge(CSState from, CSState to, CSState merged, Decision d, double cost) {
            return cost;
        }
    }
}