package org.ddolib.util.testbench;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.modeling.*;
import org.junit.jupiter.api.DynamicTest;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NonRegressionTestBench<T, P extends Problem<T>> {

    protected final List<P> problems;
    private final Function<P, DdoModel<T>> model;

    public NonRegressionTestBench(TestDataSupplier<T, P> dataSupplier) {
        problems = dataSupplier.generateProblems();
        model = dataSupplier::model;
    }


    protected void testAllSolver(P problem) {
        DdoModel<T> globalModel =
                model.apply(problem).setCutSetType(CutSetType.LastExactLayer).fixWidth(500);

        boolean dominanceUsed = !(globalModel.dominance() instanceof DefaultDominanceChecker<T>);

        Model<T> astarModel = new Model<>() {
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

        double aStarVal = solveAndChecksSolution(astarModel.disableDominance());

        if (dominanceUsed) {
            // No dominance rule defined for this problem. No need to test it.
            double aStarWithDominance = solveAndChecksSolution(astarModel);
            assertEquals(aStarVal, aStarWithDominance, 1e-10,
                    "A* : adding dominance change the value"
            );
        }

        double ddoVal = solveAndChecksSolution(globalModel.disableDominance());
        assertEquals(aStarVal, ddoVal, 1e-10,
                "A* solver and DDO solver do not return the same value."
        );

        if (dominanceUsed) {
            // No dominance rule defined for this problem. No need to test it.
            double ddoWithDominance = solveAndChecksSolution(globalModel);
            assertEquals(ddoVal, ddoWithDominance, 1e-10,
                    "DDO: adding the dominance changes the value"
            );
        }

        double ddoWithFrontier =
                solveAndChecksSolution(globalModel.setCutSetType(CutSetType.Frontier));
        assertEquals(ddoVal, ddoWithFrontier, 1e-10,
                "DDO: using CutSetType.Frontier changes the value."
        );

        double ddoWithCache =
                solveAndChecksSolution(globalModel.useCache(true));
        assertEquals(ddoVal, ddoWithCache, 1e-10,
                "DDO: using cache changes the value"
        );


        double ddoWithCacheAndFrontier =
                solveAndChecksSolution(globalModel.setCutSetType(CutSetType.Frontier).useCache(true));
        assertEquals(ddoVal, ddoWithCacheAndFrontier, 1e-10,
                "DDO: using cache and Frontier changes the value"
        );


        for (int w = 600; w <= 1000; w += 100) {
            double ddo = solveAndChecksSolution(globalModel.fixWidth(w));
            assertEquals(ddoVal, ddo, 1e-10,
                    "DDO: using width " + w + " changes the value"
            );
        }

        AcsModel<T> acsModel = new AcsModel<>() {
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
        assertEquals(aStarVal, acsVal, 1e-10,
                "A* and ACS do not return the same value"
        );

        if (dominanceUsed) {
            // No dominance rule defined for this problem. No need to test it.
            double acsWithDominance = solveAndChecksSolution(acsModel);
            assertEquals(acsVal, acsWithDominance, 1e-10,
                    "ACS: the dominance change the value"
            );
        }

        for (int c = 6; c <= 20; c++) {
            double acs = solveAndChecksSolution(acsModel.setColumnWidth(c));
            assertEquals(acsVal, acs, 1e-10,
                    "ACS: using column width " + c + " changes the value");
        }
    }

    public Stream<DynamicTest> generateTests() {
        return problems.stream().map(p ->
                DynamicTest.dynamicTest("Non Regression tests for " + p.toString(),
                        () -> testAllSolver(p))
        );
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
            default -> {
                solverStr = "A*";
                yield Solvers.minimizeAstar(model);
            }
        };
        try {
            assertEquals(model.problem().evaluate(solution.solution()), solution.value(), 1e-10,
                    solverStr + ": The solution has not the same value that the returned value");
        } catch (InvalidSolutionException e) {
            throw new RuntimeException(e);
        }

        return solution.value();
    }
}
