package org.ddolib.layered.examples.tsptw;

import org.ddolib.common.frontier.CutSetType;
import org.ddolib.common.frontier.Frontier;
import org.ddolib.common.frontier.SimpleFrontier;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class TSPTWTestDataSupplier extends TestDataSupplier<TSPTWState, TSPTWProblem> {

    private final Path dir;

    public TSPTWTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<TSPTWProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return new TSPTWProblem(filePath.toString());
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
    protected DdoModel<TSPTWState> model(TSPTWProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<TSPTWState> problem() {
                return problem;
            }

            @Override
            public TSPTWFastLowerBound lowerBound() {
                return new TSPTWFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<TSPTWState> dominance() {
                return new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }

            @Override
            public TSPTWRelax relaxation() {
                return new TSPTWRelax(problem);
            }

            @Override
            public TSPTWRanking ranking() {
                return new TSPTWRanking();
            }

            @Override
            public Frontier<TSPTWState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }
        };
    }
}
