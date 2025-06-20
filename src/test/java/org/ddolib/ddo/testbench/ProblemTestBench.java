package org.ddolib.ddo.testbench;

import org.ddolib.ddo.core.Problem;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.junit.jupiter.api.DynamicTest;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;
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

    public ProblemTestBench() {
        problems = generateProblems();
    }

    /**
     * Test if the exact mdd generated for the input problem lead to optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testTransitionModel(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        Solver solver = sequentialSolver(problem, config.relax(), config.varh(), config.ranking(), config.width(), config.frontier());
        solver.maximize();
        assertEquals(problem.optimalValue().get(), solver.bestValue().get(), 1e-10);
    }

    /**
     * Test if the fast upper bound is an upper bound for the root node and if the compilation with the fast upper
     * bound enabled lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testFub(P problem) {
        SolverConfig<T, K> config = configSolver(problem);
        Solver solver = sequentialSolver(problem, config.relax(), config.varh(), config.ranking(), config.width(), config.frontier());

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = config.relax().fastUpperBound(problem.initialState(), vars);
        assertTrue(rub >= problem.optimalValue().get(),
                String.format("Upper bound %.2f is not bigger than the expected optimal solution %.2f",
                        rub,
                        problem.optimalValue().get()));

        solver.maximize();
        assertEquals(problem.optimalValue().get(), solver.bestValue().get(), 1e-10);
    }

    /**
     * Test if the model with relaxation enabled lead to the optimal solution.
     *
     * @param problem The instance to test.
     */
    protected void testRelaxation(P problem) {
        SolverConfig<T, K> config = configSolver(problem);

        if (config.relax() != null) {
            FixedWidth<T> width = new FixedWidth<>(2);
            Solver solver = sequentialSolver(problem, config.relax(), config.varh(), config.ranking(), width,
                    config.frontier());

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
        Solver solver = sequentialSolver(problem, config.relax(), config.varh(), config.ranking(), config.width(),
                config.frontier(), config.dominance());

        solver.maximize();
        assertEquals(problem.optimalValue().get(), solver.bestValue().get(), 1e-10);
    }

    /**
     * Generates all the tests for all the generated instances.
     *
     * @return A stream of tests over all the generated instances.
     */
    public Stream<DynamicTest> generateTests() {
        Stream<DynamicTest> modelTests = problems.stream().map(p ->
                DynamicTest.dynamicTest(String.format("Model for %s", p.toString()), () -> testTransitionModel(p))
        );

        Stream<DynamicTest> relaxTests = problems.stream().map(p ->
                DynamicTest.dynamicTest(String.format("Relaxation for %s", p.toString()), () -> testRelaxation(p))
        );

        Stream<DynamicTest> fubTests = problems.stream().map(p ->
                DynamicTest.dynamicTest(String.format("FUB for %s", p.toString()), () -> testFub(p))
        );

        Stream<DynamicTest> dominanceTests = problems.stream().map(p ->
                DynamicTest.dynamicTest(String.format("Dominance for %s", p.toString()), () -> testDominance(p))
        );

        return Stream.of(modelTests, relaxTests, fubTests, dominanceTests).flatMap(x -> x);
    }
}
