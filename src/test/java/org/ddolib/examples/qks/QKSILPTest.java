package org.ddolib.examples.qks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.ddolib.examples.qks.QKSILP.solveQuadraticKnapsack;

public class QKSILPTest {

    private List<QKSProblem> generateProblem() {
        String dir = Paths.get("src", "test", "resources", "qks").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);

        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new QKSProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    @Test
    protected void testOptimalIlp() throws IOException {
        for (QKSProblem problem : generateProblem()) {
            System.out.println(problem);
            double objective = solveQuadraticKnapsack(problem);
            Assertions.assertEquals(problem.optimal.get(), Math.abs(objective));
        }
    }
    
}
