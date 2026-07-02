package org.ddolib.layered.examples.pdptw;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Utility entry point to generate PDPTW benchmark instances.
 */
public class GenData {

    /**
     * Generates a small batch of PDPTW instances and writes them under {@code data/PDPTW}.
     *
     * @param args ignored
     * @throws IOException if writing an instance fails
     */
    public static void main(final String[] args) throws IOException {
        Random r = new Random(2);
        for (int n = 30; n <= 30; n += 5) {
            for (int i = 0; i < 10; i++) {
                final PDPTWProblem problem = PDPTWGenerator.constructInstanceWithSolution(n, 3, 7, r, false);
                String path = Paths.get("data", "PDPTW", "instance_" + n + "_" + i).toAbsolutePath().toString();
                problem.saveToFile(path);
            }
        }
    }
}
