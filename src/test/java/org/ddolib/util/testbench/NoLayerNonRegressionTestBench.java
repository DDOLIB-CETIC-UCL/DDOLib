package org.ddolib.util.testbench;

import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.nolayer.common.solver.Solution;
import org.ddolib.nolayer.modeling.*;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.util.InvalidSolutionException;
import org.ddolib.util.verbosity.VerbosityLevel;
import org.junit.jupiter.api.DynamicTest;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoLayerNonRegressionTestBench<T, P extends Problem<T>> {

    protected final List<P> problems;
    private final Function<P, DdoModel<T>> model;

    public NoLayerNonRegressionTestBench(NoLayerTestDataSupplier<T, P> dataSupplier) {
        problems = dataSupplier.generateProblems();
        model = dataSupplier::model;
    }

    protected void testAllSolver(P problem) {
        DdoModel<T> globalModel = model.apply(problem);

        boolean dominanceUsed = !(globalModel.dominance() instanceof DefaultNoLayerDominanceChecker<T>);

        // A* tests
        Model<T> astarModelNoDom = wrapModel(globalModel, false);
        double aStarVal = solveAndChecksSolution(astarModelNoDom, "A*");

        if (dominanceUsed) {
            double aStarWithDominance = solveAndChecksSolution(globalModel, "A* (Dominance)");
            assertEquals(aStarVal, aStarWithDominance, 1e-10,
                    "A* : adding dominance change the value"
            );
        }

        // DDO tests
        DdoModel<T> ddoModelNoDom = wrapDdoModel(globalModel, false, false, null);
        double ddoVal = solveAndChecksSolution(ddoModelNoDom, "DDO");
        assertEquals(aStarVal, ddoVal, 1e-10,
                "A* solver and DDO solver do not return the same value."
        );

        if (dominanceUsed) {
            double ddoWithDominance = solveAndChecksSolution(globalModel, "DDO (Dominance)");
            assertEquals(ddoVal, ddoWithDominance, 1e-10,
                    "DDO: adding the dominance changes the value"
            );
        }

        double ddoWithCache = solveAndChecksSolution(wrapDdoModel(globalModel, dominanceUsed, true, null), "DDO (Cache)");
        assertEquals(ddoVal, ddoWithCache, 1e-10,
                "DDO: using cache changes the value"
        );

        for (int w = 60; w <= 100; w += 20) {
            double ddo = solveAndChecksSolution(wrapDdoModel(globalModel, dominanceUsed, false, w), "DDO (w=" + w + ")");
            assertEquals(ddoVal, ddo, 1e-10,
                    "DDO: using width %d changes the value".formatted(w)
            );
        }

        // ACS tests
        AcsModel<T> acsModelNoDom = wrapAcsModel(globalModel, false, null);
        double acsVal = solveAndChecksSolution(acsModelNoDom, "ACS");
        assertEquals(aStarVal, acsVal, 1e-10,
                "A* and ACS do not return the same value"
        );

        if (dominanceUsed) {
            AcsModel<T> acsModel = wrapAcsModel(globalModel, true, null);
            double acsWithDominance = solveAndChecksSolution(acsModel, "ACS (Dominance)");
            assertEquals(acsVal, acsWithDominance, 1e-10,
                    "ACS: the dominance change the value"
            );
        }

        for (int c = 6; c <= 20; c += 2) {
            double acs = solveAndChecksSolution(wrapAcsModel(globalModel, dominanceUsed, c), "ACS (c=" + c + ")");
            assertEquals(acsVal, acs, 1e-10,
                    "ACS: using column width %d changes the value".formatted(c)
            );
        }
    }

    public Stream<DynamicTest> generateTests() {
        return problems.stream().map(p ->
                DynamicTest.dynamicTest("Non Regression tests for " + p.toString(),
                        () -> testAllSolver(p))
        );
    }

    protected double solveAndChecksSolution(Model<T> model, String solverStr) {
        Solution solution;
        if (model instanceof DdoModel<T>) {
            solution = Solvers.minimizeDdo((DdoModel<T>) model);
        } else if (model instanceof AcsModel<T>) {
            solution = Solvers.minimizeAcs((AcsModel<T>) model);
        } else {
            solution = Solvers.minimizeAstar(model);
        }

        double value = solution.value();

        try {
            if (!Double.isInfinite(value)) {
                assertEquals(model.problem().evaluate(solution.solution()), value, 1e-10,
                        solverStr + ": The solution has not the same value that the returned value");
            }
        } catch (InvalidSolutionException e) {
            throw new RuntimeException(e);
        }

        return value;
    }

    protected Model<T> wrapModel(Model<T> base, boolean useDominance) {
        return new Model<T>() {
            @Override
            public Problem<T> problem() {
                return base.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return base.lowerBound();
            }

            @Override
            public NoLayerDominanceChecker<T> dominance() {
                return useDominance ? base.dominance() : new DefaultNoLayerDominanceChecker<>();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }
        };
    }

    protected DdoModel<T> wrapDdoModel(DdoModel<T> base, boolean useDominance, boolean useCache, Integer fixWidth) {
        return new DdoModel<T>() {
            @Override
            public Problem<T> problem() {
                return base.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return base.lowerBound();
            }

            @Override
            public NoLayerDominanceChecker<T> dominance() {
                return useDominance ? base.dominance() : new DefaultNoLayerDominanceChecker<>();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public Relaxation<T> relaxation() {
                return base.relaxation();
            }

            @Override
            public StateRanking<T> ranking() {
                return base.ranking();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return fixWidth != null ? new FixedWidth<>(fixWidth) : base.widthHeuristic();
            }

            @Override
            public ReductionStrategy<T> relaxStrategy() {
                return base.relaxStrategy();
            }

            @Override
            public ReductionStrategy<T> restrictStrategy() {
                return base.restrictStrategy();
            }

            @Override
            public boolean useCache() {
                return useCache;
            }
        };
    }

    protected AcsModel<T> wrapAcsModel(DdoModel<T> base, boolean useDominance, Integer fixWidth) {
        return new AcsModel<T>() {
            @Override
            public Problem<T> problem() {
                return base.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return base.lowerBound();
            }

            @Override
            public NoLayerDominanceChecker<T> dominance() {
                return useDominance ? base.dominance() : new DefaultNoLayerDominanceChecker<>();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public int columnWidth() {
                return fixWidth != null ? fixWidth : 5;
            }
        };
    }
}
