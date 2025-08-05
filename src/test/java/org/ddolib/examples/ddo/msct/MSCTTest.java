package org.ddolib.examples.ddo.msct;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.modeling.DefaultFastUpperBound;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.util.testbench.ProblemTestBench;
import org.ddolib.util.testbench.SolverConfig;
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


    private static class MSCTBench extends ProblemTestBench<MSCTState, Integer, MSCTProblem> {

        /**
         * Instantiate a test bench.
         *
         * @param testRelaxation Whether the relaxation must be tested.
         * @param testFUB        Whether the fast upper bound must be tested.
         * @param testDominance  Whether the dominance must be tested.
         */
        public MSCTBench(boolean testRelaxation, boolean testFUB, boolean testDominance) {
            super(testRelaxation, testFUB, testDominance);

        }

        @Override
        protected List<MSCTProblem> generateProblems() {
            return Stream.concat(problemWithFixedRelease(), problemWithUnfixedRelease()).toList();
        }

        @Override
        protected SolverConfig<MSCTState, Integer> configSolver(MSCTProblem problem) {
            MSCTRelax relax = new MSCTRelax(problem);
            MSCTRanking ranking = new MSCTRanking();
            FastUpperBound<MSCTState> fub = new DefaultFastUpperBound<>();

            FixedWidth<MSCTState> width = new FixedWidth<>(1000);
            VariableHeuristic<MSCTState> varh = new DefaultVariableHeuristic<>();
            SimpleDominanceChecker<MSCTState, Integer> dominance = new SimpleDominanceChecker<>(new MSCTDominance(),
                    problem.nbVars());
            Frontier<MSCTState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

            return new SolverConfig<>(relax, varh, ranking, 2, 20, frontier, fub, dominance);
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
        var bench = new MSCTBench(true, true, true);
        return bench.generateTests();
    }
}