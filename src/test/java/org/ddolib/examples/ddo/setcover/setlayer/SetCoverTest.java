package org.ddolib.examples.ddo.setcover.setlayer;

import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.*;

import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.examples.ddo.setcover.setlayer.SetCoverProblem;
import org.ddolib.examples.ddo.setcover.setlayer.SetCoverRanking;
import org.ddolib.examples.ddo.setcover.setlayer.SetCoverRelax;
import org.ddolib.examples.ddo.setcover.setlayer.SetCoverState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.ddolib.factory.Solvers.relaxationSolver;
import static org.ddolib.factory.Solvers.sequentialSolver;

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
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = relaxationSolver(
                problem,
                relax,
                varh,
                ranking,
                width);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;
        System.out.printf("Duration : %.3f seconds%n", duration);
        // System.out.printf("Objective: %d%n", solver.bestValue().get());
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
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
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

    // *************************************************************************

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
