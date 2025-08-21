package org.ddolib.examples.ddo.pigmentscheduling;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.lang.model.type.NullType;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PSTest {

    // quite easy instances
    static Stream<PSInstance> instance2items() {
        Stream<Integer> testStream = IntStream.rangeClosed(1, 10).boxed();
        return testStream.flatMap(i -> {
            return Stream.of(new PSInstance("src/test/resources/PSP/2items/" + i));
        });
    }

    // harder instances
    static Stream<PSInstance> instance5items() {
        Stream<Integer> testStream = IntStream.rangeClosed(1, 10).boxed();
        return testStream.flatMap(i -> {
            return Stream.of(new PSInstance("src/test/resources/PSP/5items/" + i));
        });
    }

    @ParameterizedTest
    @MethodSource({"instance2items"/*,"instance5items"*/})
    public void testParameterized(PSInstance instance) {
        SolverConfig<PSState, NullType> config = new SolverConfig<>();
        config.problem = new PSProblem(instance);
        config.relax = new PSRelax(instance);
        config.ranking = new PSRanking();
        config.fub = new PSFastUpperBound(instance);
        config.width = new FixedWidth<>(10);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        final Solver solver = new SequentialSolver<>(config);
        solver.maximize();
        assertEquals(solver.bestValue().get(), -instance.optimal); // put a minus since it is a minimization problem
    }
}