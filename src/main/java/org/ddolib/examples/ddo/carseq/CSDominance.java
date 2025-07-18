package org.ddolib.examples.ddo.carseq;

import org.ddolib.modeling.Dominance;

import java.util.Arrays;


public class CSDominance implements Dominance<CSState, Integer> {
    private final CSProblem problem;

    public CSDominance(CSProblem problem) {
        this.problem = problem;
    }


    @Override
    public Integer getKey(CSState state) {
        return 0;
    }


    @Override
    public boolean isDominatedOrEqual(CSState state1, CSState state2) {
        return dominatedCarsToBuild(state1, state2) && dominatedPreviousBlocks(state2, state1);
    }


    // Check if state1 is easier than state2 for previousBlocks
    private boolean dominatedPreviousBlocks(CSState state1, CSState state2) {
        for (int i = 0; i < problem.nOptions(); i++) { // Check for each option
            int nPrevious1 = 0, nPrevious2 = 0;
            for (int j = 0; j < problem.blockSize[i] - 1; j++) { // Easier if previous1[0:j] <= previous2[0:j] for all j
                if ((state1.previousBlocks[i] & (1L << j)) != 0) nPrevious1++;
                if ((state2.previousBlocks[i] & (1L << j)) != 0) nPrevious2++;
                if (nPrevious1 > nPrevious2) return false;
            }
        }
        return true;
    }


    // Check if state1 is easier than state2 for carsToBuild
    private boolean dominatedCarsToBuild(CSState state1, CSState state2) {
        return Arrays.equals(state1.carsToBuild, state2.carsToBuild);
    }
}
