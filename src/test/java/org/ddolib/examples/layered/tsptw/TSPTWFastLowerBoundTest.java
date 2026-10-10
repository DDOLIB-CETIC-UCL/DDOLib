package org.ddolib.examples.layered.tsptw;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.ExactModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.modeling.StateRanking;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests of the fast lower bound of the TSPTW ({@link TSPTWFastLowerBound}). */
public class TSPTWFastLowerBoundTest {

    @TempDir Path tmp;

    private TSPTWProblem write(String content) throws IOException {
        Path file = tmp.resolve("instance.txt");
        Files.writeString(file, content);
        return new TSPTWProblem(file.toString());
    }

    @Test
    void testZeroCostEdgesDoNotForceTheDirectReturnToTheDepot() throws IOException {
        // From node 1, going back to the depot directly costs 5 but the detour 1 -> 2 -> 0 is free
        TSPTWProblem problem = write("3\n0 0 0\n5 0 0\n0 0 0\n0 100\n0 100\n0 100\n");

        // the debug mode checks that the bound is admissible on every state
        double exact = Solvers.minimizeExact(exactModel(problem, DebugLevel.ON)).value();
        assertEquals(0.0, exact, 1e-9);
        assertEquals(0.0, Solvers.minimizeDdo(ddoModel(problem, 1)).value(), 1e-9);
    }

    @Test
    void testDdoFindsTheOptimumOnRandomInstances() throws IOException {
        Random random = new Random(3);
        for (int it = 0; it < 200; it++) {
            int n = 3 + random.nextInt(4);
            StringBuilder sb = new StringBuilder(n + "\n");
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    int d = i == j || random.nextInt(3) == 0 ? 0 : random.nextInt(10);
                    sb.append(d).append(j + 1 < n ? " " : "\n");
                }
            }
            for (int i = 0; i < n; i++) {
                sb.append("0 ").append(i == 0 ? 1000 : 50 + random.nextInt(100)).append("\n");
            }
            TSPTWProblem problem = write(sb.toString());
            // reference optimum computed without any bound
            double optimum = Solvers.minimizeExact(withoutBound(problem)).value();
            for (int width : new int[] {1, 2}) {
                assertEquals(optimum, Solvers.minimizeDdo(ddoModel(problem, width)).value(), 1e-9);
            }
        }
    }

    private static ExactModel<TSPTWState> withoutBound(TSPTWProblem problem) {
        return new ExactModel<>() {
            @Override
            public Problem<TSPTWState> problem() {
                return problem;
            }
        };
    }

    private static ExactModel<TSPTWState> exactModel(TSPTWProblem problem, DebugLevel debug) {
        return new ExactModel<>() {
            @Override
            public Problem<TSPTWState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<TSPTWState> lowerBound() {
                return new TSPTWFastLowerBound(problem);
            }

            @Override
            public DebugLevel debugMode() {
                return debug;
            }
        };
    }

    private static DdoModel<TSPTWState> ddoModel(TSPTWProblem problem, int width) {
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
            public WidthHeuristic<TSPTWState> widthHeuristic() {
                return new FixedWidth<>(width);
            }
        };
    }
}
