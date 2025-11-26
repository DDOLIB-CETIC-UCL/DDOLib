package org.ddolib.examples.pigmentscheduling;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.util.testbench.TestUnit;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class PSTestUnit extends TestUnit<PSState, PSProblem> {

    private final String dir;

    public PSTestUnit(String dir) {
        this.dir = dir;
    }

    @Override
    protected List<PSProblem> generateProblems() {
        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);

        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> new PSProblem(filePath.toString())).toList();
    }

    @Override
    protected DdoModel<PSState> model(PSProblem problem) {
        return new DdoModel<>() {

            @Override
            public PSProblem problem() {
                return problem;
            }

            @Override
            public FastLowerBound<PSState> lowerBound() {
                return new PSFastLowerBound(problem);
            }

            @Override
            public PSRelax relaxation() {
                return new PSRelax(problem);
            }

            @Override
            public PSRanking ranking() {
                return new PSRanking();
            }
        };
    }
}
