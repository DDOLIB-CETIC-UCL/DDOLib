package org.ddolib.ddo.examples.setcover.setlayer;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.SearchStatistics;
import org.ddolib.ddo.core.Solver;
import static org.ddolib.ddo.examples.setcover.setlayer.SetCover.*;

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

import org.ddolib.ddo.examples.setcover.setlayer.SetCoverHeuristics.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class SetCoverTest {

    @Test
    public void testSmallRelaxation() {
        int nElem = 6;
        int nSets = 6;
        List<Set<Integer>> sets = new ArrayList<>();
        sets.add(Set.of(0,1,2));
        sets.add(Set.of(3,4,5));
        sets.add(Set.of(0,1));
        sets.add(Set.of(1,3));
        sets.add(Set.of(0,1,3,4));
        sets.add(Set.of(5));

        /*sets.add(Set.of(5));
        sets.add(Set.of(3,4,5));
        sets.add(Set.of(1,3));
        sets.add(Set.of(0,1,3,4));
        sets.add(Set.of(0,1));
        sets.add(Set.of(0,1,2));*/

        final SetCoverProblem problem = new SetCoverProblem(nElem, nSets, sets);
        final SetCoverRelax relax = new SetCoverRelax();
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final SetCoverRanking ranking = new SetCoverRanking();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(1);
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
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        Assertions.assertTrue(solver.bestValue().get() <= 1 );
    }

    /**
     * Test on small random instances to verify that the solution returned by the sequential solver is really
     * optimal.
     * The returned solution is compared with the optimal cost computed by a brute-force approach
     * @param problem
     */
    @ParameterizedTest
    @MethodSource("smallGeneratedInstances")
    public void testCompleteness(SetCoverProblem problem) {
        int optimalCost = bruteForce(problem);
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

        solver.maximize();

        // Retrieve solution
        Set<Integer> solution = solver.bestSolution().map(decisions -> {
            Set<Integer> sol = new HashSet<>();
            for (Decision d : decisions) {
                if (d.val() == 1)
                    sol.add(d.var());
            }
            // System.out.println("Cost: "+ Arrays.stream(values).sum());
            return sol;
        }).get();

        Assertions.assertTrue(solver.bestValue().isPresent());
        Assertions.assertEquals(optimalCost, -solver.bestValue().get());
        Assertions.assertTrue(testValidity(problem, solution));
    }


    @Test
    public void testReducedProblem() {
        List<Set<Integer>> sets = new ArrayList<>();
        sets.add(Set.of(0,1));
        sets.add(Set.of(0,2));
        sets.add(Set.of(0,3));
        sets.add(Set.of(0,1));

        int nbrElemRemoved = 1;
        SetCoverProblem problem = new SetCoverProblem(4, sets.size(), sets, nbrElemRemoved);
        SetCoverState initState = problem.initialState();
        Assertions.assertEquals(nbrElemRemoved, problem.nbrElemRemoved);
        Assertions.assertEquals(initState.uncoveredElements.size(), problem.nElem - nbrElemRemoved);
        Assertions.assertFalse(initState.uncoveredElements.containsKey(0));
        nbrElemRemoved = 2;
        problem = new SetCoverProblem(4, sets.size(), sets, nbrElemRemoved);
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
                uncoveredElements.removeAll(problem.sets.get(i));
            }
        }
        Assertions.assertEquals(0, uncoveredElements.size());
    }

    // The following tests are here for measurement and can be quite long to run
    // they should not be used as unit test

    @Disabled
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
    @MethodSource("dataProvider")
    public void testRelaxationStrength(String file) throws IOException {
        SetCoverProblem problem = readInstance(file);
        final SetCoverRanking ranking = new SetCoverRanking();
        SetCoverRelax relax;
        FixedWidth<SetCoverState> width;
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        VariableHeuristic<SetCoverState> varh;

        StringBuilder csvString = new StringBuilder();

        Map<String, VariableHeuristic<SetCoverState>> heuristics = new HashMap<>();
        heuristics.put("default", new DefaultVariableHeuristic<>());
        heuristics.put("minCentrality", new MinCentralityHeuristic(problem));
        // heuristics.put("focusSmallState", new FocusMostSmallState(problem));
        heuristics.put("focusClosingElement", new FocusClosingElements(problem));

        for (String heuristic : heuristics.keySet()) {
            varh = heuristics.get(heuristic);
            System.out.println(heuristic);
            for (int maxWidth = 1; maxWidth < 10000; maxWidth = maxWidth + Math.max(1, (int) (maxWidth*0.1))) {
                System.out.print(maxWidth + ", ");
                relax = new SetCoverRelax();
                width = new FixedWidth<>(maxWidth);
                Solver solver = new RelaxationSolver<>(problem,
                        relax,
                        varh,
                        ranking,
                        width,
                        frontier);

                long start = System.currentTimeMillis();
                SearchStatistics stats = solver.maximize();
                double duration = (System.currentTimeMillis() - start) / 1000.0;

                csvString.append(file).append(";");
                csvString.append(maxWidth).append(";");
                csvString.append(heuristic).append(";");
                csvString.append(duration).append(";");
                csvString.append(solver.bestValue().get()).append("\n");
            }
        }

        FileWriter writer = new FileWriter("tmp/setCoverSetStats.csv", true);
        writer.write(csvString.toString());
        writer.close();

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
        FixedWidth<SetCoverState> width;
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);

        StringBuilder csvString = new StringBuilder();

        for (int proportionKept = 10; proportionKept <= 100; proportionKept+=10) {
            for (int maxWidth = 1; maxWidth <= 10000; maxWidth = maxWidth*10) {
                width = new FixedWidth<>(maxWidth);
                System.out.println("@@@@@@@@@@");
                System.out.println("ProportionKept = " + proportionKept);
                int nbrElemRemoved = (int) Math.ceil((100.0 - proportionKept) / 100.0 * problem.nElem);
                System.out.println("nbrElemRemoved = " + nbrElemRemoved);
                problem.setNbrElemRemoved(nbrElemRemoved);

                RelaxationSolver<SetCoverState> solver = new RelaxationSolver<>(
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
                csvString.append(maxWidth).append(";");
                csvString.append(nbrElemRemoved).append(";");
                csvString.append(duration).append(";");
                csvString.append(solver.bestValue().get()).append(";");
                csvString.append(stats.nbIterations()).append(";");
                csvString.append(stats.queueMaxSize()).append(";");
                csvString.append(problem.countZeroOnly).append(";");
                csvString.append(problem.countOneOnly).append(";");
                csvString.append(problem.countZeroOne).append("\n");
                // csvString.append(solver.timeForBest).append("\n");
            }
        }

        FileWriter writer = new FileWriter("tmp/setCoverStats.csv", true);
        writer.write(csvString.toString());
        writer.close();

    }

    @Disabled
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRelaxation(String fname) throws IOException {
        System.out.println("******************");
        System.out.println(fname);
        final SetCoverProblem problem = readInstance(fname);
        final SetCoverRelax relax = new SetCoverRelax();
        VariableHeuristic<SetCoverState> varh;
        final SetCoverRanking ranking = new SetCoverRanking();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        FixedWidth<SetCoverState> width;
        for (int widthVal = 1; widthVal < 1000000; widthVal = widthVal * 10) {
            System.out.println("@@@@@@@@@");
            width = new FixedWidth<>(widthVal);
            varh = new DefaultVariableHeuristic<>();

            Solver solver = new RelaxationSolver<>(
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier);

            long start = System.currentTimeMillis();
            solver.maximize();
            double duration = (System.currentTimeMillis() - start) / 1000.0;
            System.out.printf("Max width: %d%n", width.maximumWidth(null));
            System.out.printf("Duration : %.3f seconds%n", duration);
            System.out.printf("Objective: %d%n", solver.bestValue().get());
        }
    }

    // *************************************************************************


    static Stream<String> dataProvider() {
        return Stream.of(
                "data/SetCover/generated/n_6_b_5_d_5",
                "data/SetCover/generated/n_10_b_8_d_3",
                "data/SetCover/1id_problem/abilene",
                "data/SetCover/1id_problem/ai3",
                "data/SetCover/1id_problem/aarnet"
        );
    }

    static Stream<String> dataProviderLight() {
        return Stream.of(
                "data/SetCover/generated/n_6_b_5_d_5",
                "data/SetCover/generated/n_10_b_8_d_3",
                "data/SetCover/1id_problem/abilene",
                "data/SetCover/1id_problem/ai3"
        );
    }


    static Stream<String> dataProviderMedium() {
        return Stream.of(
                "data/SetCover/1id_problem/double_flower",
                "data/SetCover/1id_problem/tripode",
                "data/SetCover/1id_problem/ai3",
                "data/SetCover/1id_problem/abilene",
                "data/SetCover/1id_problem/garr199904",
                "data/SetCover/1id_problem/aarnet",
                "data/SetCover/generated/n_190_b_121_d_3"
        );
    }

    static Stream<String> dataProviderHeavy() {
        return Stream.of(
                "data/SetCover/1id_problem/syringa",
                "data/SetCover/1id_problem/renater_2010",
                "data/SetCover/1id_problem/garr199904",
                "data/SetCover/1id_problem/aarnet",
                "data/SetCover/generated/n_190_b_121_d_3"
        );
    }

    /**
     * Test the validity of a solution, i.e. if the collection of selected sets covers all elements in the universe
     * @param problem the considered instance
     * @param solution the tested solution, a set containing the indexes of the selected sets
     * @return true iff the solution is valid
     */
    private boolean testValidity(SetCoverProblem problem, Set<Integer> solution) {
        Set<Integer> uncoveredElements = problem.initialState().uncoveredElements.keySet();
        for (int i: solution) {
            uncoveredElements.removeAll(problem.sets.get(i));
        }
        return uncoveredElements.isEmpty();
    }

    /**
     * Generate all non-ordered combination of k elements among n
     * @param n the total number of element
     * @param k the size of the combination
     * @return a list containing the different combination, represented by sets
     */
    public static List<Set<Integer>> generateCombinations(int n, int k) {
        List<Set<Integer>> result = new ArrayList<>();
        backtrack(0, n, k, new ArrayList<>(), result);
        return result;
    }

    /**
     * Backtrack method to generation combination
     * @param start
     * @param n
     * @param k
     * @param tempList
     * @param result
     */
    private static void backtrack(int start, int n, int k, List<Integer> tempList, List<Set<Integer>> result) {
        if (tempList.size() == k) {
            result.add(new HashSet<>(tempList));
            return;
        }

        for (int i = start; i < n; i++) {
            tempList.add(i);
            backtrack(i + 1, n, k, tempList, result);
            tempList.removeLast(); // backtrack
        }
    }

    /**
     * Solve to optimality a SetCoverProblem with brute-force
     * @param problem the problem to solve
     * @return the cost of the optimal solution
     */
    private int bruteForce(SetCoverProblem problem) {
        for (int nSetsSelected = 1; nSetsSelected <= problem.nSet; nSetsSelected++) {
            List<Set<Integer>> combinations = generateCombinations(problem.nSet, nSetsSelected);
            for (Set<Integer> combination : combinations) {
                if (testValidity(problem, combination)) {
                    return nSetsSelected;
                }
            }
        }
        return -1;
    }

    /**
     * Generates small Set Cover problem instances
     * @return a stream of SetCoverProblem
     */
    private static Stream<SetCoverProblem> smallGeneratedInstances() {
        Random rnd = new Random(684654654);
        int nInstances = 5; // number of instances to generate
        int nElem = 10; // number of elements in the instances
        int nSet = 20;
        int constraintSize = 2; // the number of sets that must cover each element

        List<SetCoverProblem> instances = new ArrayList<>();

        for (int instance = 0; instance < nInstances; instance++) {
            // First, generate for each element the sets that will contain it
            // By doing so, we are sure that each element can be covered by at least one element
            List<Set<Integer>> constraints = new ArrayList<>();
            int maxSetIndex = -1;

            List<Integer> range = new ArrayList<>();
            for (int i = 0; i < nSet; i++) {
                range.add(i);
            }

            for (int i = 0; i < nElem; i++) {
                Set<Integer> constraint = new HashSet<>();
                Collections.shuffle(range, rnd);
                for (int j = 0; j < Math.min(constraintSize, range.size()); j++) {
                    constraint.add(range.get(j));
                }
                constraints.add(constraint);
            }

            // We compute the sets and removes the empy ones
            List<Set<Integer>> sets = new ArrayList<>();
            for (int setIndex = 0; setIndex <= nSet; setIndex++) {
                Set<Integer> currentSet = new HashSet<>();
                for (int elem = 0; elem < nElem; elem++) {
                    if (constraints.get(elem).contains(setIndex)) {
                        currentSet.add(elem);
                    }
                }
                if (!currentSet.isEmpty()) {
                    sets.add(currentSet);
                }
            }
            instances.add(new SetCoverProblem(nElem, sets.size(), sets));

        }
        return instances.stream();
    }


}
