package org.ddolib.examples.ssalbrb;

import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.testbench.TestDataSupplier;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class SSALBRBTestDataSupplier extends TestDataSupplier<SSALBRBState, SSALBRBProblem> {
    private final Path dir;
    public SSALBRBTestDataSupplier(Path dir) {this.dir = dir;}

    @Override
    protected List<SSALBRBProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return new SSALBRBProblem(filePath.toString());
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
    protected DdoModel<SSALBRBState> model(SSALBRBProblem problem) {
        System.out.println(problem);
        return new DdoModel<>() {
            @Override
            public Problem<SSALBRBState> problem() {
                return problem;
            }


            @Override
            public Relaxation<SSALBRBState> relaxation() {
                return new SSALBRBRelax(problem.humanDurations, problem.robotDurations, problem.collaborationDurations);
            }

            @Override
            public SSALBRBRanking ranking() {
                return new SSALBRBRanking();
            }

            @Override
            public FastLowerBound<SSALBRBState> lowerBound() {
                return new SSALBRBFastLowerBound(problem);
            }

            @Override
            public WidthHeuristic<SSALBRBState> widthHeuristic() {
                return new FixedWidth<>(2);
            }

        };
    }
}
