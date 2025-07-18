package org.ddolib.util.testbench;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.modeling.DefaultFastUpperBound;
import org.ddolib.modeling.Problem;
import org.junit.jupiter.api.DynamicTest;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.ddolib.factory.Solvers.exactSolver;
import static org.ddolib.factory.Solvers.sequentialSolver;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Abstract class to generate tests on implementations of {@link Problem}. The user need to implement an instance
 * generator and a {@link SolverConfig}.
 *
 * @param <T> The type of states.
 * @param <K> The type of dominance keys.
 * @param <P> The type of problem to test.
 */
public abstract class ProblemTestBench<T, K, P extends Problem<T>> {

    /**
     * List of problem used for tests.
     */
    protected final List<P> problems;

    /**
     * Whether the relaxation must be tested.
     */
    protected final boolean testRelaxation;

    /**
     * Whether the fast upper bound must be tested.
     */
    protected final boolean testFUB;

    /**
     * Whether the dominance must be tested.
     */
    protected final boolean testDominance;

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
     *
     * @param testRelaxation Whether the relaxation must be tested.
     * @param testFUB        Whether the fast upper bound must be tested.
     * @param testDominance  Whether the dominance must be tested.
     */
    public ProblemTestBench(boolean testRelaxation, boolean testFUB, boolean testDominance) {
        this.testRelaxation = testRelaxation;
        this.testFUB = testFUB;
        this.testDominance = testDominance;
        problems = generateProblems();
    }

    /**
     * Test if the exact mdd generated for the input problem lead to optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testTransitionModel(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        Solver solver = exactSolver(problem, config.relax(), config.varh(), config.ranking());
        solver.maximize();
        assertEquals(problem.optimalValue().get(), solver.bestValue().get(), 1e-10);
    }

    /**
     * Test if the fast upper bound is an upper bound for the root node and if the compilation with only the fast upper
     * bound enabled lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testFub(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        Solver solver = exactSolver(
                problem,
                config.relax(),
                config.varh(),
                config.ranking(),
                config.fub(),
                new DefaultDominanceChecker<>());

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = config.fub().fastUpperBound(problem.initialState(), vars);
        DecimalFormat df = new DecimalFormat("#.##########");
        assertTrue(rub >= problem.optimalValue().get(),
                String.format("Upper bound %s is not bigger than the expected optimal solution %s",
                        df.format(rub),
                        df.format(problem.optimalValue().get())));

        solver.maximize();
        assertEquals(problem.optimalValue().get(), solver.bestValue().get(), 1e-10);
    }

    /**
     * Test if the model only with relaxation enabled lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testRelaxation(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        for (int w = 2; w <= 20; w++) {
            FixedWidth<T> width = new FixedWidth<>(w);
            Solver solver = sequentialSolver(
                    problem,
                    config.relax(),
                    config.varh(),
                    config.ranking(),
                    width,
                    config.frontier());

            solver.maximize();
            assertEquals(problem.optimalValue().get(), solver.bestValue().get(), 1e-10);
        }
    }

    /**
     * Test if the mode with the relaxation and the fast upper bound enabled lead to the optimal solution. As side
     * effect, it tests if the fast upper bound on merged states does not cause errors.
     *
     * @param problem The instance to test.
     */
    protected void testFubOnRelaxedNodes(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        for (int w = 2; w <= 20; w++) {
            FixedWidth<T> width = new FixedWidth<>(2);
            Solver solver = sequentialSolver(
                    problem,
                    config.relax(),
                    config.varh(),
                    config.ranking(),
                    width,
                    config.frontier(),
                    config.fub());

            solver.maximize();
            assertEquals(problem.optimalValue().get(), solver.bestValue().get(), 1e-10);
        }
    }

    /**
     * Test if the model with the dominance enabled lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testDominance(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        Solver solver = exactSolver(
                problem,
                config.relax(),
                config.varh(),
                config.ranking(),
                new DefaultFastUpperBound<>(),
                config.dominance());

        solver.maximize();
        assertEquals(problem.optimalValue().get(), solver.bestValue().get(), 1e-10);
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

        return allTests;
    }
}
