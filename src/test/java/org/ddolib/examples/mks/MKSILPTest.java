package org.ddolib.examples.mks;

import org.ddolib.examples.knapsack.KSILP;
import org.ddolib.examples.knapsack.KSProblem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class MKSILPTest {

    protected List<MKSProblem> generateProblems() {
        String dir = Paths.get("src", "test", "resources", "MKS").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);

        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new MKSProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    @Test
    protected void testOptimalILP() throws IOException {
        for (MKSProblem problem : generateProblems()) {
            System.out.println(problem.name);
            double objective = MKSILP.solveMultiKnapsack(problem);
            Assertions.assertEquals(problem.optimal.get(), objective);
        }
    }

}
