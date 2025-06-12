package org.ddolib.ddo.examples.msct;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        final MSCTRanking ranking = new MSCTRanking();
        final FixedWidth<MSCTState> width = new FixedWidth<>(w);
        final VariableHeuristic<MSCTState> varh = new DefaultVariableHeuristic<MSCTState>();
        final SimpleDominanceChecker<MSCTState, Integer> dominance = new SimpleDominanceChecker<>(new MSCTDominance(),
                problem.nbVars());
        final Frontier<MSCTState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                dominance
        );

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
            int bestSolDDO = solve(data, w);
            int bestSolRef = bestSol(releaseTime, data.processing);
            assertEquals(bestSolRef, bestSolDDO);
        }
    }

    private int bestBruteForceSolution(int[] release, int[] processing) {
        int n = release.length;
        List<List<Integer>> permutations = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        generatePermutations(list, 0, permutations);
        int bestSolutionValue = Integer.MAX_VALUE;
        List<Integer> bestSolution = new ArrayList<>();
        for (List<Integer> permutation : permutations) {
            int t = 0;
            int objective = 0;
            int[] ends = new int[n];
            int k = 0;
            for (Integer i : permutation) {
                t = Math.max(t, release[i]) + processing[i];
                objective += t;
            }
            if (objective < bestSolutionValue) {
                bestSolutionValue = objective;
                bestSolution = permutation;
            }
        }
        return bestSolutionValue;
    }

    private void generatePermutations(List<Integer> list, int start, List<List<Integer>> permutations) {
        if (start == list.size() - 1) {
            permutations.add(new ArrayList<>(list));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            Collections.swap(list, start, i);
            generatePermutations(list, start + 1, permutations);
            Collections.swap(list, start, i);
        }
    }

    @Test
    public void testAnySolutionBruteForce() {
        int n = 8;
        MSCTData data = randomMSCTData(n);
        List<List<Integer>> permutations = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        generatePermutations(list, 0, permutations);
        int bestBruteForceSol = bestBruteForceSolution(data.release, data.processing);
        for (int w = 10; w < 100; w += 10) {
            int bestSolDDO = solve(data, w);
            assertEquals(bestBruteForceSol, bestSolDDO);
        }
    }


}