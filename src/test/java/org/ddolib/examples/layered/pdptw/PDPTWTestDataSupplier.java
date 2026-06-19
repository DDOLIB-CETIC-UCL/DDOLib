package org.ddolib.examples.layered.pdptw;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
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
