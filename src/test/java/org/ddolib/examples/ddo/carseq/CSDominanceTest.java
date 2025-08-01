package org.ddolib.examples.ddo.carseq;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CSDominanceTest {
    @Test
    public void testDominancePreviousBlocks() throws IOException {
        CSProblem problem = CSInstance.read("data/CarSeq/example.txt");
        CSDominance dominance = new CSDominance(problem);
        int[] carsToBuild = new int[problem.nClasses() + 1];
        int[] nWithOption = new int[problem.nOptions()];
        int nToBuild = 0;
        long[][] previousBlocks1 = {
            { 1, 1, 3, 3, 7 }, // 1, 10, 11, 1100, 1110
            { 1, 1, 1, 6, 12 }, // 1, 01, 01, 0110, 1100
            { 0, 2, 0, 5, 15 }, // 0, 10, 00, 0101, 1111
            { 1, 3, 2, 6, 13 }, // 1, 11, 10, 0110, 1011
        };
        long[][] previousBlocks2 = {
            { 0, 1, 2, 5, 3 }, // 0, 10, 01, 1010, 1100
            { 0, 2, 0, 9, 12 }, // 1, 10, 00, 1001, 1100
            { 1, 2, 0, 5, 15 }, // 1, 10, 00, 0101, 1111
            { 1, 3, 2, 12, 13 }, // 1, 11, 10, 0011, 1011
        };
        boolean[] dominated = { true, false, false, true };

        for (int i = 0; i < dominated.length; i++) {
            CSState state1 = new CSState(problem, carsToBuild, previousBlocks1[i], nWithOption, nToBuild);
            CSState state2 = new CSState(problem, carsToBuild, previousBlocks2[i], nWithOption, nToBuild);
            assertEquals(dominated[i], dominance.isDominatedOrEqual(state1, state2));
        }
    }
}
