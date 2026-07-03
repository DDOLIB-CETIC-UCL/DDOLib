package org.ddolib.common.solver.stopcriterion;

import org.ddolib.common.solver.stat.DdoStats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StopCriterionTest {

    @Test
    void testMaxTime() throws InterruptedException {
        StopCriterion criterion = StopCriterion.maxTime(100);
        DdoStats stats = new DdoStats(System.currentTimeMillis() - 150, 0.0);
        stats = stats.updateTime(System.currentTimeMillis());
        assertTrue(criterion.test(stats));

        stats = new DdoStats(System.currentTimeMillis(), 0.0);
        assertFalse(criterion.test(stats));
    }

    @Test
    void testMaxIter() {
        StopCriterion criterion = StopCriterion.maxIter(10);
        DdoStats stats = new DdoStats(System.currentTimeMillis(), 0.0);
        for (int i = 0; i < 9; i++) stats = stats.incrementNbIter();
        assertFalse(criterion.test(stats));
        stats = stats.incrementNbIter();
        assertTrue(criterion.test(stats));
    }

    @Test
    void testMaxTimeSinceLastImprovement() {
        StopCriterion criterion = StopCriterion.maxTimeSinceLastImprovement(100);
        long start = System.currentTimeMillis();
        DdoStats stats = new DdoStats(start, 1000.0);
        
        // No improvement for 150ms
        stats = stats.updateTime(start + 150);
        assertTrue(criterion.test(stats));

        // Improvement just now
        stats = stats.updateIncumbent(500.0, 50.0);
        assertFalse(criterion.test(stats));
    }

    @Test
    void testMaxIterSinceLastImprovement() {
        StopCriterion criterion = StopCriterion.maxIterSinceLastImprovement(5);
        DdoStats stats = new DdoStats(System.currentTimeMillis(), 1000.0);
        
        for (int i = 0; i < 4; i++) stats = stats.incrementNbIter();
        assertFalse(criterion.test(stats));
        
        stats = stats.incrementNbIter();
        assertTrue(criterion.test(stats));
        
        stats = stats.updateIncumbent(500.0, 50.0);
        assertFalse(criterion.test(stats));
    }

    @Test
    void testMinRelativeImprovement() {
        StopCriterion criterion = StopCriterion.minRelativeImprovement(10.0); // 10%
        DdoStats stats = new DdoStats(System.currentTimeMillis(), 1000.0);
        
        // 5% improvement (1000 -> 950)
        stats = stats.updateIncumbent(950.0, 95.0);
        assertTrue(criterion.test(stats), "5% < 10% should stop");

        // 20% improvement (950 -> 760)
        stats = stats.updateIncumbent(760.0, 76.0);
        assertFalse(criterion.test(stats), "20% > 10% should continue");
    }

    @Test
    void testMinGap() {
        StopCriterion criterion = StopCriterion.minGap(1.0); // 1%
        DdoStats stats = new DdoStats(System.currentTimeMillis(), 1000.0);
        
        stats = stats.updateGap(5.0);
        assertFalse(criterion.test(stats));
        
        stats = stats.updateGap(0.5);
        assertTrue(criterion.test(stats));
    }

    @Test
    void testMaxFrontierSize() {
        StopCriterion criterion = StopCriterion.maxFrontierSize(100);
        DdoStats stats = new DdoStats(System.currentTimeMillis(), 0.0);
        
        assertFalse(criterion.test(stats));
        
        stats = stats.updateFrontierMaxSize(150);
        assertTrue(criterion.test(stats));
    }

    @Test
    void testMaxIterWithoutGapImprovement() {
        StopCriterion criterion = StopCriterion.maxIterWithoutGapImprovement(5);
        DdoStats stats = new DdoStats(System.currentTimeMillis(), 1000.0);
        stats = stats.updateGap(100.0); // Iteration 0, gap 100
        
        // Iterations 1 to 5 without gap improvement
        for (int i = 0; i < 4; i++) {
            stats = stats.incrementNbIter();
            assertFalse(criterion.test(stats), "Should not stop at iteration " + (i + 1));
        }
        
        stats = stats.incrementNbIter(); // Iteration 5
        assertTrue(criterion.test(stats), "Should stop at iteration 5 if gap didn't improve");
        
        // Improve gap at iteration 6
        stats = stats.incrementNbIter();
        stats = stats.updateGap(90.0);
        assertFalse(criterion.test(stats), "Should not stop after gap improvement");
        
        // Check stagnation after improvement
        for (int i = 0; i < 4; i++) {
            stats = stats.incrementNbIter();
            assertFalse(criterion.test(stats));
        }
        stats = stats.incrementNbIter();
        assertTrue(criterion.test(stats), "Should stop after 5 more iterations without improvement");
    }

    @Test
    void testUpdateIncumbentUpdatesGapImprovement() {
        StopCriterion criterion = StopCriterion.maxIterWithoutGapImprovement(5);
        DdoStats stats = new DdoStats(System.currentTimeMillis(), 1000.0);
        stats = stats.updateGap(100.0);
        
        stats = stats.incrementNbIter();
        stats = stats.incrementNbIter();
        
        // Update incumbent with better gap
        stats = stats.updateIncumbent(500.0, 50.0);
        assertEquals(2, stats.lastIterationOfGapImprovement());
        assertFalse(criterion.test(stats));
    }
}
