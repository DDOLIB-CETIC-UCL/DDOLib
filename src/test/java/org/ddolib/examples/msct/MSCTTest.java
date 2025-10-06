package org.ddolib.examples.msct;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class MSCTTest {

    static Random rand = new Random(42);


    protected record MSCTData(int[] release, int[] processing) {
    }


    private static class MSCTBench extends ProblemTestBench<MSCTState, MSCTProblem> {

        public MSCTBench() {
            super();

        }

        @Override
        protected List<MSCTProblem> generateProblems() {
            return Stream.concat(problemWithFixedRelease(), problemWithUnfixedRelease()).toList();
        }

        @Override
        protected SolverConfig<MSCTState> configSolver(MSCTProblem problem) {
            SolverConfig<MSCTState> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new MSCTRelax(problem);
            config.ranking = new MSCTRanking();
            config.width = new FixedWidth<>(100);
            config.varh = new DefaultVariableHeuristic<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            config.dominance = new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());

            return config;
        }

        private Stream<MSCTProblem> problemWithFixedRelease() {
            int release = 15;
            return IntStream.range(2, 7).mapToObj(i -> {
                MSCTData data = randomMSCTDataFixedRelease(i, release);
                int opti = bruteForceForFixedRelease(release, data.processing);
                return new MSCTProblem(data.release, data.processing, opti);
            });
        }

        private Stream<MSCTProblem> problemWithUnfixedRelease() {
            return IntStream.range(2, 9).mapToObj(i -> {
                MSCTData data = randomMSCTData(i);
                int opti = bestBruteForceForUnfixedRelease(data.release, data.processing);
                return new MSCTProblem(data.release, data.processing, opti);
            });
        }

        private MSCTData randomMSCTData(int n) {
            int[] release = new int[n];
            int[] processing = new int[n];
            for (int i = 0; i < n; i++) {
                release[i] = rand.nextInt(n * 100);
                processing[i] = 1 + rand.nextInt(n * 100);
            }
            return new MSCTData(release, processing);
        }

        private MSCTData randomMSCTDataFixedRelease(int n, int release) {
            MSCTData res = randomMSCTData(n);
            Arrays.fill(res.release, release);
            return res;
        }

        private int bruteForceForFixedRelease(int release, int[] processing) {
            Arrays.sort(processing);
            int objective = 0;
            int t = release;
            for (int j : processing) {
                t += j;
                objective += t;
            }
            return objective;
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

        private int bestBruteForceForUnfixedRelease(int[] release, int[] processing) {
            int n = release.length;
            List<List<Integer>> permutations = new ArrayList<>();
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                list.add(i);
            }
            generatePermutations(list, 0, permutations);
            int bestSolutionValue = Integer.MAX_VALUE;
            for (List<Integer> permutation : permutations) {
                int t = 0;
                int objective = 0;
                for (Integer i : permutation) {
                    t = Math.max(t, release[i]) + processing[i];
                    objective += t;
                }
                if (objective < bestSolutionValue) {
                    bestSolutionValue = objective;
                }
            }
            return bestSolutionValue;
        }

    }

    @DisplayName("MSCT")
    @TestFactory
    public Stream<DynamicTest> testMSCT() {
        var bench = new MSCTBench();
        bench.testRelaxation = true;
        bench.testDominance = true;
        return bench.generateTests();
    }
}