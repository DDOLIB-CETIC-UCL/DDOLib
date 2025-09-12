package org.ddolib.util.testbench;

import org.ddolib.astar.core.solver.ACSSolver;
import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.astar.core.solver.BestFirstSearchSolver;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.ExactSolver;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.modeling.DefaultFastUpperBound;
import org.ddolib.modeling.Problem;
import org.junit.jupiter.api.DynamicTest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Abstract class to generate tests on implementations of {@link Problem}. The user needs to implement an instance
 * generator and a {@link SolverConfig}.
 *
 * @param <T> The type of states.
 * @param <K> The type of dominance keys.
 * @param <P> The type of problem to test.
 */
public abstract class ProblemTestBench<T, K, P extends Problem<T>> {

    /**
     * List of problems used for tests.
     */
    protected final List<P> problems;

    /**
     * Whether the relaxation must be tested.
     */
    public boolean testRelaxation = false;

    /**
     * Whether the fast upper bound must be tested.
     */
    public boolean testFUB = false;

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
     * Given a problem, returns the solver's inputs (relaxation, dominance, frontier,...).
     *
     * @param problem A problem to solve during the tests.
     * @return The solver's inputs (relaxation, dominance, frontier,...).
     */
    abstract protected SolverConfig<T, K> configSolver(P problem);


    /**
     * Instantiate a test bench.
     */
    protected ProblemTestBench() {
        problems = generateProblems();
    }


    /**
     * Test if the exact mdd generated for the input problem lead to optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testTransitionModel(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        config.fub = new DefaultFastUpperBound<>();
        config.dominance = new DefaultDominanceChecker<>();

        Solver solver = new ExactSolver<>(config);
        solver.maximize();
        assertOptionalDoubleEqual(problem.optimalValue(), solver.bestValue(), 1e-10);
    }

    /**
     * Test if the fast upper bound is an upper bound for the root node and if the compilation with only the fast upper
     * bound enabled lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testFub(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        config.dominance = new DefaultDominanceChecker<>();
        config.debugLevel = 1;
        Solver solver = new ExactSolver<>(config);

        solver.maximize();
        assertOptionalDoubleEqual(problem.optimalValue(), solver.bestValue(), 1e-10);
    }

    /**
     * Test if the model only with relaxation enabled lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testRelaxation(P problem) {
        for (int w = minWidth; w <= maxWidth; w++) {
            SolverConfig<T, K> config = configSolver(problem);
            config.dominance = new DefaultDominanceChecker<>();
            config.fub = new DefaultFastUpperBound<>();
            config.width = new FixedWidth<>(w);
            Solver solver = new SequentialSolver<>(config);

            solver.maximize();
            assertOptionalDoubleEqual(problem.optimalValue(), solver.bestValue(), 1e-10, w);
        }
    }

    /**
     * Test if using the cache lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testCache(P problem) {
        for (int w = minWidth; w <= maxWidth; w++) {
            SolverConfig<T, K> config = configSolver(problem);
            config.width = new FixedWidth<>(w);
            config.cache = new SimpleCache<>();
            Solver solver = new SequentialSolver<>(config);

            solver.maximize();
            assertOptionalDoubleEqual(problem.optimalValue(), solver.bestValue(), 1e-10, w);
        }
    }

    /**
     * Test if the A* solver reaches the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testAStarSolver(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        Solver solver = new AStarSolver<>(config);

        solver.maximize();
        assertOptionalDoubleEqual(problem.optimalValue(), solver.bestValue(), 1e-10);
    }


    /**
     * Test if the ACS solver reaches the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testACSSolver(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        Solver solver = new ACSSolver<>(config, 4);

        solver.maximize();
        assertOptionalDoubleEqual(problem.optimalValue(), solver.bestValue(), 1e-10);
    }

    /**
     * Test if the Best First search solver reaches the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testBestFirstSearch(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        Solver solver = new BestFirstSearchSolver<>(config);
        solver.maximize();

        assertOptionalDoubleEqual(problem.optimalValue(), solver.bestValue(), 1e-10);
    }

    /**
     * Test if the mode with the relaxation and the fast upper bound enabled lead to the optimal solution. As side
     * effect, it tests if the fast upper bound on merged states does not cause errors.
     *
     * @param problem The instance to test.
     */
    protected void testFubOnRelaxedNodes(P problem) {
        for (int w = minWidth; w <= maxWidth; w++) {
            SolverConfig<T, K> config = configSolver(problem);
            config.dominance = new DefaultDominanceChecker<>();
            config.debugLevel = 1;
            config.width = new FixedWidth<>(w);
            Solver solver = new ExactSolver<>(config);

            solver.maximize();
            assertOptionalDoubleEqual(problem.optimalValue(), solver.bestValue(), 1e-10, w);
        }
    }

    /**
     * Test if the model with the dominance enabled lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testDominance(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        config.fub = new DefaultFastUpperBound<>();
        Solver solver = new ExactSolver<>(config);

        solver.maximize();
        assertOptionalDoubleEqual(problem.optimalValue(), solver.bestValue(), 1e-10);
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
        if (testFUB) {
            Stream<DynamicTest> fubTests = problems.stream().map(p ->
                    DynamicTest.dynamicTest(String.format("FUB for %s", p.toString()), () -> testFub(p))
            );
            allTests = Stream.concat(allTests, fubTests);
        }

        if (testRelaxation && testFUB) {
            Stream<DynamicTest> relaxAndFubTest = problems.stream().map(p ->
                    DynamicTest.dynamicTest(String.format("Relax and FUB for %s", p.toString()), () -> testFubOnRelaxedNodes(p))
            );
            allTests = Stream.concat(allTests, relaxAndFubTest);
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

        Stream<DynamicTest> bestFirstTests = problems.stream().map(p ->
                DynamicTest.dynamicTest(String.format("BestFirstSearch for %s", p.toString()),
                        () -> testBestFirstSearch(p))
        );

        allTests = Stream.concat(allTests, bestFirstTests);
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
