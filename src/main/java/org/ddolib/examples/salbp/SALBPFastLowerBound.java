package org.ddolib.examples.salbp;

import org.ddolib.modeling.FastLowerBound;
import org.ddolib.util.algo.BinPacking;

import java.util.Set;

public class SALBPFastLowerBound implements FastLowerBound<SALBPState> {

    private final SALBProblem problem;

    public SALBPFastLowerBound(SALBProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(SALBPState state, Set<Integer> variables) {
        if (variables.isEmpty()) return 0;

        // To use labbeLB algorithm, we cannot consider bin that are partially full.
        // Therefore, we remove the content of each used bin AS ONE ITEM and add it to the list (sorted biggest to lowest).
        // And we run the algorithm. We need to remove all used bins from the result (otherwise counted twice).
        int[] fullTasks = new int[1 + variables.size()];
        fullTasks[0] = problem.cycleTime - state.currentStationRemainingTime();

        int fullTasksId = 1;
        for (int i = state.remainingTasks().nextSetBit(0); i >= 0; i = state.remainingTasks().nextSetBit(i + 1)) {
            fullTasks[fullTasksId] = problem.tasksTime[i];
            fullTasksId++;
        }
        // Sort only first element
        fullTasksId = 1;
        while(fullTasksId < fullTasks.length && fullTasks[fullTasksId] > fullTasks[fullTasksId - 1]) {
            int temp = fullTasks[fullTasksId];
            fullTasks[fullTasksId] = fullTasks[fullTasksId - 1];
            fullTasks[fullTasksId - 1] = temp;
            fullTasksId++;
        }
        //System.out.println(state.usedBins + Math.max(0,BinPacking.labbeLB(fullTasks, this.problem.binMaxSpace) - 1));
        return Math.max(0, BinPacking.labbeLB(fullTasks, this.problem.cycleTime) - 1);
    }
}
