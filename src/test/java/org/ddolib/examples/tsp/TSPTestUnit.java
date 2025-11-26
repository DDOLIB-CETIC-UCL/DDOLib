package org.ddolib.examples.tsp;

import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.testbench.TestUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TSPTestUnit extends TestUnit<TSPState, TSPProblem> {

    private final String dir;

    public TSPTestUnit(String dir) {
        this.dir = dir;
    }

    @Override
    protected List<TSPProblem> generateProblems() {
        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);

        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new TSPProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    @Override
    protected DdoModel<TSPState> model(TSPProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<TSPState> problem() {
                return problem;
            }

            @Override
            public TSPFastLowerBound lowerBound() {
                return new TSPFastLowerBound(problem);
            }

            @Override
            public Relaxation<TSPState> relaxation() {
                return new TSPRelax(problem);
            }

            @Override
            public TSPRanking ranking() {
                return new TSPRanking();
            }

            @Override
            public WidthHeuristic<TSPState> widthHeuristic() {
                return new FixedWidth<>(500);
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };
    }
}
