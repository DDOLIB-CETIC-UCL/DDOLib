package org.ddolib.examples.maximumcoverage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class MaxCoverILPTest {
    protected List<MaxCoverProblem> generateProblems() {
        String dir = Paths.get("src", "test", "resources", "MaxCover").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);

        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new MaxCoverProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    @Test
    protected void testOptimalILP() throws IOException {
        for (MaxCoverProblem problem : generateProblems()) {
            double objective = MaxCoverILP.solveMaxCover(problem);
            Assertions.assertEquals(problem.optimal.get(), objective);
        }
    }
}
