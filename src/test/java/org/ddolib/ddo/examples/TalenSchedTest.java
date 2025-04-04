package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.examples.talentscheduling.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.heuristics.WidthHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TalenSchedTest {

    static Stream<TalentSchedulingProblem> dataProvider() throws IOException {
        String dir = "src/test/resources/TalentScheduling/";

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> dir + fileName)
                .map(fileName -> {
                    try {
                        return TalentScheduling.readFile(fileName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTalentScheduling(TalentSchedulingProblem problem) {
        final TalentSchedRelax relax = new TalentSchedRelax(problem);
        final TalentSchedRanking ranking = new TalentSchedRanking();

        final WidthHeuristic<TalentSchedState> width = new FixedWidth<>(1000);
        final VariableHeuristic<TalentSchedState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TalentSchedState> frontier = new SimpleFrontier<>(ranking);

        SequentialSolver<TalentSchedState> solver = new SequentialSolver<>(
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
    public void testFastUpperBound(TalentSchedulingProblem problem) {
        final TalentSchedRelax relax = new TalentSchedRelax(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        int rub = relax.fastUpperBound(problem.initialState(), vars);
        assertTrue(rub >= problem.optimal.get(),
                String.format("Upper bound %d is not bigger than the expected optimal solution %d",
                        rub,
                        problem.optimal.get()));
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTalentSchedulingWithRelaxation(TalentSchedulingProblem problem) {
        final TalentSchedRelax relax = new TalentSchedRelax(problem);
        final TalentSchedRanking ranking = new TalentSchedRanking();

        final WidthHeuristic<TalentSchedState> width = new FixedWidth<>(2);
        final VariableHeuristic<TalentSchedState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TalentSchedState> frontier = new SimpleFrontier<>(ranking);

        SequentialSolver<TalentSchedState> solver = new SequentialSolver<>(
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

}
