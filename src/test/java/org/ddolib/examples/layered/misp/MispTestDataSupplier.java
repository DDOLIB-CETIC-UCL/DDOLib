package org.ddolib.examples.layered.misp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.common.util.verbosity.VerbosityLevel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.testbench.TestDataSupplier;

/**
 * Supplies the Maximum Independent Set Problem (MISP) instances (read from the files of a
 * directory) and the layered model used to solve them in the tests.
 */
public class MispTestDataSupplier extends TestDataSupplier<BitSet, MispProblem> {

    private final Path dir;

    /**
     * Creates a supplier reading the instances from the given directory.
     *
     * @param dir the directory containing the instance files
     */
    public MispTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<MispProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(
                            filePath -> {
                                try {
                                    return new MispProblem(filePath.toString());
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
    protected DdoModel<BitSet> model(MispProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<BitSet> problem() {
                return problem;
            }

            @Override
            public MispFastLowerBound lowerBound() {
                return new MispFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<BitSet> dominance() {
                return new SimpleDominanceChecker<>(new MispDominance(), problem.nbVars());
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
            public MispRelax relaxation() {
                return new MispRelax(problem);
            }

            @Override
            public MispRanking ranking() {
                return new MispRanking();
            }
        };
    }
}
