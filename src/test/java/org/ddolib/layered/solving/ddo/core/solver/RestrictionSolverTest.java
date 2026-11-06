package org.ddolib.layered.solving.ddo.core.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;
import org.ddolib.layered.solving.ddo.core.Decision;
import org.junit.jupiter.api.Test;

/**
 * Tests of {@link RestrictionSolver} when the restricted decision diagram contains no solution:
 * it must return an empty solution instead of stopping the JVM.
 */
public class RestrictionSolverTest {

    /**
     * A problem with three variables. The first one only takes the value 0, since the DDs are not
     * restricted before depth 2. The state is the value of the second variable: the value 1 is
     * cheaper, but only the value 0 can be completed (by 0), unless the problem is infeasible.
     */
    private static final class TrapProblem implements Problem<Integer> {
        private final boolean feasible;

        TrapProblem(boolean feasible) {
            this.feasible = feasible;
        }

        @Override
        public int nbVars() {
            return 3;
        }

        @Override
        public Integer initialState() {
            return -1;
        }

        @Override
        public double initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(Integer state, int var) {
            if (var == 0) {
                return List.of(0).iterator();
            }
            if (var == 1) {
                return List.of(0, 1).iterator();
            }
            return feasible && state == 0
                    ? List.of(0).iterator()
                    : Collections.<Integer>emptyIterator();
        }

        @Override
        public Integer transition(Integer state, Decision decision) {
            return decision.variable() == 1 ? decision.value() : state;
        }

        @Override
        public double transitionCost(Integer state, Decision decision) {
            return decision.variable() == 1 ? 1 - decision.value() : 0;
        }

        @Override
        public Optional<Double> optimalValue() {
            return feasible ? Optional.of(1.0) : Optional.empty();
        }

        @Override
        public double evaluate(int[] solution) {
            return 1 - solution[1];
        }
    }

    private static DdoModel<Integer> model(TrapProblem problem, int width) {
        return new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public Relaxation<Integer> relaxation() {
                return new Relaxation<>() {
                    @Override
                    public Integer mergeStates(Iterator<Integer> states) {
                        return 1;
                    }

                    @Override
                    public double relaxEdge(
                            Integer from, Integer to, Integer merged, Decision d, double cost) {
                        return cost;
                    }
                };
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(width);
            }
        };
    }

    @Test
    void testRestrictionWithoutSolutionReturnsUnknown() {
        // width 1: the restriction keeps the cheaper state 1, which is a dead end
        Solution sol = Solvers.restrictedDdo(model(new TrapProblem(true), 1));

        assertTrue(sol.isEmpty());
        assertEquals(SearchStatus.UNKNOWN, sol.statistics().status());
    }

    @Test
    void testExactRestrictionWithoutSolutionReturnsUnsat() {
        Solution sol = Solvers.restrictedDdo(model(new TrapProblem(false), 10));

        assertTrue(sol.isEmpty());
        assertEquals(SearchStatus.UNSAT, sol.statistics().status());
    }

    @Test
    void testRestrictionWithSolutionReturnsSat() {
        Solution sol = Solvers.restrictedDdo(model(new TrapProblem(true), 10));

        assertFalse(sol.isEmpty());
        assertEquals(1.0, sol.value(), 1e-9);
        assertEquals(SearchStatus.SAT, sol.statistics().status());
    }
}
