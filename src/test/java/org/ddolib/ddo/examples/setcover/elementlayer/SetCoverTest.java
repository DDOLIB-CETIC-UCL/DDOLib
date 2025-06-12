package org.ddolib.ddo.examples.setcover.elementlayer;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.examples.setcover.elementlayer.SetCoverHeuristics.*;
import org.ddolib.ddo.examples.setcover.elementlayer.SetCoverProblem;
import static org.ddolib.ddo.examples.setcover.elementlayer.SetCover.readInstance;
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

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import static java.lang.Math.max;

public class SetCoverTest {

    @Test
    public void testSmallStatic() {
        int nElem = 6;
        int nSets = 6;
        List<Set<Integer>> constraints = new ArrayList<>();
        constraints.add(Set.of(0,2,4));
        constraints.add(Set.of(0,2,3,4));
        constraints.add(Set.of(0));
        constraints.add(Set.of(1,3,4));
        constraints.add(Set.of(1,4));
        constraints.add(Set.of(1,5));

        final SetCoverProblem problem = new SetCoverProblem(nElem, nSets, constraints);
        final int optimalCost = bruteForce(problem);

        final SetCoverRelax relax = new SetCoverRelax();
        final VariableHeuristic<SetCoverState> varh = new MinCentrality(problem);
        final SetCoverRanking ranking = new SetCoverRanking();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(Integer.MAX_VALUE);
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

        Set<Integer> solution = solver.bestSolution().map(decisions -> {
            System.out.println("Solution Found");
            Set<Integer> values = new HashSet<>();
            for (Decision d : decisions) {
                if (d.val() != -1) {
                    values.add(d.val());
                }
            }
            return values;
        }).get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", solution);

        Assertions.assertTrue(testValidity(problem, solution));
        Assertions.assertEquals(optimalCost, solution.size());
    }

    @Test
    public void testDistance() {
        final SetCoverState a = new SetCoverState(Set.of(0, 1));
        final SetCoverState b = new SetCoverState(Set.of(2, 3));
        final SetCoverState c = new SetCoverState(Set.of(0));
        final SetCoverState d = new SetCoverState(Set.of(0));

        final SetCoverDistance dist = new SetCoverDistance();
        Assertions.assertEquals(dist.distance(a,b), 4);
        Assertions.assertEquals(dist.distance(a,c), 1);
        Assertions.assertEquals(dist.distance(b,c), 3);
        Assertions.assertEquals(dist.distance(b,a), 4);
        Assertions.assertEquals(dist.distance(c,a), 1);
        Assertions.assertEquals(dist.distance(c,b), 3);
        Assertions.assertEquals(dist.distance(d,c), 0);
        Assertions.assertEquals(dist.distance(c,d), 0);
    }

    @Test
    public void testDistanceRnd() {
        Random rnd = new Random(684654654);
        SetCoverDistance dist = new SetCoverDistance();
        for (int test = 0; test < 100; test++) {
            Set<Integer> a = new HashSet<>(50);
            while (a.size() < 50) {
                a.add(rnd.nextInt());
            }

            Set<Integer> b = new HashSet<>(a);
            int nbrRemoved = rnd.nextInt(5, 10);
            List<Integer> tmp = new ArrayList<>(b);
            Collections.shuffle(tmp, rnd);
            tmp.subList(0, nbrRemoved).forEach(b::remove);
            int nbrAdded = rnd.nextInt(5, 10);
            while (b.size() < a.size() - nbrRemoved + nbrAdded) {
                b.add(rnd.nextInt());
            }

            int distRef = nbrRemoved + nbrAdded;
            Assertions.assertEquals(distRef, dist.distance(new SetCoverState(a), new SetCoverState(b)));
        }
    }

