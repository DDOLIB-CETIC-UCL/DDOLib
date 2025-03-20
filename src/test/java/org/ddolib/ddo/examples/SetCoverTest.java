package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.SetCover.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.RelaxationSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

public class SetCoverTest {
    static Stream<SetCover.SetCoverProblem> dataProvider() {
        try {
            return Stream.of(
                    SetCover.readInstance("data/SetCover/1id_problem/double_flower"),
                    SetCover.readInstance("data/SetCover/1id_problem/tripode")
                    // SetCover.readInstance("data/SetCover/1id_problem/aarnet")
                    // SetCover.readInstance("data/SetCover/generated/n_190_b_67_d_35")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testParameterized(SetCoverProblem problem) {
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(Integer.MAX_VALUE);
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;


        int[] solution = solver.bestSolution().map(decisions -> {
            System.out.println("Solution Found");
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            System.out.println("Cost: "+ Arrays.stream(values).sum());
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

    @Test
    public void testSyringa() throws IOException {
        final SetCoverProblem problem = SetCover.readInstance("data/SetCover/1id_problem/syringa");
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();

        final FixedWidth<SetCoverState> width = new FixedWidth<>(100);
        // final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final VariableHeuristic<SetCoverState> varh = new MinBandwithHeuristic(problem);
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new RelaxationSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        System.out.printf("Duration : %.3f seconds%n", duration);

        Set<Integer> uncoveredElements = problem.initialState().uncoveredElements.keySet();

        //Assertions.assertEquals(0, uncoveredElements.size());
    }

    @Test
    public void testRenater() throws IOException {
        final SetCoverProblem problem = SetCover.readInstance("data/SetCover/1id_problem/renater_2010");
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();

        final FixedWidth<SetCoverState> width = new FixedWidth<>(100);
        final VariableHeuristic<SetCoverState> varh = new MinBandwithHeuristic(problem);
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new RelaxationSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        System.out.printf("Duration : %.3f seconds%n", duration);

        Set<Integer> uncoveredElements = problem.initialState().uncoveredElements.keySet();
        // Assertions.assertEquals(0, uncoveredElements.size());
    }

    @Test
    public void testAbilene() throws IOException {
        final SetCoverProblem problem = SetCover.readInstance("data/SetCover/1id_problem/abilene");
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        // 1: 0
        // 10: -1
        // 100: -2
        // 1000: -2
        // 10000: -3
        // 20000: -3
        // 30000: -3
        // 40000: -3
        // 60000: UB: -3 - Time needed: 141,472 seconds
        // 100000:
        final FixedWidth<SetCoverState> width = new FixedWidth<>(100);
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new RelaxationSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        System.out.printf("Duration : %.3f seconds%n", duration);

        Set<Integer> uncoveredElements = problem.initialState().uncoveredElements.keySet();
    }

    @Test
    public void testAarnet() throws IOException {
        final SetCoverProblem problem = SetCover.readInstance("data/SetCover/1id_problem/aarnet");
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        // 1: 0
        // 10: -1
        // 100: -2
        // 1000: -2
        // 10000: -3
        // 20000: -3
        // 30000: -3
        // 40000: UB: -3 - Time needed: 97,536 seconds
        // 60000: UB: -3 - Time needed: 141,472 seconds
        // 100000:
        final FixedWidth<SetCoverState> width = new FixedWidth<>(10000);
        final VariableHeuristic<SetCoverState> varh = new MostCovered(problem);
        // final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new RelaxationSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        System.out.printf("Duration : %.3f seconds%n", duration);

        Set<Integer> uncoveredElements = problem.initialState().uncoveredElements.keySet();
        // Assertions.assertEquals(0, uncoveredElements.size());
    }

    @Test
    public void testGenerated() throws IOException {
        final SetCoverProblem problem = SetCover.readInstance("data/SetCover/generated/n_190_b_67_d_35");
        // final SetCoverProblem problem = SetCover.readInstance("data/SetCover/generated/n_190_b_67_d_35");
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(5);
        final VariableHeuristic<SetCoverState> varh = new MostCovered(problem);
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new RelaxationSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        System.out.printf("Duration : %.3f seconds%n", duration);

        Set<Integer> uncoveredElements = problem.initialState().uncoveredElements.keySet();
    }

    @Test
    public void testTripode() throws IOException {
        final SetCoverProblem problem = SetCover.readInstance("data/SetCover/1id_problem/tripode");
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(5);
        final VariableHeuristic<SetCoverState> varh = new MostCovered(problem);
        // final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new RelaxationSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        System.out.printf("Duration : %.3f seconds%n", duration);

        Set<Integer> uncoveredElements = problem.initialState().uncoveredElements.keySet();

    }

}
