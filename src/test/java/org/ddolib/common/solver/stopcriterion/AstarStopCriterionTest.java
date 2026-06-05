package org.ddolib.common.solver.stopcriterion;

import org.ddolib.common.solver.stat.AstarStats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AstarStopCriterionTest {

    @Test
    void testMinValidChildrenPercent() {
        AstarStopCriterion criterion = AstarStopCriterion.minValidChildrenPercent(20.0);
        AstarStats stats = new AstarStats(System.currentTimeMillis(), 1000.0);
        
        // Initial ratio is 1.0 (100%), so it should not stop
        assertFalse(criterion.test(stats));
        
        // Update with 1 selected child out of 10 generated (10%)
        // With alpha=0.05, new ratio = 0.05*0.1 + 0.95*1.0 = 0.005 + 0.95 = 0.955
        stats = stats.updateValidChildrenPercent(10, 1);
        assertFalse(criterion.test(stats));
        
        // Force a low ratio for testing by repeating the update
        for (int i = 0; i < 100; i++) {
            stats = stats.updateValidChildrenPercent(100, 1); // 1%
        }
        
        assertTrue(stats.validChildrenPercent() < 20.0);
        assertTrue(criterion.test(stats));
    }

    @Test
    void testWrongStatsThrowsException() {
        AstarStopCriterion criterion = AstarStopCriterion.minValidChildrenPercent(20.0);
        assertThrows(IllegalArgumentException.class, () -> criterion.test(new org.ddolib.common.solver.stat.DdoStats(0, 0)));
    }
}
