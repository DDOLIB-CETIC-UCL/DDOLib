package org.ddolib.examples.layered.tsp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.testbench.TestDataSupplier;

/**
 * Supplies the Traveling Salesman Problem (TSP) instances (read from the files of a directory) and
 * the layered model used to solve them in the tests.
 */
public class TSPTestDataSupplier extends TestDataSupplier<TSPState, TSPProblem> {

    private final Path dir;

    /**
     * Creates a supplier reading the instances from the given directory.
     *
     * @param dir the directory containing the instance files
     */
    public TSPTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<TSPProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(
                            filePath -> {
                                try {
                                    return new TSPProblem(filePath.toString());
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
