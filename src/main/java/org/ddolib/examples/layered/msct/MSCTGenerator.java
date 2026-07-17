package org.ddolib.examples.layered.msct;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * Contains methods to generates instance of the MSCT
 */
public class MSCTGenerator {

    private MSCTGenerator() {
    }

    private static MSCTData randomMSCTData(int n, long seed) {
        Random rng = new Random(seed);
        int[] release = new int[n];
        int[] processing = new int[n];
        for (int i = 0; i < n; i++) {
            release[i] = rng.nextInt(n * 100);
            processing[i] = 1 + rng.nextInt(n * 100);
        }
        return new MSCTData(release, processing);
    }

    private static MSCTData randomMSCTDataFixedRelease(int n, int release, long seed) {
        MSCTData res = randomMSCTData(n, seed);
        Arrays.fill(res.release, release);
        return res;
    }

    /**
     * Generates and writes instances for the MSCT
     *
     * @param fname the file to save the instance
     * @param n     the number of task in the problem
     * @param seed  the seed of the random number generator
     * @throws IOException if something goes wrong while writing the file
     */
    public static void writeInstance(String fname, int n, long seed) throws IOException {
        MSCTData data = randomMSCTData(n, seed);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
            bw.write(n + "\n");
            for (int i = 0; i < n; i++) {
                bw.write(data.release[i] + " " + data.processing[i] + "\n");
            }
        }
    }

    /**
     * Entry point of the program. Generates a random MSCT instance with 12 tasks and writes it
     * to {@code data/MSCT/12_tasks.txt}.
     *
     * @param args command-line arguments (not used)
     * @throws IOException if something goes wrong while writing the file
     */
    public static void main(String[] args) throws IOException {
        int n = 12;
        String fname = "data/MSCT/" + n + "_tasks.txt";
        writeInstance(fname, n, new Random().nextLong());
    }

    private record MSCTData(int[] release, int[] processing) {
    }
}
