package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.examples.knapsack.*;
import org.ddolib.modeling.*;
import org.ddolib.util.verbosity.VerbosityLevel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KSHybridTest {

    static Stream<KSProblem> dataProvider() throws IOException {
        Random rand = new Random(10);
        int number = 1000;
        int nbVars = 10;
        int cap = 10;
        Stream<Integer> testStream = IntStream.rangeClosed(0, number).boxed();
        return testStream.flatMap(k -> {
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

    private static double optimalSolutionCostBasedClustering(KSProblem problem) {
        final DdoModel<Integer> model = new DdoModel<>() {
            ;

            @Override
            public Problem<Integer> problem() {
                return problem;
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
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public boolean useCache() {
                return false;
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(10_000);
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public ReductionStrategy relaxStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public ReductionStrategy restrictStrategy() {
                return new CostBased<>(ranking());
            }
        };

        SearchStatistics stat = Solvers.minimizeDdo(model);

        return stat.incumbent();
    }


    private double optimalSolutionGHPClustering(KSProblem problem, int w, CutSetType cutSetType) {
        final DdoModel<Integer> model = new DdoModel<>() {
            ;

            @Override
            public Problem<Integer> problem() {
                return problem;
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
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public boolean useCache() {
                return false;
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(10_000);
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public ReductionStrategy relaxStrategy() {
                return new Hybrid(new KSRanking(), new KSDistance(problem));
            }

            @Override
            public ReductionStrategy restrictStrategy() {
                return new Hybrid(new KSRanking(), new KSDistance(problem));
            }
        };

        SearchStatistics stat = Solvers.minimizeDdo(model);

        return stat.incumbent();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testOptimalSolutionFound(KSProblem problem) {
        CutSetType[] cs = new CutSetType[]{CutSetType.LastExactLayer, CutSetType.Frontier};
        for (int wid = 2; wid <= 10; wid++) {
            for (CutSetType ct : cs) {
                assertEquals(optimalSolutionCostBasedClustering(problem), optimalSolutionGHPClustering(problem, wid, ct));
            }
        }
    }

}
