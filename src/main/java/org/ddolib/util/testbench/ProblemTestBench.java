package org.ddolib.util.testbench;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.junit.jupiter.api.DynamicTest;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Abstract class to generate tests on implementations of {@link Problem}. The user needs to implement an instance
 * generator and a {@link SolverConfig}.
 *
 * @param <T> The type of states.
 * @param <P> The type of problem to test.
 */
public abstract class ProblemTestBench<T, P extends Problem<T>> {

    /**
     * List of problems used for tests.
     */
    protected final List<P> problems;

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
     * Generates {@link Problem} instances to test.
     *
     * @return A list of problems used for tests.
     */
    abstract protected List<P> generateProblems();


    /**
     * Given a problem instance returns the whole model used to solve this problem.
     *
     * @param problem The problem to solve.
     * @return A model containing all the component to solve it.
     */
    abstract protected DdoModel<T> model(P problem);

    /**
     * Instantiate a test bench.
     */
    protected ProblemTestBench() {
        problems = generateProblems();
    }


    /**
     * Test if the exact mdd generated for the input problem lead to optimal solution.
     * <p>
     *
     * @param problem The instance to test.
     */
    protected void testTransitionModel(P problem) {

        Model<T> testModel = () -> problem;


        Solvers<T> solver = new Solvers<>();
        Double best = solver.minimizeExact(testModel).incumbent();
        Optional<Double> returned = best.isInfinite() ? Optional.empty() : Optional.of(best);
        assertOptionalDoubleEqual(problem.optimalValue(), returned, 1e-10);
    }