    @Test
    public void testSmallRelaxation() {
        int nElem = 6;
        int nSets = 6;
        List<Set<Integer>> constraints = new ArrayList<>();
        constraints.add(Set.of(0,2,4));
        constraints.add(Set.of(0,2,3,4));
        constraints.add(Set.of(0));
        constraints.add(Set.of(1,3,4));
        constraints.add(Set.of(1,4));
        constraints.add(Set.of(1,5));

        final SetCoverProblem problem = new SetCoverProblem(nElem, nSets, constraints);
        final SetCoverRelax relax = new SetCoverRelax();
        final VariableHeuristic<SetCoverState> varh = new MinCentrality(problem);
        final SetCoverRanking ranking = new SetCoverRanking();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(2);
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
    public void testClusterRelaxation(SetCoverProblem problem) {
        int optimalCost = bruteForce(problem);

        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(2);
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>(
                RelaxationType.Cluster,
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        solver.maximize();

        // Retrieve solution
        Set<Integer> solution = solver.bestSolution().map(decisions -> {
            System.out.println("Solution Found");
            Set<Integer> values = new HashSet<>();
            for (Decision d : decisions) {
                if (d.val() != -1) {
                    values.add(d.val());
                }
            }
            return values;
        }).get();

        Assertions.assertTrue(solver.bestValue().isPresent());
        Assertions.assertEquals(optimalCost, -solver.bestValue().get());
        Assertions.assertTrue(testValidity(problem, solution));
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
            System.out.println("Solution Found");
            Set<Integer> values = new HashSet<>();
            for (Decision d : decisions) {
                if (d.val() != -1) {
                    values.add(d.val());
                }
            }
            return values;
        }).get();

        Assertions.assertTrue(solver.bestValue().isPresent());
        Assertions.assertEquals(optimalCost, -solver.bestValue().get());
        Assertions.assertTrue(testValidity(problem, solution));
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
        for (int widthVal = 1; widthVal < 10000; widthVal = widthVal + Math.max(1, (int) (widthVal*0.1))) {
            System.out.println("@@@@@@@@@");
            width = new FixedWidth<>(widthVal);
            varh = new MinCentrality(problem);

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
        // heuristics.put("elementDefault", new DefaultVariableHeuristic<>());
        heuristics.put("elementMinCentrality", new MinCentrality(problem));

        for (String heuristic : heuristics.keySet()) {
            varh = heuristics.get(heuristic);
            System.out.println(heuristic);
            for (int maxWidth = 1; maxWidth < 149; maxWidth = maxWidth + Math.max(1, (int) (maxWidth*0.1))) {
                System.out.print(maxWidth + ", ");
                relax = new SetCoverRelax();
                width = new FixedWidth<>(maxWidth);
                Solver solver = new RelaxationSolver<>(
                        RelaxationType.Cluster,
                        problem,
                        relax,
                        varh,
                        ranking,
                        null,
                        width,
                        frontier);

                long start = System.currentTimeMillis();
                SearchStatistics stats = solver.maximize();
                double duration = (System.currentTimeMillis() - start) / 1000.0;
                System.out.println(duration);

                csvString.append(file).append(";");
                csvString.append(maxWidth).append(";");
                csvString.append(heuristic).append(";");
                csvString.append(duration).append(";");
                csvString.append(solver.bestValue().get()).append("\n");
            }
        }

        FileWriter writer = new FileWriter("tmp/setCoverElementClusterStats.csv", true);
        writer.write(csvString.toString());
        writer.close();

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

    /**
     * Test the validity of a solution, i.e. if the collection of selected sets covers all elements in the universe
     * @param problem the considered instance
     * @param solution the tested solution, a set containing the indexes of the selected sets
     * @return true iff the solution is valid
     */
    private boolean testValidity(SetCoverProblem problem, Set<Integer> solution) {
        Set<Integer> uncoveredElements = problem.initialState().uncoveredElements;
        for (int i: solution) {
            uncoveredElements.removeAll(problem.getSetDefinition(i));
        }
        return uncoveredElements.isEmpty();
    }

    /**
     * Generates small Set Cover problem instances
     * @return a stream of SetCoverProblem
     */
    private static Stream<SetCoverProblem> smallGeneratedInstances() {
        Random rnd = new Random(684654654);
        int nInstances = 20; // number of instances to generate
        int nElem = 10; // number of elements in the instances
        int nSet = 20; // number of sets
        int constraintSize = 2; // the number of sets that must cover each element

        List<SetCoverProblem> instances = new ArrayList<>();
        for (int instance = 0; instance < nInstances; instance++) {
            // Generate for each element the sets that will contain it
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

            instances.add(new SetCoverProblem(nElem, nSet, constraints));

        }
        return instances.stream();
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

}
