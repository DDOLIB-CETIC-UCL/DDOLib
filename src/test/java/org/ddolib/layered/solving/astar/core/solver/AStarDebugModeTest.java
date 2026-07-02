package org.ddolib.layered.solving.astar.core.solver;

import org.ddolib.layered.examples.misp.MispProblem;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.util.debug.DebugLevel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying the debug mode behaviors of AStarSolver.
 */
public class AStarDebugModeTest {

    private static Model<BitSet> getFailingFlbModel(String instance, DebugLevel debugLvl) throws IOException {
        final MispProblem problem = new MispProblem(instance);
        return new Model<>() {
            @Override
            public Problem<BitSet> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<BitSet> lowerBound() {
                return (state, variables) -> variables.isEmpty() ? 0.0 : 1000.0;
            }

            @Override
            public DebugLevel debugMode() {
                return debugLvl;
            }
        };
    }

    @Test
    public void debugModeDetectFlbError() throws IOException {
        final String instance = Path.of("src", "test", "resources", "MISP", "tadpole_4_2.dot").toString();
        Model<BitSet> model = getFailingFlbModel(instance, DebugLevel.ON);

        // Expecting a RuntimeException because the lower bound is invalid (not admissible)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Solvers.minimizeAstar(model);
        });
        assertTrue(exception.getMessage().contains("lower bound is not admissible"));
    }

    @Test
    public void debugModeOffDoesNotThrow() throws IOException {
        final String instance = Path.of("src", "test", "resources", "MISP", "tadpole_4_2.dot").toString();

        // With failing FLB but debug mode off, the solver should run to completion without throwing an exception
        Model<BitSet> failingFlbModel = getFailingFlbModel(instance, DebugLevel.OFF);
        assertDoesNotThrow(() -> {
            Solvers.minimizeAstar(failingFlbModel);
        });
    }
}