    /**
     * Test if the fast lower bound is a lower bound for the root node and if the compilation with only the fast lower
     * bound enabled lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testFlb(P problem) {

        DdoModel<T> globalModel = model(problem);

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
        };


        Solvers<T> solver = new Solvers<>();
        Double best = solver.minimizeExact(testModel).incumbent();
        Optional<Double> returned = best.isInfinite() ? Optional.empty() : Optional.of(best);
        assertOptionalDoubleEqual(problem.optimalValue(), returned, 1e-10);


    }

    /**
     * Test if the model only with relaxation enabled lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testRelaxation(P problem) {

        DdoModel<T> globalModel = model(problem);
        Function<Integer, DdoModel<T>> getModel = (w) -> new DdoModel<T>() {
            @Override
            public Relaxation<T> relaxation() {
                return globalModel.relaxation();
            }

            @Override
            public Problem<T> problem() {
                return problem;
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return new FixedWidth<>(w);
            }
        };
        for (int w = minWidth; w <= maxWidth; w++) {
            try {
                DdoModel<T> testModel = getModel.apply(w);

                Solvers<T> solver = new Solvers<>();
                Double best = solver.minimizeExact(testModel).incumbent();
                Optional<Double> returned = best.isInfinite() ? Optional.empty() : Optional.of(best);
                assertOptionalDoubleEqual(problem.optimalValue(), returned, 1e-10);
            } catch (Exception e) {
                String msg = String.format("Max width of the MDD: %d\n", w) + e.getMessage();
                throw new RuntimeException(msg);
            }
        }
    }

    /**
     * Test if using the cache lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testCache(P problem) {
        DdoModel<T> globalModel = model(problem);
        Function<Integer, DdoModel<T>> getModel = (w) -> new DdoModel<T>() {
            @Override
            public Relaxation<T> relaxation() {
                return globalModel.relaxation();
            }

            @Override
            public Problem<T> problem() {
                return problem;
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
            public DominanceChecker<T> dominance() {
                return globalModel.dominance();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return globalModel.lowerBound();
            }
        };
        for (int w = minWidth; w <= maxWidth; w++) {
            try {
                DdoModel<T> testModel = getModel.apply(w);

                Solvers<T> solver = new Solvers<>();
                Double best = solver.minimizeExact(testModel).incumbent();
                Optional<Double> returned = best.isInfinite() ? Optional.empty() : Optional.of(best);
                assertOptionalDoubleEqual(problem.optimalValue(), returned, 1e-10);
            } catch (Exception e) {
                String msg = String.format("Max width of the MDD: %d\n", w) + e.getMessage();
                throw new RuntimeException(msg);
            }
        }
    }

    /**
     * Test if the A* solver reaches the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testAStarSolver(P problem) {

        Model<T> testModel = model(problem);

        Solvers<T> solver = new Solvers<>();
        Double best = solver.minimizeAstar(testModel).incumbent();
        Optional<Double> returned = best.isInfinite() ? Optional.empty() : Optional.of(best);
        assertOptionalDoubleEqual(problem.optimalValue(), returned, 1e-10);
    }


    /**
     * Test if the ACS solver reaches the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testACSSolver(P problem) {

        Model<T> globalModel = model(problem);

        AcsModel<T> testModel = new AcsModel<T>() {
            @Override
            public Problem<T> problem() {
                return problem;
            }

            @Override
            public DominanceChecker<T> dominance() {
                return globalModel.dominance();
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return globalModel.lowerBound();
            }
        };

        Solvers<T> solver = new Solvers<>();
        Double best = solver.minimizeAcs(testModel).incumbent();
        Optional<Double> returned = best.isInfinite() ? Optional.empty() : Optional.of(best);
        assertOptionalDoubleEqual(problem.optimalValue(), returned, 1e-10);
    }

    /**
     * Test if the mode with the relaxation and the fast lower bound enabled lead to the optimal solution. As side
     * effect, it tests if the fast lower bound on merged states does not cause errors.
     *
     * @param problem The instance to test.
     */
    protected void testFlbOnRelaxedNodes(P problem) {
        DdoModel<T> globalModel = model(problem);
        Function<Integer, DdoModel<T>> getModel = (w) -> new DdoModel<T>() {
            @Override
            public Relaxation<T> relaxation() {
                return globalModel.relaxation();
            }

            @Override
            public Problem<T> problem() {
                return problem;
            }

            @Override
            public WidthHeuristic<T> widthHeuristic() {
                return new FixedWidth<>(w);
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return globalModel.lowerBound();
            }
        };
        for (int w = minWidth; w <= maxWidth; w++) {
            try {
                DdoModel<T> testModel = getModel.apply(w);

                Solvers<T> solver = new Solvers<>();
                Double best = solver.minimizeExact(testModel).incumbent();
                Optional<Double> returned = best.isInfinite() ? Optional.empty() : Optional.of(best);
                assertOptionalDoubleEqual(problem.optimalValue(), returned, 1e-10);
            } catch (Exception e) {
                String msg = String.format("Max width of the MDD: %d\n", w) + e.getMessage();
                throw new RuntimeException(msg);
            }
        }
    }

    /**
     * Test if the model with the dominance enabled lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testDominance(P problem) {
        DdoModel<T> globalModel = model(problem);

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


        Solvers<T> solver = new Solvers<>();
        Double best = solver.minimizeExact(testModel).incumbent();
        Optional<Double> returned = best.isInfinite() ? Optional.empty() : Optional.of(best);
        assertOptionalDoubleEqual(problem.optimalValue(), returned, 1e-10);
    }

    /**
     * Generates all the tests for all the generated instances.
     *
     * @return A stream of tests over all the generated instances.
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

    /**
     * Compares two {@code Optional<Double>} with a tolerance (delta) if both are present.
     *
     * @param expected The expected {@code Optional<Double>}.
     * @param actual   The actual {@code Optional<Double>}.
     * @param delta    The tolerance for the comparison if both optionals contain a value.
     * @param width    The maximum width of mdd used for the tests. Used to display error message.
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
     * @param expected The expected {@code Optional<Double>}.
     * @param actual   The actual {@code Optional<Double>}.
     * @param delta    The tolerance for the comparison if both optionals contain a value.
     */
    public static void assertOptionalDoubleEqual(Optional<Double> expected,
                                                 Optional<Double> actual,
                                                 double delta) {
        assertOptionalDoubleEqual(expected, actual, delta, -1);
    }
}
