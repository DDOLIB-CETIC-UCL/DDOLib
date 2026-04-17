package org.ddolib.examples.pdptw;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;

public class GenData {

    public static void main(final String[] args) throws IOException {
        Random r = new Random(2);
        for(int n = 10 ;  n <= 20; n += 5) {
            for (int i = 0; i < 10; i++) {
                final PDPTWProblem problem = PDPTWGenerator.genInstance(n, 3, 5, r,false);
                String path = Paths.get("data", "PDPTW", "instance_" + n + "_" + i).toAbsolutePath().toString();
                problem.saveToFile(path);
            }
        }
    }
}
