package org.ddolib.ddo.examples.msct;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MSCTTest {

    static Random rand = new Random(42);

    protected record MSCTData(int[] release, int[] processing) {

        protected void shuffle() {
            // shuffle the release and processing times with same permutation
            for (int i = 0; i < release.length; i++) {
                int j = rand.nextInt(release.length);
                int tmp = release[i];
                release[i] = release[j];
                release[j] = tmp;
                tmp = processing[i];
                processing[i] = processing[j];
                processing[j] = tmp;
            }
        }
    }

    public int solve(MSCTData data, int w) {
        MSCTProblem problem = new MSCTProblem(data.release, data.processing);
        final MSCTRelax relax = new MSCTRelax(problem);
        final SequencingRanking ranking = new SequencingRanking();
        final FixedWidth<MSCTState> width = new FixedWidth<>(w);
        final VariableHeuristic<MSCTState> varh = new DefaultVariableHeuristic<MSCTState>();

        final Frontier<MSCTState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        solver.maximize();

        int[] solution = solver.bestSolution().map(decisions -> {
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            return values;
        }).get();

        return -solver.bestValue().get();
    }

    private static MSCTData randomMSCTData(int n) {
        int[] release = new int[n];
        int[] processing = new int[n];
        for (int i = 0; i < n; i++) {
            release[i] = rand.nextInt(n * 100);
            processing[i] = 1 + rand.nextInt(n * 100);
        }
        return new MSCTData(release, processing);
    }

    private static MSCTData randomMSCTDataFixedRelease(int n, int release) {
        MSCTData res = randomMSCTData(n);
        Arrays.fill(res.release, release);
        return res;
    }

    private int bestSol(int release, int[] processing) {
        Arrays.sort(processing);
        int objective = 0;
        int t = release;
        for (int i = 0; i < processing.length; i++) {
            t += processing[i];
            objective += t;
        }
        return objective;
    }

    @Test
    public void testSameRelease() {

        for (int w = 10; w < 100; w += 10) {
            int n = 7;
            int releaseTime = 15;
            MSCTData data = randomMSCTDataFixedRelease(n, releaseTime);
            int bestSolDDO = solve(data, 10);
            int bestSolRef = bestSol(releaseTime, data.processing);
            assertEquals(bestSolRef, bestSolDDO);
        }


    }


}