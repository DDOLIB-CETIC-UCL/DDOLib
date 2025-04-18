package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.SearchStatistics;
import org.ddolib.ddo.core.Solver;
import static org.ddolib.ddo.examples.SetCover.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.RelaxationSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Assertions.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class SetCoverTest {
    static Stream<String> dataProviderLight() {
        return Stream.of(
                "data/SetCover/1id_problem/double_flower",
                "data/SetCover/1id_problem/tripode"
        );
    }


    static Stream<String> dataProviderMedium() {
        return Stream.of(
                "data/SetCover/1id_problem/double_flower",
                "data/SetCover/1id_problem/tripode",
                "data/SetCover/1id_problem/ai3",
                // "data/SetCover/1id_problem/garr199904",
                "data/SetCover/1id_problem/abilene",
                // "data/SetCover/1id_problem/aarnet",
                "data/SetCover/generated/n_190_b_121_d_3"
            );
    }

    static Stream<String> dataProviderHeavy() {
        return Stream.of(
                "data/SetCover/1id_problem/syringa",
                "data/SetCover/1id_problem/renater_2010"
        );
    }

    @Test
    public void testReducedProblem() {
        Set<Integer>[] sets = new Set[] {
                Set.of(0,1),
                Set.of(0,2),
                Set.of(0,3),
                Set.of(0,1),
        };

        int nbrElemRemoved = 1;
        SetCoverProblem problem = new SetCoverProblem(4, sets.length, sets, nbrElemRemoved);
        SetCoverState initState = problem.initialState();
        Assertions.assertEquals(nbrElemRemoved, problem.nbrElemRemoved);
        Assertions.assertEquals(initState.uncoveredElements.size(), problem.nElem - nbrElemRemoved);
        Assertions.assertFalse(initState.uncoveredElements.containsKey(0));
        nbrElemRemoved = 2;
        problem = new SetCoverProblem(4, sets.length, sets, nbrElemRemoved);
        initState = problem.initialState();
        Assertions.assertFalse(initState.uncoveredElements.containsKey(0));
        Assertions.assertFalse(initState.uncoveredElements.containsKey(1));
        Assertions.assertEquals(initState.uncoveredElements.size(), problem.nElem - nbrElemRemoved);
    }

    private void testTemplate(SetCoverProblem problem, Solver solver) {
        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution().map(decisions -> {
            // System.out.println("Solution Found");
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            // System.out.println("Cost: "+ Arrays.stream(values).sum());
            return values;
        }).get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));

        Set<Integer> uncoveredElements = problem.initialState().uncoveredElements.keySet();

        for (int i = 0; i< solution.length; i++) {
            if (solution[i] == 1) {
                uncoveredElements.removeAll(problem.sets[i]);
            }
        }
        Assertions.assertEquals(0, uncoveredElements.size());
    }

    @ParameterizedTest
    @MethodSource("dataProviderLight")
    public void testSmallInstances(String file) throws IOException {
        SetCoverProblem problem = readInstance(file);
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(1000);
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);
        testTemplate(problem, solver);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("dataProviderMedium")
    public void testMediumInstances(String file) throws IOException {
        SetCoverProblem problem = readInstance(file);
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(1000);
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);
        testTemplate(problem, solver);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("dataProviderHeavy")
    public void testLargeInstances(String file) throws IOException {
        SetCoverProblem problem = readInstance(file);
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(1000);
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);
        testTemplate(problem, solver);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("dataProviderMedium")
    public void testReducedProblemQuality(String file) throws IOException {
        System.out.println("***************");
        System.out.println(file);
        SetCoverProblem problem = readInstance(file);
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(10);
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);

        StringBuilder csvString = new StringBuilder();

        for (int proportionKept = 10; proportionKept <= 100; proportionKept+=10) {
            System.out.println("@@@@@@@@@@");
            System.out.println("ProportionKept = " + proportionKept);
            int nbrElemRemoved = (int) Math.ceil((100.0 - proportionKept)/100.0 * problem.nElem);
            System.out.println("nbrElemRemoved = " + nbrElemRemoved);
            problem.setNbrElemRemoved(nbrElemRemoved);

            SequentialSolver<SetCoverState> solver = new SequentialSolver<>(
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier);

            long start = System.currentTimeMillis();
            SearchStatistics stats = solver.maximize();
            double duration = (System.currentTimeMillis() - start) / 1000.0;

            System.out.printf("Duration : %.3f seconds%n", duration);
            System.out.printf("Objective: %d%n", solver.bestValue().get());
            System.out.println(stats);
            System.out.printf("Number of zero only branching: %d%n", problem.countZeroOnly);
            System.out.printf("Number of one only branching: %d%n", problem.countOneOnly);
            System.out.printf("Number of zero-one branching: %d%n", problem.countZeroOne);

            csvString.append(file).append(";");
            csvString.append(proportionKept).append(";");
            csvString.append(nbrElemRemoved).append(";");
            csvString.append(duration).append(";");
            csvString.append(solver.bestValue().get()).append(";");
            csvString.append(stats.nbIterations()).append(";");
            csvString.append(stats.queueMaxSize()).append(";");
            csvString.append(problem.countZeroOnly).append(";");
            csvString.append(problem.countOneOnly).append(";");
            csvString.append(problem.countZeroOne).append(";");
            csvString.append(solver.timeForBest).append("\n");
        }

        FileWriter writer = new FileWriter("tmp/setCoverStats.csv", true);
        writer.write(csvString.toString());
        writer.close();
    }
}
