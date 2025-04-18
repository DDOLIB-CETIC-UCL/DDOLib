package org.ddolib.ddo.examples.msct;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.Golomb;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MSCTTest {

    protected record MSCTData(int [] release, int [] processing) {
    }

    private static MSCTData randomMSCTData(int n, int seed) {
        Random r = new Random(seed);
        int [] release = new int[n];
        int [] processing = new int[n];
        for (int i = 0; i < n; i++) {
            release[i] = r.nextInt(n*100);
            processing[i] = 1 + r.nextInt(n*100);
        }
        return new MSCTData(release,processing);
    }

    @Test
    public void testSameRelease() {
        int n = 10;
        int [] release = new int[n];
        Random r = new Random(0);
    }




}