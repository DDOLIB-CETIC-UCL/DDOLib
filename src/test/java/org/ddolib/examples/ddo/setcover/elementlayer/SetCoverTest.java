package org.ddolib.examples.ddo.setcover.elementlayer;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.*;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.heuristics.cluster.Kmeans;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.RelaxationSolver;
import org.ddolib.ddo.core.solver.RestrictionSolver;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.examples.ddo.setcover.elementlayer.SetCoverHeuristics.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

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

        final SolverConfig<SetCoverState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SetCoverRelax();
        config.varh = new MinCentralityDynamic(problem);
        config.ranking = new SetCoverRanking();
        config.width = new FixedWidth<>(Integer.MAX_VALUE);
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.Frontier);
        final Solver solver = new SequentialSolver<>(config);

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
        // System.out.printf("Objective: %d%n", solver.bestValue().get());
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
    public void testSmallRestriction() {
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
        final SolverConfig<SetCoverState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SetCoverRelax();
        config.varh = new MinCentralityDynamic(problem);
        config.ranking = new SetCoverRanking();
        config.width = new FixedWidth<>(2);
        config.dominance = new DefaultDominanceChecker<>();

        config.restrictStrategy  = new CostBased<>(config.ranking);
        config.varh = new MinCentralityDynamic(problem);
        final Solver solver = new RestrictionSolver<>(config);

        solver.maximize();
        Assertions.assertTrue(solver.bestValue().isPresent());
        Assertions.assertTrue(solver.bestValue().get() <= -2);

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
        final SolverConfig<SetCoverState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SetCoverRelax();
        config.varh = new MinCentralityDynamic(problem);
        config.ranking = new SetCoverRanking();
        config.width = new FixedWidth<>(2);
        config.distance = new SetCoverDistance();
        config.dominance = new DefaultDominanceChecker<>();

        config.relaxStrategy  = new CostBased<>(config.ranking);
        config.varh = new MinCentralityDynamic(problem);
        final Solver solver = new RelaxationSolver<>(config);
        solver.maximize();
        Assertions.assertTrue(solver.bestValue().isPresent());
        Assertions.assertTrue(-solver.bestValue().get() <= 2);

    }

    /**
     * Test on small random instances to verify that the solution returned by the sequential solver is really
     * optimal.
     * The returned solution is compared with the optimal cost computed by a brute-force approach
     * @param problem
     */
    @ParameterizedTest
    @MethodSource("smallGeneratedInstances")
    @Disabled
    public void testClusterRelaxation(SetCoverProblem problem) {
        int optimalCost = bruteForce(problem);

        final SolverConfig<SetCoverState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SetCoverRelax();
        config.ranking = new SetCoverRanking();
        config.width = new FixedWidth<>(2);
        config.distance = new SetCoverDistance();
        config.dominance = new DefaultDominanceChecker<>();
        List<ReductionStrategy<SetCoverState>> strategies = new ArrayList<>();
        strategies.add(new GHP<>(new SetCoverDistance()));
        strategies.add(new CostBased<>(config.ranking));

        for (ReductionStrategy<SetCoverState> relaxStrategy: strategies) {
            config.relaxStrategy  = relaxStrategy;
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.Frontier);
            config.varh = new MinCentralityDynamic(problem);
            final Solver solver = new SequentialSolver<>(config);

            solver.maximize();

            // Retrieve solution
            Set<Integer> solution = solver.bestSolution().map(decisions -> {
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
    }

    @ParameterizedTest
    @MethodSource("smallGeneratedInstances")
    @Disabled
    public void testClusterRestriction(SetCoverProblem problem) {
        int optimalCost = bruteForce(problem);

        final SolverConfig<SetCoverState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SetCoverRelax();
        config.ranking = new SetCoverRanking();
        config.width = new FixedWidth<>(2);
        config.distance = new SetCoverDistance();
        config.dominance = new DefaultDominanceChecker<>();
        List<ReductionStrategy<SetCoverState>> strategies = new ArrayList<>();
        strategies.add(new GHP<>(new SetCoverDistance()));
        strategies.add(new CostBased<>(config.ranking));

        for (ReductionStrategy<SetCoverState> restrictStrategy: strategies) {
            config.restrictStrategy  = restrictStrategy;
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.Frontier);
            config.varh = new MinCentralityDynamic(problem);
            final Solver solver = new SequentialSolver<>(config);

            solver.maximize();

            // Retrieve solution
            Set<Integer> solution = solver.bestSolution().map(decisions -> {
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
    }

    // *************************************************************************

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
