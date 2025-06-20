package org.ddolib.ddo.testbench;

import org.ddolib.ddo.core.Problem;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class ExampleTestBench<T, K> {

    protected final ArrayList<Problem<T>> problems;

    abstract public ArrayList<Problem<T>> generateProblems();

    abstract SolverConfig<T, K> configSolver(Problem<T> problem);

    public ExampleTestBench() {
        problems = generateProblems();
    }

    protected void testTransitionModel(Problem<T> problem) {
        SolverConfig<T, K> config = configSolver(problem);
        Solver solver = sequentialSolver(problem, config.relax(), config.varh(), config.ranking(), config.width(), config.frontier());

        solver.maximize();
        assertEquals(problem.optimalValue().get(), solver.bestValue().get(), 1e-10);
    }

    protected void testFub(Problem<T> problem) {
        SolverConfig<T, K> config = configSolver(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = config.relax().fastUpperBound(problem.initialState(), vars);
        assertTrue(rub >= problem.optimalValue().get(),
                String.format("Upper bound %.2f is not bigger than the expected optimal solution %.2f",
                        rub,
                        problem.optimalValue().get()));
    }

    protected void testRelaxation(Problem<T> problem) {
        SolverConfig<T, K> config = configSolver(problem);

        if (config.relax() != null) {
            FixedWidth<T> width = new FixedWidth<>(2);
            Solver solver = sequentialSolver(problem, config.relax(), config.varh(), config.ranking(), width,
                    config.frontier());

            solver.maximize();
            assertEquals(problem.optimalValue().get(), solver.bestValue().get(), 1e-10);
        }
    }

    protected void testDominance(Problem<T> problem) {
        SolverConfig<T, K> config = configSolver(problem);
        Solver solver = sequentialSolver(problem, config.relax(), config.varh(), config.ranking(), config.width(),
                config.frontier(), config.dominance());

        solver.maximize();
        assertEquals(problem.optimalValue().get(), solver.bestValue().get(), 1e-10);
    }

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
