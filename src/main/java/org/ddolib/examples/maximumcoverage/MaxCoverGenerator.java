package org.ddolib.examples.maximumcoverage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.*;
/**
 * Utility class for generating and measuring instances of the Maximum Coverage (MaxCover) problem.
 *
 * <p>
 * This class provides methods to:
 * <ul>
 *   <li>Create random MaxCover instances with specified parameters</li>
 *   <li>Optionally compute the optimal solution using brute-force</li>
 *   <li>Generate multiple instances for benchmarking</li>
 *   <li>Generate all combinations of k elements among n for brute-force computation</li>
 * </ul>
 *
 * <p>
 * Generated instances are saved to disk in a specific text format.
 */
public class MaxCoverGenerator {
    /**
     * Program entry point.
     *
     * <p>
     * By default, calls {@link #measureInstance()} to generate benchmark instances.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        measureInstance();
    }
    /**
     * Creates a single MaxCover instance and optionally computes its optimal value.
     *
     * <p>
     * The instance is written to a file in the specified directory using a standard naming convention.
     *
     * @param n number of items
     * @param m number of subsets
     * @param k number of subsets to select
     * @param maxR maximum ratio for random coverage
     * @param seed random seed
     * @param dirPath directory path to save the instance
     * @param computeOptimum whether to compute the optimal solution via brute-force
     */
    private static void createInstance(int n, int m, int k, double maxR, int seed, String dirPath, boolean computeOptimum) {
        MaxCoverProblem instance = new MaxCoverProblem(n, m, k, maxR, seed);
        if (computeOptimum) {
            double optimum = bruteForce(instance);
            instance.optimal = Optional.of(optimum);
        }

        String instanceName = dirPath + String.format("mc_n%d_m%d_k%d_r%d_%d.txt", n, m, k, (int) ceil(100*maxR), seed);
        try {
            FileWriter fw = new FileWriter(instanceName, false);
            fw.write(instance.instanceFormat());
            fw.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    /**
     * Generates multiple benchmark instances with varying parameters.
     *
     * <p>
     * Parameter ranges are defined for number of items, subset ratios, selection ratios,
     * maximum coverage ratio, and random seeds.
     * Instances are written to the "data/MaxCover/" directory.
     */
    private static void measureInstance() {
        int[] ns = {100, 150, 200};
        double[] mFactors = {0.5, 0.8};
        double[] kFactors = {0.1, 0.2};
        double[] maxRs = {0.1, 0.2};
        int nbSeeds = 10;

        for (int n: ns) {
            for (double mFactor: mFactors) {
                int m = (int) ceil(mFactor * n);
                for (double kFactor: kFactors) {
                    int k = (int) ceil(kFactor * m);
                    for (double maxR: maxRs) {
                        for (int seed = 0; seed < nbSeeds; seed++) {
                            createInstance(n, m, k, maxR, seed, "data/MaxCover/", false);
                        }
                    }
                }
            }
        }
    }
    /**
     * Creates a few small instances for debugging and testing purposes.
     */
    private static void debugInstance() {
        createInstance(10, 5, 3, 0.1, 0, "src/test/resources/MaxCover/", true);
        createInstance(15, 8, 4, 0.1, 0, "src/test/resources/MaxCover/", true);
        createInstance(15, 10, 5, 0.1, 0, "src/test/resources/MaxCover/", true);
        createInstance(15, 12, 5, 0.1, 0, "src/test/resources/MaxCover/", true);
        createInstance(15, 10, 5, 0.1, 1, "src/test/resources/MaxCover/", true);
    }

    /**
     * Computes the exact optimal solution of a MaxCover problem using brute-force.
     *
     * <p>
     * All combinations of k subsets are considered and the one covering the maximum number of items is selected.
     *
     * @param instance the MaxCover problem instance
     * @return the maximum number of items that can be covered
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
     * Generates all non-ordered combinations of k elements from n.
     *
     * @param n the total number of elements
     * @param k the size of each combination
     * @return a list of sets, each representing a combination of k elements
     */
    public static List<Set<Integer>> generateCombinations(int n, int k) {
        List<Set<Integer>> result = new ArrayList<>();
        backtrack(0, n, k, new ArrayList<>(), result);
        return result;
    }

    /**
     * Recursive backtracking helper to generate all combinations.
     *
     * @param start the starting index for this recursion
     * @param n total number of elements
     * @param k target combination size
     * @param tempList temporary list holding the current combination
     * @param result list of all generated combinations
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
