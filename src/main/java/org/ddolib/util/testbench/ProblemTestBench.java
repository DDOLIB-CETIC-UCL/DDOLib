package org.ddolib.util.testbench;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.debug.DebugLevel;
import org.junit.jupiter.api.DynamicTest;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Abstract class to generate tests on implementations of {@link Problem}. The user needs to implement an instance
 * generator and a {@link DdoModel} containing all the problem specific components.
 *
 * @param <T> The type of states.
 * @param <P> The type of problem to test.
 */
public class ProblemTestBench<T, P extends Problem<T>> {

    /**
     * List of problems used for tests.
     */
    private final List<P> problems;
    private final Function<P, DdoModel<T>> model;
    /**
     * Whether the relaxation must be tested.
     */
    public boolean testRelaxation = false;
    /**
     * Whether the fast lower bound must be tested.
     */
    public boolean testFLB = false;
    /**
     * Whether the dominance must be tested.
     */
    public boolean testDominance = false;
    /**
     * Whether the cache has to be tested.
     */
    public boolean testCache = false;
    /**
     * The minimum width of mdd to test with the relaxation.
     */
    public int minWidth = 2;
    /**
     * The maximum width of mdd to test with the relaxation.
     */
    public int maxWidth = 20;


    /**
     * Instantiate a test bench.
     */
    public ProblemTestBench(TestDataSupplier<T, P> dataSupplier) {
        problems = dataSupplier.generateProblems();
        model = dataSupplier::model;
    }


    /**
     * Compares two {@code Optional<Double>} with a tolerance (delta) if both are present.
     *
     * @param expected the expected {@code Optional<Double>}
     * @param actual   the actual {@code Optional<Double>}
     * @param delta    the tolerance for the comparison if both optionals contain a value
     * @param width    the maximum width of mdd used for the tests. Used to display error message
     */
    public static void assertOptionalDoubleEqual(Optional<Double> expected,
                                                 Optional<Double> actual,
                                                 double delta,
                                                 int width) {
        String failureMsg = width > 0 ? String.format("Max width of the MDD: %d", width) : "";
        if (expected.isPresent() && actual.isPresent()) {
            assertEquals(expected.get(), actual.get(), delta, failureMsg);
        } else {
            assertEquals(expected, actual, failureMsg);
        }
    }

    /**
     * Compares two {@code Optional<Double>} with a tolerance (delta) if both are present.
     *
     * @param expected the expected {@code Optional<Double>}
     * @param actual   the actual {@code Optional<Double>}
     * @param delta    the tolerance for the comparison if both optionals contain a value
     */
    public static void assertOptionalDoubleEqual(Optional<Double> expected,
                                                 Optional<Double> actual,
                                                 double delta) {
        assertOptionalDoubleEqual(expected, actual, delta, -1);
    }

    /**
     * Given a model runs a solver and checks if the solution is the expected one.
     *
     * @param model the model to test
     * @param width the max width of the diagram ; -1 if the solver does not use the width
     * @throws InvalidSolutionException if the returned solution is not well evaluated
     */
    private void testSolverResult(Model<T> model, int width) throws InvalidSolutionException {
        Solution bestSolution = switch (model) {
            case DdoModel<T> ddoModel when width == -1 -> Solvers.minimizeExact(ddoModel);
            case DdoModel<T> ddoModel -> Solvers.minimizeDdo(ddoModel);
            case AcsModel<T> acsModel -> Solvers.minimizeAcs(acsModel);
            default -> Solvers.minimizeAstar(model);
        };

        Problem<T> problem = model.problem();
        double bestValue = bestSolution.value();
        Optional<Double> optBestVal = Double.isInfinite(bestValue) ? Optional.empty() : Optional.of(bestValue);
        assertOptionalDoubleEqual(problem.optimalValue(), optBestVal, 1e-10, width);

        if (problem.optimalValue().isPresent()) {
            assertEquals(problem.optimalValue().get(), problem.evaluate(bestSolution.solution()), 1e-10);
        }
    }

