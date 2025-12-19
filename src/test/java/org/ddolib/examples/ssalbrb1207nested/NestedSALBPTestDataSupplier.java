package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;
import org.ddolib.util.testbench.TestDataSupplier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class NestedSALBPTestDataSupplier extends TestDataSupplier<NestedSALBPState, NestedSALBPProblem> {
    private final Path dir;
    private final int cycleTime;
    private final int totalRobots;

    public NestedSALBPTestDataSupplier(Path dir) {
        this(dir, 200, 3);
    }

    public NestedSALBPTestDataSupplier(Path dir, int cycleTime, int totalRobots) {
        this.dir = dir;
        this.cycleTime = cycleTime;
        this.totalRobots = totalRobots;
    }

    @Override
    protected List<NestedSALBPProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".alb"))
                    .map(filePath -> {
                        try {
                            return new NestedSALBPProblem(filePath.toString(), cycleTime, totalRobots);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected DdoModel<NestedSALBPState> model(NestedSALBPProblem problem) {
        System.out.println(problem);
        return new DdoModel<>() {
            @Override
            public Problem<NestedSALBPState> problem() {
                return problem;
            }

            @Override
            public Relaxation<NestedSALBPState> relaxation() {
                return new NestedSALBPRelax();
            }

            @Override
            public StateRanking<NestedSALBPState> ranking() {
                return new NestedSALBPRanking(problem.totalRobots);
            }

            @Override
            public FastLowerBound<NestedSALBPState> lowerBound() {
                return new NestedSALBPFastLowerBound(problem);
            }

            @Override
            public WidthHeuristic<NestedSALBPState> widthHeuristic() {
                return new FixedWidth<>(2);
            }
        };
    }
}
