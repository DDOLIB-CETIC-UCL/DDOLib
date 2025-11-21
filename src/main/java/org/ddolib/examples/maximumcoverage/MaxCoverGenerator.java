package org.ddolib.examples.maximumcoverage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.*;

public class MaxCoverGenerator {

    public static void main(String[] args) {
        debugInstance();
    }

    private static void createInstance(int n, int m, int k, double maxR, int index, String dirPath, boolean computeOptimum) {
        MaxCoverProblem instance = new MaxCoverProblem(n, m, k, maxR, (int) (Math.random()*100000));
        if (computeOptimum) {
            double optimum = bruteForce(instance);
            instance.optimal = Optional.of(optimum);
        }

        String instanceName = dirPath + String.format("mc_n%d_m%d_k%d_r%d_%d.txt", n, m, k, (int) ceil(100*maxR), index);
        try {
            FileWriter fw = new FileWriter(instanceName, false);
            fw.write(instance.instanceFormat());
            fw.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void measureInstance() {
        int[] ns = {100, 150, 200};
        double[] mFactors = {0.5, 0.8};
        double[] kFactors = {0.1, 0.2};
        double[] maxRs = {0.1, 0.2};
        for (int n: ns) {
            for (double mFactor: mFactors) {
                int m = (int) ceil(mFactor * n);
                for (double kFactor: kFactors) {
                    int k = (int) ceil(kFactor * m);
                    for (double maxR: maxRs) {
                        for (int index = 0; index < 10; index++) {
                            createInstance(n, m, k, maxR, index, "data/MaxCover/", false);
                        }
                    }
                }
            }
        }
    }

    private static void debugInstance() {
        createInstance(10, 5, 3, 0.1, 0, "src/test/resources/MaxCover/", true);
        createInstance(15, 8, 4, 0.1, 0, "src/test/resources/MaxCover/", true);
        createInstance(15, 10, 5, 0.1, 0, "src/test/resources/MaxCover/", true);
        createInstance(15, 12, 5, 0.1, 0, "src/test/resources/MaxCover/", true);
        createInstance(15, 10, 5, 0.1, 1, "src/test/resources/MaxCover/", true);
    }

    /**
     * Solve top optimality a MaxCoverProblem with brute-force
     * @param instance
     * @return
     */
    private static double bruteForce(MaxCoverProblem instance) {
        double bestObjective = -1;
        List<Set<Integer>> combinations = generateCombinations(instance.nbSubSets, instance.nbSubSetsToChoose);
        for (Set<Integer> combination : combinations) {
            assert combination.size() == instance.nbSubSetsToChoose;
            BitSet coveredElements = new BitSet(instance.nbItems);
            for (Integer subSet : combination) {
                coveredElements.or(instance.subSets[subSet]);
            }
            bestObjective = max(bestObjective, coveredElements.cardinality());
        }
        return bestObjective;
    }

    /**
     * Generate all non-ordered combination of k elements among n
     * @param n the total number of element
     * @param k the size of the combination
     * @return a list containing the different combination, represented by sets
     */
    public static List<Set<Integer>> generateCombinations(int n, int k) {
        List<Set<Integer>> result = new ArrayList<>();
        backtrack(0, n, k, new ArrayList<>(), result);
        return result;
    }

    /**
     * Backtrack method to generation combination
     * @param start
     * @param n
     * @param k
     * @param tempList
     * @param result
     */
    private static void backtrack(int start, int n, int k, List<Integer> tempList, List<Set<Integer>> result) {
        if (tempList.size() == k) {
            result.add(new HashSet<>(tempList));
            return;
        }

        for (int i = start; i < n; i++) {
            tempList.add(i);
            backtrack(i + 1, n, k, tempList, result);
            tempList.removeLast(); // backtrack
        }
    }
}