    private void testSolverResult(Model<T> model) throws InvalidSolutionException {
        testSolverResult(model, -1);
    }

    /**
     * Test if the exact mdd generated for the input problem lead to optimal solution.
     * <p>
     *
     * @param problem the instance to test
     */
    private void testTransitionModel(P problem) throws InvalidSolutionException {

        DdoModel<T> globalModel = model.apply(problem);

        DdoModel<T> testModel = new DdoModel<T>() {
            @Override
            public Relaxation<T> relaxation() {
                return globalModel.relaxation();
            }

            @Override
            public Problem<T> problem() {
                return problem;
            }
        };

        testSolverResult(testModel);
    }

    /**
     * Test if the fast lower bound is a lower bound for the root node and if the compilation with only the fast lower
     * bound enabled lead to the optimal solution.
     *
     * @param problem the instance to test
     */
    private void testFlb(P problem) throws InvalidSolutionException {

        DdoModel<T> globalModel = model.apply(problem);

        DdoModel<T> testModel = new DdoModel<>() {
            @Override
            public Relaxation<T> relaxation() {
                return globalModel.relaxation();
            }

            @Override
            public Problem<T> problem() {
                return globalModel.problem();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return globalModel.lowerBound();
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };

        testSolverResult(testModel);

    }

    /**
     * Test if the model only with relaxation enabled lead to the optimal solution.
     *
     * @param problem the instance to test
     */
    private void testRelaxation(P problem) {

        DdoModel<T> globalModel = model.apply(problem);
        Function<Integer, DdoModel<T>> getModel = (w) -> new DdoModel<T>() {
            @Override
            public Relaxation<T> relaxation() {
                return globalModel.relaxation();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return new FixedWidth<>(w);
            }

            @Override
            public Problem<T> problem() {
                return problem;
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };
        for (int w = minWidth; w <= maxWidth; w++) {
            try {
                testSolverResult(getModel.apply(w), w);
            } catch (Exception e) {
                String msg = String.format("Max width of the MDD: %d\n", w) + e.getMessage();
                throw new RuntimeException(msg);
            }
        }
    }

    /**
     * Test if using the cache lead to the optimal solution.
     *
     * @param problem the instance to test
     */
    private void testCache(P problem) {
        DdoModel<T> globalModel = model.apply(problem);
        Function<Integer, DdoModel<T>> getModel = (w) -> new DdoModel<>() {
            @Override
            public Relaxation<T> relaxation() {
                return globalModel.relaxation();
            }

            @Override
            public StateRanking<T> ranking() {
                return globalModel.ranking();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return new FixedWidth<>(w);
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public Problem<T> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return globalModel.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return globalModel.dominance();
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };
        for (int w = minWidth; w <= maxWidth; w++) {
            try {
                testSolverResult(getModel.apply(w), w);
            } catch (Exception e) {
                String msg = String.format("Max width of the MDD: %d\n", w) + e.getMessage();
                throw new RuntimeException(msg);
            }
        }
    }

    /**
     * Test if the A* solver reaches the optimal solution.
     *
     * @param problem the instance to test
     */
    private void testAStarSolver(P problem) throws InvalidSolutionException {

        DdoModel<T> globalModel = model.apply(problem);

        Model<T> testModel = new Model<T>() {
            @Override
            public Problem<T> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return globalModel.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return globalModel.dominance();
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };

        testSolverResult(testModel);
    }

    /**
     * Test if the ACS solver reaches the optimal solution.
     *
     * @param problem the instance to test
     */
    private void testACSSolver(P problem) throws InvalidSolutionException {

        Model<T> globalModel = model.apply(problem);

        AcsModel<T> testModel = new AcsModel<>() {
            @Override
            public Problem<T> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return globalModel.lowerBound();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return globalModel.dominance();
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };

        testSolverResult(testModel);
    }

    /**
     * Test if the mode with the relaxation and the fast lower bound enabled lead to the optimal solution. As side
     * effect, it tests if the fast lower bound on merged states does not cause errors.
     *
     * @param problem the instance to test
     */
    private void testFlbOnRelaxedNodes(P problem) {
        DdoModel<T> globalModel = model.apply(problem);
        Function<Integer, DdoModel<T>> getModel = (w) -> new DdoModel<T>() {
            @Override
            public Relaxation<T> relaxation() {
                return globalModel.relaxation();
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return new FixedWidth<>(w);
            }

            @Override
            public Problem<T> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return globalModel.lowerBound();
            }
        };
        for (int w = minWidth; w <= maxWidth; w++) {
            try {
                testSolverResult(getModel.apply(w), w);
            } catch (Exception e) {
                String msg = String.format("Max width of the MDD: %d\n", w) + e.getMessage();
                throw new RuntimeException(msg);
            }
        }
    }

    /**
     * Test if the model with the dominance enabled lead to the optimal solution.
     *
     * @param problem the instance to test
     */
    private void testDominance(P problem) throws InvalidSolutionException {
        DdoModel<T> globalModel = model.apply(problem);

        DdoModel<T> testModel = new DdoModel<>() {
            @Override
            public Relaxation<T> relaxation() {
                return globalModel.relaxation();
            }

            @Override
            public Problem<T> problem() {
                return globalModel.problem();
            }

            @Override
            public DominanceChecker<T> dominance() {
                return globalModel.dominance();
            }
        };


    }

    /**
     * Generates all the tests for all the generated instances.
     *
     * @return a stream of tests over all the generated instances
     */
    public Stream<DynamicTest> generateTests() {

        Stream<DynamicTest> allTests = Stream.empty();

        Stream<DynamicTest> modelTests = problems.stream().map(p ->
                DynamicTest.dynamicTest(String.format("Model for %s", p.toString()), () -> testTransitionModel(p))
        );

        allTests = Stream.concat(allTests, modelTests);

        if (testRelaxation) {
            Stream<DynamicTest> relaxTests = problems.stream().map(p ->
                    DynamicTest.dynamicTest(String.format("Relaxation for %s", p.toString()), () -> testRelaxation(p))
            );
            allTests = Stream.concat(allTests, relaxTests);
        }
        if (testFLB) {
            Stream<DynamicTest> flbTests = problems.stream().map(p ->
                    DynamicTest.dynamicTest(String.format("FLB for %s", p.toString()), () -> testFlb(p))
            );
            allTests = Stream.concat(allTests, flbTests);
        }

        if (testRelaxation && testFLB) {
            Stream<DynamicTest> relaxAndFlbTest = problems.stream().map(p ->
                    DynamicTest.dynamicTest(String.format("Relax and FLB for %s", p.toString()), () -> testFlbOnRelaxedNodes(p))
            );
            allTests = Stream.concat(allTests, relaxAndFlbTest);
        }

        if (testDominance) {
            Stream<DynamicTest> dominanceTests = problems.stream().map(p ->
                    DynamicTest.dynamicTest(String.format("Dominance for %s", p.toString()), () -> testDominance(p))
            );
            allTests = Stream.concat(allTests, dominanceTests);
        }

        if (testCache) {
            Stream<DynamicTest> cacheTests = problems.stream().map(p ->
                    DynamicTest.dynamicTest(String.format("Cache for %s", p.toString()), () -> testCache(p))
            );
            allTests = Stream.concat(allTests, cacheTests);
        }

        Stream<DynamicTest> aStarTests = problems.stream().map(p ->
                DynamicTest.dynamicTest(String.format("A* for %s", p.toString()), () -> testAStarSolver(p))
        );
        allTests = Stream.concat(allTests, aStarTests);

        Stream<DynamicTest> acsTests = problems.stream().map(p ->
                DynamicTest.dynamicTest(String.format("ACS for %s", p.toString()), () -> testACSSolver(p))
        );
        allTests = Stream.concat(allTests, acsTests);

        return allTests;
    }
}
