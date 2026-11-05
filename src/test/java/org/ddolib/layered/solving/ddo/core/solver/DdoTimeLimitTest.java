package org.ddolib.layered.solving.ddo.core.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.examples.layered.tsptw.TSPTWDominance;
import org.ddolib.examples.layered.tsptw.TSPTWFastLowerBound;
import org.ddolib.examples.layered.tsptw.TSPTWProblem;
import org.ddolib.examples.layered.tsptw.TSPTWRanking;
import org.ddolib.examples.layered.tsptw.TSPTWRelax;
import org.ddolib.examples.layered.tsptw.TSPTWState;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.layered.solver.Solution;
import org.junit.jupiter.api.Test;

/**
 * Tests that the DDO solver stops when its stop criterion is met, even in the middle of the
 * compilation of a decision diagram.
 */
public class DdoTimeLimitTest {

    /** 233 nodes: the compilation of a single DD of width 100 takes several seconds. */
    private static final String INSTANCE =
            Path.of("data", "TSPTW", "AFG", "rbg233.2.tw").toString();

    private static DdoModel<TSPTWState> model(TSPTWProblem problem) {
        return new DdoModel<>() {
            @Override
            public Problem<TSPTWState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<TSPTWState> lowerBound() {
                return new TSPTWFastLowerBound(problem);
            }

            @Override
            public Relaxation<TSPTWState> relaxation() {
                return new TSPTWRelax(problem);
            }

            @Override
            public StateRanking<TSPTWState> ranking() {
                return new TSPTWRanking();
            }

            @Override
            public DominanceChecker<TSPTWState> dominance() {
                return new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
            }

            @Override
            public WidthHeuristic<TSPTWState> widthHeuristic() {
                return new FixedWidth<>(100);
            }
        };
    }

    @Test
    void testStopCriterionIsTestedDuringTheCompilation() throws IOException {
        TSPTWProblem problem = new TSPTWProblem(INSTANCE);
        // A criterion that stops at its 10th call. It used to be tested only once per
        // subproblem, so that the first compilations (233 layers each) were always completed.
        AtomicInteger calls = new AtomicInteger();
        Solution sol = Solvers.minimizeDdo(model(problem), s -> calls.incrementAndGet() >= 10);
        SearchStatistics stats = sol.statistics();

        assertEquals(10, calls.get());
        // the 10 calls happen while the first subproblem is compiled, not one per subproblem
        assertEquals(1, stats.nbIterations());
        assertNotEquals(SearchStatus.OPTIMAL, stats.status());
        // the interrupted subproblem is kept in the frontier: the bound remains valid
        assertTrue(stats.bestBound() <= stats.incumbent());
    }

    @Test
    void testTimeLimitIsRespected() throws IOException {
        TSPTWProblem problem = new TSPTWProblem(INSTANCE);
        long limit = 1000;
        long start = System.currentTimeMillis();
        Solution sol = Solvers.minimizeDdo(model(problem), s -> s.runtime() > limit);
        long runtime = System.currentTimeMillis() - start;

        // the limit used to be exceeded by more than 25 s on this instance
        assertTrue(runtime < limit + 3000, "runtime " + runtime + " ms for a limit of " + limit);
        assertNotEquals(SearchStatus.OPTIMAL, sol.statistics().status());
    }
}
