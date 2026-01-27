package org.ddolib.examples.qks;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class QKSGenerator {

    public static void main(String[] args) {
        xpInstance();
    }

    /**
     * Creates a few small instances for debugging and testing purposes.
     */
    private static void debugInstance() {
        for (int n : new int[] {3, 5}) {
            for (double sparsity: new double[] { 0.25, 0.5, 0.75 }) {
                for (int seed = 0; seed < 5; seed++) {
                    createInstance(n, sparsity, seed, "src/test/resources/QKS/", true);
                }
            }
        }
    }

    /**
     * Creates instances for experiments
     */
    private static void xpInstance() {
        for (int n : new int[] {15, 25, 50, 100}) {
            for (double sparsity: new double[] { 0.25, 0.5, 0.75 }) {
                for (int seed = 0; seed < 5; seed++) {
                    createInstance(n, sparsity, seed, "data/QKS/", false);
                }
            }
        }
    }


    private static void createInstance(int n, double sparsity, int seed, String dirPath, boolean computeOptimum) {
        QKSProblem instance = new QKSProblem(n ,sparsity, seed, computeOptimum);

        int sparsityInteger = (int) (sparsity * 100);
        String instanceName = dirPath + String.format("qks_%d_%d_%d.txt", n, sparsityInteger, seed);
        try {
            FileWriter fw = new FileWriter(instanceName, false);
            fw.write(instance.instanceFormat());
            fw.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }



}
