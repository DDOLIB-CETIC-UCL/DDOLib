package org.ddolib.examples.msct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

class MsctNonRegressionDataSupplier extends MSCTTestDataSupplier {

    private final String dir;

    public MsctNonRegressionDataSupplier(String dir) {
        this.dir = dir;
    }

    @Override
    protected List<MSCTProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(Path.of(dir))) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return new MSCTProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}