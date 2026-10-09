package org.ddolib.nolayer.testbench;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.common.util.verbosity.VerbosityLevel;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.nolayer.modeling.AcsModel;
import org.ddolib.nolayer.modeling.AwAstarModel;
import org.ddolib.nolayer.modeling.DdoModel;
import org.ddolib.nolayer.modeling.DefaultNoLayerDominanceChecker;
import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;
import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.nolayer.modeling.Relaxation;
import org.ddolib.nolayer.modeling.Solvers;
import org.ddolib.nolayer.solver.Solution;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.ReductionStrategy;
import org.junit.jupiter.api.DynamicTest;

/**
 * Test bench for the no-layer API: on each instance, the A*, ACS and AWA* solvers, with and without
 * dominance and with various column widths, must return the same value, and every returned solution
 * must be consistent with its value.
 *
 * @param <T> the type of states
 * @param <P> the type of problem to test
 */
public class NoLayerTestBench<T, P extends Problem<T>> {

    protected final List<P> problems;
    private final Function<P, DdoModel<T>> model;

    /**
     * Creates a test bench for the instances and the model given by a data supplier.
     *
     * @param dataSupplier the supplier of the instances and of the model used to solve them
     */
    public NoLayerTestBench(NoLayerTestDataSupplier<T, P> dataSupplier) {
        problems = dataSupplier.generateProblems();
        model = dataSupplier::model;
    }

    /**
     * Solves the given problem with all the tested solvers and configurations and checks that they
     * all return the same value.
     *
     * @param problem the instance to test
     */
    protected void testAllSolver(P problem) {
        DdoModel<T> globalModel = model.apply(problem);

        boolean dominanceUsed =
                !(globalModel.dominance() instanceof DefaultNoLayerDominanceChecker<T>);

        // A* tests
        Model<T> astarModelNoDom = wrapModel(globalModel, false);
        double astarVal = solveAndChecksSolution(astarModelNoDom, "A*");

        if (dominanceUsed) {
            double astarWithDominance = solveAndChecksSolution(globalModel, "A* (Dominance)");
            assertEquals(
                    astarVal, astarWithDominance, 1e-10, "A* : adding dominance change the value");
        }

        /* // DDO tests
        DdoModel<T> ddoModelNoDom = wrapDdoModel(globalModel, false, false, null);
        double ddoVal = solveAndChecksSolution(ddoModelNoDom, "DDO");
        assertEquals(astarVal, ddoVal, 1e-10,
                "A* solver and DDO solver do not return the same value."
        );

        if (dominanceUsed) {
            double ddoWithDominance = solveAndChecksSolution(globalModel, "DDO (Dominance)");
            assertEquals(ddoVal, ddoWithDominance, 1e-10,
                    "DDO: adding the dominance changes the value"
            );
        }

        double ddoWithCache = solveAndChecksSolution(
                wrapDdoModel(globalModel, dominanceUsed, true, null), "DDO (Cache)");
        assertEquals(ddoVal, ddoWithCache, 1e-10,
                "DDO: using cache changes the value"
        );

        for (int w = 60; w <= 100; w += 20) {
            double ddo = solveAndChecksSolution(
                    wrapDdoModel(globalModel, dominanceUsed, false, w), "DDO (w=" + w + ")");
            assertEquals(ddoVal, ddo, 1e-10,
                    "DDO: using width %d changes the value".formatted(w)
            );
        }*/

        // ACS tests
        AcsModel<T> acsModelNoDom = wrapAcsModel(globalModel, false, null);
        double acsVal = solveAndChecksSolution(acsModelNoDom, "ACS");
        assertEquals(astarVal, acsVal, 1e-10, "A* and ACS do not return the same value");

        if (dominanceUsed) {
            AcsModel<T> acsModel = wrapAcsModel(globalModel, true, null);
            double acsWithDominance = solveAndChecksSolution(acsModel, "ACS (Dominance)");
            assertEquals(acsVal, acsWithDominance, 1e-10, "ACS: the dominance change the value");
        }

        for (int c = 6; c <= 20; c += 2) {
            double acs =
                    solveAndChecksSolution(
                            wrapAcsModel(globalModel, dominanceUsed, c), "ACS (c=" + c + ")");
            assertEquals(
                    acsVal,
                    acs,
                    1e-10,
                    "ACS: using column width %d changes the value".formatted(c));
        }

        // AWA* tests
        AwAstarModel<T> awAstarModel = wrapAwAStarModel(globalModel, false);
        double awAstarVal = solveAndChecksSolution(awAstarModel, "AWA*");
        assertEquals(astarVal, awAstarVal, 1e-10, "A* and AWA* do not return the same value");
        if (dominanceUsed) {
            AwAstarModel<T> awAstarModelDom = wrapAwAStarModel(globalModel, true);
            double awastar = solveAndChecksSolution(awAstarModelDom, "AWA* (Dominance)");
            assertEquals(awAstarVal, awastar, "AWA*: the dominance changes the value");
        }
    }

