package org.ddolib.nolayer.solving.startheuristic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.examples.nolayer.knapsack.KSModel;
import org.ddolib.examples.nolayer.knapsack.KSProblem;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.nolayer.modeling.Solvers;
import org.ddolib.nolayer.solver.Solution;
import org.junit.jupiter.api.Test;

/** Tests of the no-layer depth-first start heuristic. */
class DFSTest {

    private static KSProblem ksProblem() {
        return new KSProblem(
                new int[] {10, 7, 8, 4, 6, 3, 9, 5, 2, 6},
                new int[] {5, 4, 6, 3, 5, 2, 7, 4, 1, 4},
                20);
    }

    @Test
    void testFindsFeasibleSolution() throws InvalidSolutionException {
        KSProblem problem = ksProblem();
        Solution sol = new DFS<>(new KSModel(problem)).findSolution();
        double optimum = Solvers.minimizeAstar(new KSModel(problem)).value();

        assertEquals(SearchStatus.SAT, sol.statistics().status());
        assertEquals(problem.evaluate(sol.solution()), sol.value(), 1e-10);
        assertTrue(sol.value() >= optimum - 1e-10);
    }

    @Test
    void testUnsatWhenNoSolution() {
        Model<Integer> model = () -> new NoTargetProblem(3);
        Solution sol = new DFS<>(model).findSolution();

        assertTrue(sol.isEmpty());
        assertEquals(SearchStatus.UNSAT, sol.statistics().status());
    }

    @Test
    void testUnknownWhenLimitReached() {
        Solution sol = new DFS<>(new KSModel(ksProblem())).findSolution(s -> true);

        assertTrue(sol.isEmpty());
        assertEquals(SearchStatus.UNKNOWN, sol.statistics().status());
    }

    @Test
    void testDeadEndsAreNotReexplored() {
        // Without remembering the explored states, the 2^depth branches would all be explored.
        // The limit makes the test fail fast (status UNKNOWN) instead of hanging in that case.
        int depth = 30;
        Model<Integer> model = () -> new NoTargetProblem(depth);
        Solution sol = new DFS<>(model).findSolution(s -> s.nbIterations() > 10 * depth);

        assertTrue(sol.isEmpty());
        assertEquals(SearchStatus.UNSAT, sol.statistics().status());
    }

    @Test
    void testTerminatesOnCycles() {
        // Without remembering the explored states, the DFS would loop forever on the cycle. The
        // limit makes the test fail fast (status UNKNOWN) instead of hanging in that case.
        Model<Integer> model = () -> new CycleProblem(5);
        Solution sol = new DFS<>(model).findSolution(s -> s.nbIterations() > 100);

        assertTrue(sol.isEmpty());
        assertEquals(SearchStatus.UNSAT, sol.statistics().status());
    }

    /**
     * Problem whose state is the number of decisions taken. Both labels lead to the same state, and
     * no target is ever reached: the state {@code maxDepth} has no successor.
     */
    private record NoTargetProblem(int maxDepth) implements Problem<Integer> {

        @Override
        public Integer initialState() {
            return 0;
        }

        @Override
        public double initialValue() {
            return 0;
        }

        @Override
        public boolean isTarget(Integer state) {
            return false;
        }

        @Override
        public Iterator<Integer> domain(Integer state) {
            if (state >= maxDepth) {
                return Collections.emptyIterator();
            }
            return List.of(0, 1).iterator();
        }

        @Override
        public Integer transition(Integer state, int label) {
            return state + 1;
        }

        @Override
        public double transitionCost(Integer state, int label) {
            return label;
        }

        @Override
        public double evaluate(List<Integer> solution) throws InvalidSolutionException {
            throw new InvalidSolutionException("This problem has no solution");
        }
    }

    /** Problem whose states form a cycle {@code 0 -> 1 -> ... -> n-1 -> 0} without any target. */
    private record CycleProblem(int n) implements Problem<Integer> {

        @Override
        public Integer initialState() {
            return 0;
        }

        @Override
        public double initialValue() {
            return 0;
        }

        @Override
        public boolean isTarget(Integer state) {
            return false;
        }

        @Override
        public Iterator<Integer> domain(Integer state) {
            return List.of(0).iterator();
        }

        @Override
        public Integer transition(Integer state, int label) {
            return (state + 1) % n;
        }

        @Override
        public double transitionCost(Integer state, int label) {
            return 1;
        }

        @Override
        public double evaluate(List<Integer> solution) throws InvalidSolutionException {
            throw new InvalidSolutionException("This problem has no solution");
        }
    }
}
