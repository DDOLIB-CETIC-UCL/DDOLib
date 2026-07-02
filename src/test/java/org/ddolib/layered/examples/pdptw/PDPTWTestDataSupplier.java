package org.ddolib.layered.examples.pdptw;

import org.ddolib.common.frontier.CutSetType;
import org.ddolib.common.frontier.Frontier;
import org.ddolib.common.frontier.SimpleFrontier;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.util.testbench.TestDataSupplier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class PDPTWTestDataSupplier extends TestDataSupplier<PDPTWState, PDPTWProblem> {

    private final Path dir;

    public PDPTWTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<PDPTWProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return new PDPTWProblem(filePath.toString());
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
    protected DdoModel<PDPTWState> model(PDPTWProblem problem) {
        return new DdoModel<>() {
            @Override
            public Problem<PDPTWState> problem() {
                return problem;
            }

            @Override
            public PDPTWFastLowerBound lowerBound() {
                return new PDPTWFastLowerBound(problem);
            }

            @Override
            public PDPTWRelax relaxation() {
                return new PDPTWRelax(problem);
            }

            @Override
            public PDPTWRanking ranking() {
                return new PDPTWRanking();
            }

            @Override
            public WidthHeuristic<PDPTWState> widthHeuristic() {
                return new FixedWidth<>(1000);
            }

            @Override
            public Frontier<PDPTWState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public DominanceChecker<PDPTWState> dominance() {
                return new SimpleDominanceChecker<>(new PDPTWDominance(), problem.nbVars());
            }
        };
    }
}
