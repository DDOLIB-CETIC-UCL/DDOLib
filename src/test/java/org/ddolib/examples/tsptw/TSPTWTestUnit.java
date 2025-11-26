package org.ddolib.examples.tsptw;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestUnit;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TSPTWTestUnit extends TestUnit<TSPTWState, TSPTWProblem> {

    private final String dir;

    public TSPTWTestUnit(String dir) {
        this.dir = dir;
    }

    @Override
    protected List<TSPTWProblem> generateProblems() {
        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new TSPTWProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
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