    /**
     * Generates one dynamic test per instance, running {@link #testAllSolver(Problem)} on it.
     *
     * @return the stream of generated tests
     */
    public Stream<DynamicTest> generateTests() {
        return problems.stream()
                .map(
                        p ->
                                DynamicTest.dynamicTest(
                                        "Unit tests for " + p.toString(), () -> testAllSolver(p)));
    }

    /**
     * Solves the given model with the solver matching its type (ACS, AWA*, DDO or A* by default)
     * and checks that the returned solution evaluates to the returned value.
     *
     * @param model the model to solve
     * @param solverStr the name of the solver, used in the assertion messages
     * @return the value of the obtained solution
     */
    protected double solveAndChecksSolution(Model<T> model, String solverStr) {
        Solution solution = switch (model) {
            case AcsModel<T> acsModel -> Solvers.minimizeAcs(acsModel);
            case AwAstarModel<T> awAstarModel -> Solvers.minimizeAwAstar(awAstarModel);
            case DdoModel<T> ddoModel -> Solvers.minimizeDdo(ddoModel);
            default -> Solvers.minimizeAstar(model);
        };

        double value = solution.value();

        try {
            if (!Double.isInfinite(value)) {
                assertEquals(
                        model.problem().evaluate(solution.solution()),
                        value,
                        1e-10,
                        solverStr
                                + ": The solution has not the same value that the returned value");
            }
        } catch (InvalidSolutionException e) {
            throw new RuntimeException(e);
        }

        return value;
    }

    /**
     * Wraps a model into a silent A* model in debug mode.
     *
     * @param base the model to wrap
     * @param useDominance whether the dominance of {@code base} is used
     * @return the wrapped model
     */
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

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };
    }

    /**
     * Wraps a DDO model into a silent DDO model in debug mode with the given options.
     *
     * @param base the model to wrap
     * @param useDominance whether the dominance of {@code base} is used
     * @param useCache whether the cache is used
     * @param fixWidth the fixed maximum width of the diagrams, or {@code null} to keep the width
     *     heuristic of {@code base}
     * @return the wrapped model
     */
    protected DdoModel<T> wrapDdoModel(
            DdoModel<T> base, boolean useDominance, boolean useCache, Integer fixWidth) {
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

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };
    }

    /**
     * Wraps a DDO model into a silent ACS model in debug mode.
     *
     * @param base the model to wrap
     * @param useDominance whether the dominance of {@code base} is used
     * @param fixWidth the column width, or {@code null} to use a column width of 5
     * @return the wrapped model
     */
    protected AcsModel<T> wrapAcsModel(DdoModel<T> base, boolean useDominance, Integer fixWidth) {
        return new AcsModel<>() {
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

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };
    }

    /**
     * Wraps a DDO model into a silent AWA* model in debug mode.
     *
     * @param base the model to wrap
     * @param useDominance whether the dominance of {@code base} is used
     * @return the wrapped model
     */
    protected AwAstarModel<T> wrapAwAStarModel(DdoModel<T> base, boolean useDominance) {
        return new AwAstarModel<T>() {
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
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }
        };
    }
}
