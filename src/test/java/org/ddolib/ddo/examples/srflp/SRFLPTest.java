package org.ddolib.ddo.examples.srflp;

import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SRFLPTest {

    static Stream<SRFLPProblem> dataProvider() {
        String dir = Paths.get("src", "test", "resources", "SRFLP").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(file -> Paths.get(dir, file.getName()))
                .map(filePath -> {
                    try {
                        SRFLPProblem problem = SRFLPIO.readInstance(filePath.toString());
                        problem.setName(filePath.getFileName().toString());
                        return problem;

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSRFLP(SRFLPProblem problem) {
        final SRFLPRelax relax = new SRFLPRelax(problem);
        final SRFLPRanking ranking = new SRFLPRanking();

        final FixedWidth<SRFLPState> width = new FixedWidth<>(1000);
        final DefaultVariableHeuristic<SRFLPState> varh = new DefaultVariableHeuristic<>();
        final SimpleFrontier<SRFLPState> frontier = new SimpleFrontier<>(ranking);

        SequentialSolver<SRFLPState> solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );

        solver.maximize();
        assertEquals(problem.optimal.get(), -solver.bestValue().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastLowerBoundAtRoot(SRFLPProblem problem) {
        final SRFLPRelax relax = new SRFLPRelax(problem);
        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        int rub = relax.fastUpperBound(problem.initialState(), vars);
        assertTrue(-rub <= problem.optimal.get(),
                String.format("Lower bound %d is not smaller than the expected optimal solution %d",
                        rub,
                        problem.optimal.get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSRFLPWithRelaxation(SRFLPProblem problem) {
        final SRFLPRelax relax = new SRFLPRelax(problem);
        final SRFLPRanking ranking = new SRFLPRanking();

        final FixedWidth<SRFLPState> width = new FixedWidth<>(2);
        final DefaultVariableHeuristic<SRFLPState> varh = new DefaultVariableHeuristic<>();
        final SimpleFrontier<SRFLPState> frontier = new SimpleFrontier<>(ranking);

        SequentialSolver<SRFLPState> solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );

        solver.maximize();
        assertEquals(problem.optimal.get(), -solver.bestValue().get());
    }


}
