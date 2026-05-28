package org.ddolib.common.solver.stopcriterion;

import org.ddolib.common.solver.stat.DdoStats;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DdoStopCriterionTest {

    @Test
    void testMaxTotalMddNodes() {
        DdoStopCriterion criterion = DdoStopCriterion.maxTotalMddNodes(500);
        DdoStats stats = new DdoStats(System.currentTimeMillis(), 1000.0);
        
        assertFalse(criterion.test(stats));
        
        stats = stats.addNodes(300);
        assertFalse(criterion.test(stats));
        
        stats = stats.addNodes(200);
        assertTrue(criterion.test(stats));
    }

    @Test
    void testMaxIterWithoutLowerBoundImprovement() {
        DdoStopCriterion criterion = DdoStopCriterion.maxIterWithoutLowerBoundImprovement(5);
        DdoStats stats = new DdoStats(System.currentTimeMillis(), 1000.0);
        
        // Initial LB at iteration 0
        stats = stats.updateLowerBound(10.0);
        
        // Iterations 1 to 5 without improvement
        for (int i = 0; i < 4; i++) {
            stats = stats.incrementNbIter();
            assertFalse(criterion.test(stats));
        }
        
        stats = stats.incrementNbIter(); // Iteration 5
        assertTrue(criterion.test(stats));
        
        // Improve LB at iteration 6
        stats = stats.incrementNbIter();
        stats = stats.updateLowerBound(15.0);
        assertFalse(criterion.test(stats));
    }

    @Test
    void testMaxExploredDepth() {
        DdoStopCriterion criterion = DdoStopCriterion.maxExploredDepth(10);
        DdoStats stats = new DdoStats(System.currentTimeMillis(), 1000.0);
        
        assertFalse(criterion.test(stats));
        
        stats = stats.updateMaxDepth(8);
        assertFalse(criterion.test(stats));
        
        stats = stats.updateMaxDepth(10);
        assertTrue(criterion.test(stats));
    }

    @Test
    void testWrongStatsThrowsException() {
        DdoStopCriterion criterion = DdoStopCriterion.maxExploredDepth(10);
        assertThrows(IllegalArgumentException.class, () -> criterion.test(new DummyStats(0, 0)));
    }

    private static class DummyStats extends SearchStatistics<DummyStats> {
        public DummyStats(long startTime, double initValue) { super(startTime, initValue); }
        @Override protected DummyStats createSpecificInstance() { return new DummyStats(0, 0); }
    }
}
