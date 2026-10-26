package org.ddolib.layered.solving.startheuristic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.examples.layered.knapsack.KSDominance;
import org.ddolib.examples.layered.knapsack.KSFastLowerBound;
import org.ddolib.examples.layered.knapsack.KSProblem;
import org.ddolib.examples.layered.knapsack.KSRanking;
import org.ddolib.examples.layered.knapsack.KSRelax;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;
import org.ddolib.layered.solving.ddo.core.Decision;
import org.junit.jupiter.api.Test;

/** Tests of the depth-first start heuristic. */
class DFSTest {

    private static KSProblem ksProblem() {
        return new KSProblem(
                20,
                new int[] {10, 7, 8, 4, 6, 3, 9, 5, 2, 6},
                new int[] {5, 4, 6, 3, 5, 2, 7, 4, 1, 4});
    }

    private static DdoModel<Integer> ksModel(KSProblem problem) {
        return new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public Relaxation<Integer> relaxation() {
                return new KSRelax();
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
            }
        };
    }

    private static Model<Integer> deadEndModel(int nbVars) {
        DeadEndProblem problem = new DeadEndProblem(nbVars);
        return () -> problem;
    }

    @Test
    void testFindsFeasibleSolution() throws InvalidSolutionException {
        KSProblem problem = ksProblem();
        Solution sol = new DFS<>(ksModel(problem)).findSolution();
        double optimum = Solvers.minimizeExact(ksModel(problem).toExactModel()).value();

        assertEquals(SearchStatus.SAT, sol.statistics().status());
        assertEquals(problem.nbVars(), sol.solution().length);
        assertEquals(problem.evaluate(sol.solution()), sol.value(), 1e-10);
        assertTrue(sol.value() >= optimum - 1e-10);
    }

    @Test
    void testUnsatWhenNoSolution() {
        Solution sol = new DFS<>(deadEndModel(3)).findSolution();

        assertTrue(sol.isEmpty());
        assertEquals(SearchStatus.UNSAT, sol.statistics().status());
    }

    @Test
    void testUnknownWhenLimitReached() {
        Solution sol = new DFS<>(ksModel(ksProblem())).findSolution(s -> true);

        assertTrue(sol.isEmpty());
        assertEquals(SearchStatus.UNKNOWN, sol.statistics().status());
    }

    @Test
    void testDeadEndsAreNotReexplored() {
        // Without remembering the explored states, the 2^nbVars branches would all be explored.
        // The limit makes the test fail fast (status UNKNOWN) instead of hanging in that case.
        int nbVars = 30;
        Solution sol =
                new DFS<>(deadEndModel(nbVars))
                        .findSolution(s -> s.nbIterations() > 10 * nbVars);

        assertTrue(sol.isEmpty());
        assertEquals(SearchStatus.UNSAT, sol.statistics().status());
    }

    /**
     * Problem with a single state and two decisions per variable, except the last variable whose
     * domain is empty: it has no solution.
     */
    private record DeadEndProblem(int nbVars) implements Problem<Integer> {

        @Override
        public Integer initialState() {
            return 0;
        }

        @Override
        public double initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(Integer state, int var) {
            if (var == nbVars - 1) {
                return Collections.emptyIterator();
            }
            return List.of(0, 1).iterator();
        }

        @Override
        public Integer transition(Integer state, Decision decision) {
            return state;
        }

        @Override
        public double transitionCost(Integer state, Decision decision) {
            return decision.value();
        }

        @Override
        public double evaluate(int[] solution) throws InvalidSolutionException {
            throw new InvalidSolutionException("This problem has no solution");
        }
    }
}
