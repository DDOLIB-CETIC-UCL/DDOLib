package org.ddolib.examples.ddo.talentscheduling;

import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.ddolib.factory.Solvers.sequentialSolver;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TalenSchedTest {

    static Stream<TSProblem> dataProvider() {
        String dir = Paths.get("src", "test", "resources", "TalentScheduling").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        TSProblem problem = TSMain.readFile(filePath.toString());
                        problem.setName(filePath.getFileName().toString());
                        return problem;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTalentScheduling(TSProblem problem) {
        final TSRelax relax = new TSRelax(problem);
        final TSRanking ranking = new TSRanking();

        final WidthHeuristic<TSState> width = new FixedWidth<>(1000);
        final VariableHeuristic<TSState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );

        solver.maximize();

        assertEquals(problem.optimal.get(), solver.bestValue().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastUpperBound(TSProblem problem) {
        final TSFastUpperBound fub = new TSFastUpperBound(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = fub.fastUpperBound(problem.initialState(), vars);
        assertTrue(rub >= problem.optimal.get(),
                String.format("Upper bound %.1f is not bigger than the expected optimal solution %.1f",
                        rub,
                        problem.optimal.get()));
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTalentSchedulingWithRelaxation(TSProblem problem) {
        final TSRelax relax = new TSRelax(problem);
        final TSRanking ranking = new TSRanking();

        final WidthHeuristic<TSState> width = new FixedWidth<>(2);
        final VariableHeuristic<TSState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );

        solver.maximize();

        assertEquals(problem.optimal.get().doubleValue(), solver.bestValue().get());
    }

}
