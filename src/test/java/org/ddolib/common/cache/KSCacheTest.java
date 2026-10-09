package org.ddolib.common.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.ddolib.common.frontier.CutSetType;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.verbosity.VerbosityLevel;
import org.ddolib.examples.layered.knapsack.KSDominance;
import org.ddolib.examples.layered.knapsack.KSFastLowerBound;
import org.ddolib.examples.layered.knapsack.KSProblem;
import org.ddolib.examples.layered.knapsack.KSRanking;
import org.ddolib.examples.layered.knapsack.KSRelax;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests that the DDO solver with caching finds the same optimal value as without caching on random
 * knapsack instances.
 */
public class KSCacheTest {

    static Stream<KSProblem> dataProvider() throws IOException {
        Random rand = new Random(10);
        int number = 1000;
        int nbVars = 10;
        int cap = 10;
        Stream<Integer> testStream = IntStream.rangeClosed(0, number).boxed();
        return testStream.flatMap(
                k -> {
                    int[] profit = new int[nbVars];
                    int[] weight = new int[nbVars];
                    for (int i = 0; i < nbVars; i++) {
                        profit[i] = 1 + rand.nextInt(cap / 2);
                        weight[i] = 2 + rand.nextInt(cap / 2);
                    }
                    KSProblem pb = new KSProblem(cap, profit, weight, 0.0);
                    return Stream.of(pb);
                });
    }

    private static double optimalSolutionNoCaching(KSProblem problem) {

        final DdoModel<Integer> model =
                new DdoModel<>() {
                    ;

                    @Override
                    public Problem<Integer> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<Integer> lowerBound() {
                        return new KSFastLowerBound(problem);
                    }

                    @Override
                    public DominanceChecker<Integer> dominance() {
                        return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
                    }

                    @Override
                    public VerbosityLevel verbosityLevel() {
                        return VerbosityLevel.SILENT;
                    }

                    @Override
                    public Relaxation<Integer> relaxation() {
                        return new KSRelax();
                    }

                    @Override
                    public KSRanking ranking() {
                        return new KSRanking();
                    }

                    @Override
                    public WidthHeuristic<Integer> widthHeuristic() {
                        return new FixedWidth<>(10_000);
                    }

                    @Override
                    public boolean useCache() {
                        return false;
                    }
                };

        Solution bestSol = Solvers.minimizeDdo(model);

        return bestSol.value();
    }

    private double optimalSolutionWithCache(KSProblem problem, int w, CutSetType cutSetType) {
        final DdoModel<Integer> model =
                new DdoModel<>() {
                    ;

                    @Override
                    public Problem<Integer> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<Integer> lowerBound() {
                        return new KSFastLowerBound(problem);
                    }

                    @Override
                    public DominanceChecker<Integer> dominance() {
                        return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
                    }

                    @Override
                    public VerbosityLevel verbosityLevel() {
                        return VerbosityLevel.SILENT;
                    }

                    @Override
                    public Relaxation<Integer> relaxation() {
                        return new KSRelax();
                    }

                    @Override
                    public KSRanking ranking() {
                        return new KSRanking();
                    }

                    @Override
                    public WidthHeuristic<Integer> widthHeuristic() {
                        return new FixedWidth<>(10_000);
                    }

                    @Override
                    public boolean useCache() {
                        return true;
                    }
                };

        Solution bestSol = Solvers.minimizeDdo(model);

        return bestSol.value();
    }

    /**
     * Checks that, for every width from 1 to 10 and every cut-set type, the DDO solver with caching
     * returns the same optimal value as without caching.
     *
     * @param problem the knapsack instance to solve
     */
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testOptimalSolutionFound(KSProblem problem) {
        CutSetType[] cs = new CutSetType[] {CutSetType.LastExactLayer, CutSetType.Frontier};
        for (int wid = 1; wid <= 10; wid++) {
            for (CutSetType ct : cs) {
                assertEquals(
                        optimalSolutionNoCaching(problem),
                        optimalSolutionWithCache(problem, wid, ct));
            }
        }
    }
}
