package org.ddolib.examples.layered.tsptw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.ddolib.common.frontier.CutSetType;
import org.ddolib.common.frontier.Frontier;
import org.ddolib.common.frontier.SimpleFrontier;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.common.util.verbosity.VerbosityLevel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.testbench.TestDataSupplier;

/**
 * Supplies the Traveling Salesman Problem with Time Windows (TSPTW) instances (read from the files
 * of a directory) and the layered model used to solve them in the tests.
 */
public class TSPTWTestDataSupplier extends TestDataSupplier<TSPTWState, TSPTWProblem> {

    private final Path dir;

    /**
     * Creates a supplier reading the instances from the given directory.
     *
     * @param dir the directory containing the instance files
     */
    public TSPTWTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<TSPTWProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(
                            filePath -> {
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
