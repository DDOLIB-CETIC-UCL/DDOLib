package org.ddolib.examples.ddo.smic;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ddolib.examples.ddo.smic.SMICMain.readProblem;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SMICTest {
    // Easy instances
    static Stream<SMICProblem> easySMICInstances() {
        Stream<Integer> testStream = IntStream.rangeClosed(1, 10).boxed();
        return testStream.flatMap(i -> {
            try {
                return Stream.of(readProblem("src/test/resources/SMIC/data10_" + i + ".txt"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    final int[] cpSolution = new int[]{60, 55, 58, 41, 69, 55, 56, 72, 48, 73};

    private int unitaryTestSMIC(int w, SMICProblem problem) {
        SolverConfig<SMICState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SMICRelax(problem);
        config.ranking = new SMICRanking();
        config.width = new FixedWidth<>(w);
        config.varh = new DefaultVariableHeuristic<>();
        config.dominance =
                new SimpleDominanceChecker<>(new SMICDominance(),
                        problem.nbVars());
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        final Solver solver = new SequentialSolver<>(config);
        solver.maximize();
        return (int) -solver.bestValue().get();
    }

    @ParameterizedTest
    @MethodSource("easySMICInstances")
    public void testSMIC(SMICProblem problem) {
        for (int w = 2; w <= 3; w++) {
            int i = Integer.parseInt(problem.name.split("_")[1].split(".t")[0]) - 1;
            assertEquals(unitaryTestSMIC(w, problem), cpSolution[i]);
        }
    }
}
