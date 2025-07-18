package org.ddolib.examples.ddo.smic;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ddolib.examples.ddo.smic.SMICMain.readProblem;
import static org.ddolib.factory.Solvers.sequentialSolver;
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
        final SMICRelax relax = new SMICRelax(problem);
        final SMICRanking ranking = new SMICRanking();
        final FixedWidth<SMICState> width = new FixedWidth<>(w);
        final VariableHeuristic<SMICState> varh = new DefaultVariableHeuristic<SMICState>();
        final SimpleDominanceChecker<SMICState, Integer> dominance = new SimpleDominanceChecker<>(
                new SMICDominance(), problem.nbVars());
        final Frontier<SMICState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                dominance
        );
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
