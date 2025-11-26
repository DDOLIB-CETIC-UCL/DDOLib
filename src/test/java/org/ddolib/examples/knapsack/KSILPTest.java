package org.ddolib.examples.knapsack;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class KSILPTest {

    protected List<KSProblem> generateProblems() {
        String dir = Paths.get("src", "test", "resources", "Knapsack").toString();

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

    @Test
    protected void testOptimalILP() throws IOException {
        for (KSProblem problem : generateProblems()) {
            double objective = KSILP.solveKnapsack(problem);
            Assertions.assertEquals(problem.optimal.get(), objective);
        }
    }

}
