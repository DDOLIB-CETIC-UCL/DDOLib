package org.ddolib.examples.misp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Contains methods to generate instances of the MISP.
 */
public class MispGenerator {

    /**
     * Generates a random instance of the MISP.
     *
     * @param nbVars         How many nodes in the graph.
     * @param fileName       The file to save the instance.
     * @param connectedProba Each pair of nodes has a probability of {@code 1 / connectedProba}
     *                       to <i>not</i> be connected.
     * @param seed           The seed of the random number generator.
     * @throws IOException If something goes wrong while writing the file
     */
    public static void generateRandom(int nbVars, String fileName, int connectedProba, long seed) throws IOException {
        Random rng = new Random(seed);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write("strict graph G {\n");
            for (int i = 1; i <= nbVars; i++) {
                int w = rng.nextInt(20);
                bw.write(String.format("%d [weight=%d];\n", i, w));
            }

            for (int i = 1; i <= nbVars; i++) {
                for (int j = i + 1; j <= nbVars; j++) {
                    int connected = rng.nextInt(connectedProba);
                    if (connected != 0) {
                        bw.write(String.format("%d -- %d;\n", i, j));
                    }

                }
            }
            bw.write("}");
        }
    }

    /**
     * Generates a random instance of the MISP.
     *
     * @param nbVars         How many nodes in the graph.
     * @param fileName       The file to save the instance.
     * @param connectedProba Each pair of nodes has a probability of {@code 1 / connectedProba}
     *                       to <i>not</i> be connected.
     * @throws IOException If something goes wrong while writing the file
     */
    public static void generateRandom(int nbVars, String fileName, int connectedProba) throws IOException {
        long seed = new Random().nextLong();
        System.out.printf("Used seed: %d\n", seed);
        generateRandom(nbVars, fileName, connectedProba, seed);
    }

    public static void main(String[] args) throws IOException {
        generateRandom(500, "data/MISP/500_nodes_3.dot", 42);
    }
}
