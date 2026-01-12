package org.ddolib.examples.misp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.ddolib.examples.misp.MispILP.solveMisp;

public class MispILPTest {

    private List<MispProblem> generateProblem() {
        String dir = Paths.get("src", "test", "resources", "misp").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);

        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new MispProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    @Test
    protected void testOptimalIlp() throws IOException {
        for (MispProblem problem : generateProblem()) {
            System.out.println(problem);
            double objective = solveMisp(problem);
            Assertions.assertEquals(problem.optimal.get(), objective);
        }
    }

}
