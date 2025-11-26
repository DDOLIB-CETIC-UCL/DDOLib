package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestUnit;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class KSTestUnit extends TestUnit<Integer, KSProblem> {

    private final String dir;

    public KSTestUnit(String dir) {
        this.dir = dir;
    }

    @Override
    protected List<KSProblem> generateProblems() {
        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);

        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new KSProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    @Override
    protected DdoModel<Integer> model(KSProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
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
            public Relaxation<Integer> relaxation() {
                return new KSRelax();
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
            }
        };
    }
}
