package org.ddolib.examples.msct;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MSCTTestDataSupplier extends TestDataSupplier<MSCTState, MSCTProblem> {
    static Random rand = new Random(42);

    @Override
    protected List<MSCTProblem> generateProblems() {
        return Stream.concat(problemWithFixedRelease(), problemWithUnfixedRelease()).toList();
    }

    @Override
    protected DdoModel<MSCTState> model(MSCTProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<MSCTState> problem() {
                return problem;
            }

            @Override
            public DominanceChecker<MSCTState> dominance() {
                return new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }

            @Override
            public MSCTRelax relaxation() {
                return new MSCTRelax(problem);
            }

            @Override
            public MSCTRanking ranking() {
                return new MSCTRanking();
            }
        };
    }

    private Stream<MSCTProblem> problemWithFixedRelease() {
        int release = 15;
        return IntStream.range(2, 7).mapToObj(i -> {
            MSCTData data = randomMSCTDataFixedRelease(i, release);
            double opti = bruteForceForFixedRelease(release, data.processing);
            return new MSCTProblem(data.release, data.processing, Optional.of(-opti));
        });
    }

    private Stream<MSCTProblem> problemWithUnfixedRelease() {
        return IntStream.range(2, 9).mapToObj(i -> {
            MSCTData data = randomMSCTData(i);
            double opti = bestBruteForceForUnfixedRelease(data.release, data.processing);
            return new MSCTProblem(data.release, data.processing, Optional.of(-opti));
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

    protected record MSCTData(int[] release, int[] processing) {
    }
}
