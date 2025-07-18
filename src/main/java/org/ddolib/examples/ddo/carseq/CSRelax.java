package org.ddolib.examples.ddo.carseq;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

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
                if (state.carsToBuild[i] < mergedCarsToBuild[i]) {
                    mergedCarsToBuild[i] = state.carsToBuild[i];
                }
            }
            for (int i = 0; i < problem.nOptions(); i++) { // Intersection of the blocks for each option
                mergedPreviousBlocks[i] &= state.previousBlocks[i];
            }
        } while (states.hasNext());

        // Add jokers to replace removed cars
        mergedCarsToBuild[problem.nClasses()] = state.nToBuild;
        for (int i = 0; i < problem.nClasses(); i++) {
            mergedCarsToBuild[problem.nClasses()] -= mergedCarsToBuild[i];
        }

        // Compute nWithOption
        int[] mergedNWithOption = new int[problem.nOptions()];
        for (int i = 0; i < problem.nClasses(); i++) {
            int nCars = mergedCarsToBuild[i];
            for (int j = 0; j < problem.nOptions(); j++) {
                if (problem.carOptions[i][j]) {
                    mergedNWithOption[j] += nCars;
                }
            }
        }
        for (int i = 0; i < problem.nOptions(); i++) {
            mergedNWithOption[i] += Long.bitCount(mergedPreviousBlocks[i]);
        }

        return new CSState(problem, mergedCarsToBuild, mergedPreviousBlocks, mergedNWithOption, state.nToBuild);
    }


    @Override
    public double relaxEdge(CSState from, CSState to, CSState merged, Decision d, double cost) {
        return cost;
    }
}
