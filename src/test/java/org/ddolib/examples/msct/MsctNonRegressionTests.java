package org.ddolib.examples.msct;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Tag("non-regression")
public class MsctNonRegressionTests {

    @DisplayName("MSCT: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMsct() {
        var supplier =
                new MsctNonRegressionDataSupplier(Paths.get("src", "test", "resources", "Non-Regression",
                        "MSCT").toString());
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }

    private static class MsctNonRegressionDataSupplier extends MSCTTestDataSupplier {

        private final String dir;

        private MsctNonRegressionDataSupplier(String dir) {
            this.dir = dir;
        }

        @Override
        protected List<MSCTProblem> generateProblems() {
            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);
            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            return new MSCTProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }
    }
}
