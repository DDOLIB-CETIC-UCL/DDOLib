package org.ddolib.solving.ddo.core.mdd;

import org.ddolib.solving.ddo.core.Decision;
import org.ddolib.solving.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.solving.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.layered.misp.MispProblem;
import org.ddolib.modeling.layered.*;
import org.ddolib.util.debug.DebugLevel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DebugModeTest {

    private static ExactModel<BitSet> getFailingFlbModel(String instance, DebugLevel debugLvl) throws IOException {
        final MispProblem problem = new MispProblem(instance);
        return new ExactModel<>() {
            @Override
            public Problem<BitSet> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<BitSet> lowerBound() {
                return (state, variables) -> 1000;
            }

            @Override
            public DebugLevel debugMode() {
                return debugLvl;
            }
        };
    }

    public static DdoModel<BitSet> getFailingRelaxationModel(String instance, DebugLevel debugLvl) throws IOException {
        final MispProblem problem = new MispProblem(instance);

        return new DdoModel<>() {
            @Override
            public WidthHeuristic<BitSet> widthHeuristic() {
                return new FixedWidth<>(2);
            }

            @Override
            public Relaxation<BitSet> relaxation() {
                return new Relaxation<>() {
                    @Override
                    public BitSet mergeStates(Iterator<BitSet> states) {
                        var merged = new BitSet(problem.nbVars());
                        while (states.hasNext()) {
                            final BitSet state = states.next();
                            // the merged state is the union of all the state
                            merged.and(state);
                        }
                        return merged;
                    }

                    @Override
                    public double relaxEdge(BitSet from, BitSet to, BitSet merged, Decision d, double cost) {
                        return cost;
                    }


                };
            }

            @Override
            public Problem<BitSet> problem() {
                return problem;
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
        ExactModel<BitSet> model = getFailingFlbModel(instance, DebugLevel.ON);

        // Expecting a RuntimeException because the lower bound is invalid
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Solvers.minimizeExact(model);
        });
        assertTrue(exception.getMessage().contains("lower bound"));

    }

    @Test
    public void debugModeDetectRelaxationError() throws IOException {
        final String instance = Path.of("src", "test", "resources", "MISP", "tadpole_4_2.dot").toString();
        DdoModel<BitSet> model = getFailingRelaxationModel(instance, DebugLevel.ON);

        // Expecting a RuntimeException because the lower bound is invalid
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Solvers.minimizeDdo(model);
        });
        assertTrue(exception.getMessage().contains("Found relaxed node that lead to worst solution"));
    }

    @Test
    public void debugModeDetectRelaxedCostError() throws IOException {
        final String instance = Path.of("src", "test", "resources", "MISP", "tadpole_4_2.dot").toString();
        final MispProblem problem = new MispProblem(instance);
        DdoModel<BitSet> model = new DdoModel<>() {

            @Override
            public WidthHeuristic<BitSet> widthHeuristic() {
                return new FixedWidth<>(2);
            }

            @Override
            public Relaxation<BitSet> relaxation() {
                return new Relaxation<>() {
                    @Override
                    public BitSet mergeStates(Iterator<BitSet> states) {
                        var merged = new BitSet(problem.nbVars());
                        while (states.hasNext()) {
                            final BitSet state = states.next();
                            // the merged state is the union of all the state
                            merged.or(state);
                        }
                        return merged;
                    }

                    @Override
                    public double relaxEdge(BitSet from, BitSet to, BitSet merged, Decision d, double cost) {
                        return cost + 1000;
                    }


                };
            }

            @Override
            public Problem<BitSet> problem() {
                return problem;
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };

        // Expecting a RuntimeException because the lower bound is invalid
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Solvers.minimizeDdo(model);
        });
        assertTrue(exception.getMessage().contains("Found relaxed node that lead to worst solution"));
    }

    @Test
    public void debugModeExportMDDFlbError() throws IOException {
        final String instance = Path.of("src", "test", "resources", "MISP", "tadpole_4_2.dot").toString();
        ExactModel<BitSet> model = getFailingFlbModel(instance, DebugLevel.EXTENDED);

        // Expecting a RuntimeException because the lower bound is invalid
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Solvers.minimizeExact(model);
        });
        assertTrue(exception.getMessage().contains("MDD saved in output/failed.dot"));
    }

    @Test
    public void debugModeExportMddRelaxationError() throws IOException {
        final String instance = Path.of("src", "test", "resources", "MISP", "tadpole_4_2.dot").toString();
        final MispProblem problem = new MispProblem(instance);
        DdoModel<BitSet> model = getFailingRelaxationModel(instance, DebugLevel.EXTENDED);


        // Expecting a RuntimeException because the lower bound is invalid
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Solvers.minimizeDdo(model);
        });
        assertTrue(exception.getMessage().contains("MDD saved in output/failed.dot"));
    }


}
