package org.ddolib.util.testbench;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.modeling.*;
import org.junit.jupiter.api.DynamicTest;

import java.util.Arrays;
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

        double aStarVal = solveWithAStar(globalModel.disableDominance());

        if (!(globalModel.dominance() instanceof DefaultDominanceChecker<T>)) {
            // No dominance rule defined for this problem. No need to test it.
            double aStarWithDominance = solveWithAStar(globalModel);
            assertEquals(aStarVal, aStarWithDominance, 1e-10,
                    "A* : adding dominance change the value"
            );
        }

        double ddoVal = solveWithDdo(globalModel.disableDominance());
        assertEquals(aStarVal, ddoVal, 1e-10,
                "A* solver and DDO solver do not return the same value."
        );

        if (!(globalModel.dominance() instanceof DefaultDominanceChecker<T>)) {
            // No dominance rule defined for this problem. No need to test it.
            double ddoWithDominance = solveWithDdo(globalModel);
            assertEquals(ddoVal, ddoWithDominance, 1e-10,
                    "DDO: adding the dominance changes the value"
            );
        }

        double ddoWithFrontier =
                solveWithDdo(globalModel.setCutSetType(CutSetType.Frontier));
        assertEquals(ddoVal, ddoWithFrontier, 1e-10,
                "DDO: using CutSetType.Frontier changes the value."
        );

        double ddoWithCache =
                solveWithDdo(globalModel.useCache(true));
        assertEquals(ddoVal, ddoWithCache, 1e-10,
                "DDO: using cache changes the value"
        );


        double ddoWithCacheAndFrontier =
                solveWithDdo(globalModel.setCutSetType(CutSetType.Frontier).useCache(true));
        assertEquals(ddoVal, ddoWithCacheAndFrontier, 1e-10,
                "DDO: using cache and Frontier changes the value"
        );


        for (int w = 600; w <= 1000; w += 100) {
            double ddo = solveWithDdo(globalModel.fixWidth(w));
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

        double acsVal = solveWithAcs(acsModel.disableDominance());
        assertEquals(aStarVal, acsVal, 1e-10,
                "A* and ACS do not return the same value"
        );

        if (!(globalModel.dominance() instanceof DefaultDominanceChecker<T>)) {
            // No dominance rule defined for this problem. No need to test it.
            double acsWithDominance = solveWithAcs(acsModel);
            assertEquals(acsVal, acsWithDominance, 1e-10,
                    "ACS: the dominance change the value"
            );
        }

        for (int c = 6; c <= 20; c++) {
            double acs = solveWithAcs(acsModel.setColumnWidth(c));
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

    private double solveWithAStar(Model<T> model) {
        int[] solution = new int[model.problem().nbVars()];
        double value =
                Solvers.minimizeAstar(model,
                        (sol, s) -> Arrays.setAll(solution, i -> sol[i])).incumbent();
        try {
            assertEquals(model.problem().evaluate(solution), value, 1e-10,
                    "A*: The solution has not the same value that the returned value");
        } catch (InvalidSolutionException e) {
            throw new RuntimeException(e);
        }

        return value;
    }

    private double solveWithDdo(DdoModel<T> model) {
        int[] solution = new int[model.problem().nbVars()];
        double value = Solvers.minimizeDdo(model,
                (sol, s) -> Arrays.setAll(solution, i -> sol[i])).incumbent();


        try {
            assertEquals(model.problem().evaluate(solution), value, 1e-10,
                    "DDO: The solution has not the same value that the returned value");
        } catch (InvalidSolutionException e) {
            throw new RuntimeException(e);
        }

        return value;
    }

    private double solveWithAcs(AcsModel<T> model) {
        int[] solution = new int[model.problem().nbVars()];
        double value = Solvers.minimizeAcs(model,
                (sol, s) -> Arrays.setAll(solution, i -> sol[i])).incumbent();


        try {
            assertEquals(model.problem().evaluate(solution), value, 1e-10,
                    "ACS: The solution has not the same value that the returned value");
        } catch (InvalidSolutionException e) {
            throw new RuntimeException(e);
        }

        return value;
    }
}
