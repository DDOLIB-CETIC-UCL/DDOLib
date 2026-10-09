package org.ddolib.layered.testbench;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.ddolib.common.frontier.CutSetType;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.layered.modeling.AcsModel;
import org.ddolib.layered.modeling.AwAstarModel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DefaultDominanceChecker;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;
import org.junit.jupiter.api.DynamicTest;

/**
 * Test bench generating non-regression tests: on each instance, all the layered solvers (A*, DDO,
 * ACS and AWA*), with various configurations (dominance, cut-set type, cache, width, column width),
 * must return the same value, and every returned solution must be consistent with its value.
 *
 * @param <T> the type of states
 * @param <P> the type of problem to test
 */
public class NonRegressionTestBench<T, P extends Problem<T>> {

    protected final List<P> problems;
    private final Function<P, DdoModel<T>> model;

    /**
     * Creates a test bench for the instances and the model given by a data supplier.
     *
     * @param dataSupplier the supplier of the instances and of the model used to solve them
     */
    public NonRegressionTestBench(TestDataSupplier<T, P> dataSupplier) {
        problems = dataSupplier.generateProblems();
        model = dataSupplier::model;
    }

    /**
     * Solves the given problem with all the solvers and configurations and checks that they all
     * return the same value.
     *
     * @param problem the instance to test
     */
    protected void testAllSolver(P problem) {
        DdoModel<T> globalModel =
                model.apply(problem).setCutSetType(CutSetType.LastExactLayer).fixWidth(500);

        boolean dominanceUsed = !(globalModel.dominance() instanceof DefaultDominanceChecker<T>);

        Model<T> astarModel =
                new Model<>() {
                    @Override
                    public Problem<T> problem() {
                        return globalModel.problem();
                    }

                    @Override
                    public FastLowerBound<T> lowerBound() {
                        return globalModel.lowerBound();
                    }

                    @Override
                    public DominanceChecker<T> dominance() {
                        return globalModel.dominance();
                    }
                };

        double astarVal = solveAndChecksSolution(astarModel.disableDominance());

        if (dominanceUsed) {
            // No dominance rule defined for this problem. No need to test it.
            double astarWithDominance = solveAndChecksSolution(astarModel);
            assertEquals(
                    astarVal, astarWithDominance, 1e-10, "A* : adding dominance change the value");
        }

        double ddoVal = solveAndChecksSolution(globalModel.disableDominance());
        assertEquals(
                astarVal, ddoVal, 1e-10, "A* solver and DDO solver do not return the same value.");

        if (dominanceUsed) {
            // No dominance rule defined for this problem. No need to test it.
            double ddoWithDominance = solveAndChecksSolution(globalModel);
            assertEquals(
                    ddoVal, ddoWithDominance, 1e-10, "DDO: adding the dominance changes the value");
        }

        double ddoWithFrontier =
                solveAndChecksSolution(globalModel.setCutSetType(CutSetType.Frontier));
        assertEquals(
                ddoVal,
                ddoWithFrontier,
                1e-10,
                "DDO: using CutSetType.Frontier changes the value.");

        double ddoWithCache = solveAndChecksSolution(globalModel.useCache(true));
        assertEquals(ddoVal, ddoWithCache, 1e-10, "DDO: using cache changes the value");

        double ddoWithCacheAndFrontier =
                solveAndChecksSolution(
                        globalModel.setCutSetType(CutSetType.Frontier).useCache(true));
        assertEquals(
                ddoVal,
                ddoWithCacheAndFrontier,
                1e-10,
                "DDO: using cache and Frontier changes the value");

        for (int w = 600; w <= 1000; w += 100) {
            double ddo = solveAndChecksSolution(globalModel.fixWidth(w));
            assertEquals(ddoVal, ddo, 1e-10, "DDO: using width %d changes the value".formatted(w));
        }

        AcsModel<T> acsModel =
                new AcsModel<>() {
                    @Override
                    public Problem<T> problem() {
                        return globalModel.problem();
                    }

                    @Override
                    public FastLowerBound<T> lowerBound() {
                        return globalModel.lowerBound();
                    }

                    @Override
                    public DominanceChecker<T> dominance() {
                        return globalModel.dominance();
                    }
                };

        double acsVal = solveAndChecksSolution(acsModel.disableDominance());
        assertEquals(astarVal, acsVal, 1e-10, "A* and ACS do not return the same value");

        if (dominanceUsed) {
            // No dominance rule defined for this problem. No need to test it.
            double acsWithDominance = solveAndChecksSolution(acsModel);
            assertEquals(acsVal, acsWithDominance, 1e-10, "ACS: the dominance change the value");
        }

        for (int c = 6; c <= 20; c++) {
            double acs = solveAndChecksSolution(acsModel.setColumnWidth(c));
            assertEquals(
                    acsVal,
                    acs,
                    1e-10,
                    "ACS: using column width %d changes the value".formatted(c));
        }

        AwAstarModel<T> awAstarModel =
                new AwAstarModel<>() {
                    @Override
                    public Problem<T> problem() {
                        return globalModel.problem();
                    }

                    @Override
                    public FastLowerBound<T> lowerBound() {
                        return globalModel.lowerBound();
                    }

                    @Override
                    public DominanceChecker<T> dominance() {
                        return globalModel.dominance();
                    }
                };

        double awAstarVal = solveAndChecksSolution(awAstarModel.disableDominance());
        assertEquals(astarVal, awAstarVal, 1e-10, "A* and AWA* do not return the same value");
        if (dominanceUsed) {
            double awAStarWithDominance = solveAndChecksSolution(awAstarModel);
            assertEquals(
                    awAstarVal,
                    awAStarWithDominance,
                    1e-10,
                    "AWA*: the dominance change the value");
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
                                        "Non Regression tests for " + p.toString(),
                                        () -> testAllSolver(p)));
    }

    /**
     * Given a model, runs a solver and checks if the solution is coherent.
     *
     * @param model the model to test
     * @return the value of the obtained solution
     */
    private double solveAndChecksSolution(Model<T> model) {
        String solverStr;
        Solution solution = switch (model) {
            case DdoModel<T> ddoModel -> {
                solverStr = "DDO";
                yield Solvers.minimizeDdo(ddoModel);
            }
            case AcsModel<T> acsModel -> {
                solverStr = "ACS";
                yield Solvers.minimizeAcs(acsModel);
            }
            case AwAstarModel<T> awAstarModel -> {
                solverStr = "AWA*";
                yield Solvers.minimizeAwAStar(awAstarModel);
            }
            default -> {
                solverStr = "A*";
                yield Solvers.minimizeAstar(model);
            }
        };
        try {
            assertEquals(
                    model.problem().evaluate(solution.solution()),
                    solution.value(),
                    1e-10,
                    solverStr + ": The solution has not the same value that the returned value");
        } catch (InvalidSolutionException e) {
            throw new RuntimeException(e);
        }

        return solution.value();
    }
}
