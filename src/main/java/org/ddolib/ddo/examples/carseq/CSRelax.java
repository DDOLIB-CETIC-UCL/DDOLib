package org.ddolib.ddo.examples.carseq;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;


public class CSRelax implements Relaxation<CSState> {
    private final CSProblem problem;

    public CSRelax(CSProblem problem) {
        this.problem = problem;
    }


    @Override
    public CSState mergeStates(Iterator<CSState> states) {
        // Merge carsToBuild and previousBLocks
        int[] mergedCarsToBuild = new int[problem.nClasses() + 1];
        Arrays.fill(mergedCarsToBuild, 0, problem.nClasses(), Integer.MAX_VALUE);
        long[] mergedPreviousBlocks = new long[problem.nOptions()];
        Arrays.fill(mergedPreviousBlocks, -1);
        CSState state;
        do {
            state = states.next();
            for (int i = 0; i < problem.nClasses(); i++) { // Min number of cars for each class
                if (state.carsToBuild()[i] < mergedCarsToBuild[i]) {
                    mergedCarsToBuild[i] = state.carsToBuild()[i];
                }
            }
            for (int i = 0; i < problem.nOptions(); i++) { // Intersection of the blocks for each option
                mergedPreviousBlocks[i] &= state.previousBlocks()[i];
            }
        } while (states.hasNext());

        // Add jokers to replace removed cars
        for (int i = 0; i < problem.nClasses(); i++) {
            mergedCarsToBuild[problem.nClasses()] += state.carsToBuild()[i] - mergedCarsToBuild[i];
        }

        return new CSState(mergedCarsToBuild, mergedPreviousBlocks);
    }


    @Override
    public double relaxEdge(CSState from, CSState to, CSState merged, Decision d, double cost) {
        return cost;
    }


    @Override
    public double fastUpperBound(CSState state, Set<Integer> variables) {
        // Count remaining number of cars
        int nToBuild = state.carsToBuild()[problem.nClasses()];
        int[] nWithOption = new int[problem.nOptions()];
        for (int i = 0; i < problem.nClasses(); i++) {
            int nCars = state.carsToBuild()[i];
            nToBuild += nCars;
            for (int j = 0; j < problem.nOptions(); j++) {
                if (problem.carOptions[i][j]) {
                    nWithOption[j] += nCars;
                }
            }
        }

        // Bound for each option separately
        double bound = 0;
        for (int i = 0; i < problem.nOptions(); i++) {
            // Count number of cars with and without the option in the previous block and in the future
            int k = problem.blockMax[i], l = problem.blockSize[i];
            int n = nToBuild + l;
            int withOption = nWithOption[i] + Long.bitCount(state.previousBlocks()[i]);
            int withoutOption = n - withOption;

            // Compute bound
            int nReduce = n / l * (l - k) +
                Math.max((n - l) % l - k, 0); // Number of cars without the option that can reduce the number of violations
            if (withoutOption < nReduce) {
                bound -= nReduce - withoutOption;
            }
        }
        return bound;
    }
}
