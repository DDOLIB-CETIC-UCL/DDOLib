package org.ddolib.examples.binpacking;

import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.modeling.*;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestDataSupplier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class BPPTestDataSupplier extends TestDataSupplier<BPPState, BPPProblem> {

    private final Path dir;

    public BPPTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<BPPProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return BPP.extractFile(filePath.toString());
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
    protected DdoModel<BPPState> model(BPPProblem problem) {
        return new DdoModel<>() {
            private final BPPRanking ranking = new BPPRanking();

            @Override
            public Relaxation<BPPState> relaxation() {
                return new BPPRelax(problem) {
                };
            }

            @Override
            public Problem<BPPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<BPPState> lowerBound() {
                return new BPPFastLowerBound(problem);
            }

            @Override
            public StateRanking<BPPState> ranking() {
                return ranking;
            }

            @Override
            public Frontier<BPPState> frontier() {
                return new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };
    }
}
