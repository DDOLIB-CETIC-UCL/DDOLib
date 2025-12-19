package org.ddolib.util;

import java.util.HashSet;
import java.util.Set;
/**
 * Utility class to compute lower bounds for the Traveling Salesman Problem (TSP).
 * <p>
 * This class provides methods to compute a simple yet effective lower bound on the
 * cost of visiting subsets of nodes in a TSP instance, based on the minimum incident
 * edges for each node in the subset.
 * </p>
 *
 * <p>
 * The lower bound is used in more complex problems (like Production Scheduling Problem)
 * where the TSP component appears in the calculation of minimum changeover costs.
 * </p>
 */
public class TSPLowerBound {

    /**
     * Computes a lower-bound for all subsets of a set of nodes based on the given cost matrix.
     * <p>
     * For a given cost matrix, this method calculates a lower bound for each subset of nodes
     * {0, ..., n-1}, where n is the size of the matrix. The lower bound is computed by
     * summing the minimum incident edge of each node in the subset, considering only edges
     * connecting nodes within the subset.
     * </p>
     *
     * <p>
     * Each subset of nodes is represented by an integer using its binary representation:
     * the i-th bit is 1 if node i is included in the subset, 0 otherwise.
     * </p>
     *
     * @param costMatrix a square matrix of size n x n representing the cost between nodes
     * @return an array of integers where the i-th element represents the lower bound
     *         for the subset corresponding to the binary representation of i
     */
    public static int[] lowerBoundForAllSubsets(int[][] costMatrix) {
        int nItems = costMatrix.length;
        int nPoss = 1 << nItems; // 2 ^ nItems = number of all possible subsets
        int[] result = new int[nPoss];
        for (int i = 0; i < nPoss; i++) {
            Set<Integer> members = intToSet(i, nItems);
            result[i] = (sumMinIncident(members, costMatrix));
        }
        return result;
    }
    /**
     * Computes the sum of the minimum incident edges for a given subset of nodes.
     * <p>
     * For each node in the subset, find the smallest edge connecting it to another node
     * in the subset and sum these minimum edges. Each node is counted at most once
     * to avoid double-counting edges.
     * </p>
     *
     * @param members a set of node indices representing the subset
     * @param changeover a square cost matrix
     * @return the sum of the minimum incident edges for the subset
     */
    private static int sumMinIncident(Set<Integer> members, int[][] changeover) {
        if (members.size() <= 1) {
            return 0;
        }
        Set<Integer> covered = new HashSet<>();
        int total = 0;
        for (int a : members) {
            if (covered.contains(a)) {
                continue;
            }
            int emin = Integer.MAX_VALUE; // minimum edge adjacent to a
            int bmin = a; // the other end of the minimum edge
            for (int b : members) {
                if (a == b) {
                    continue;
                }
                int edge = Math.min(changeover[a][b], changeover[b][a]);
                if (edge < emin) {
                    emin = edge;
                    bmin = b;
                }
            }
            total += emin;
            covered.add(a);
            covered.add(bmin);
        }
        return total;
    }
    /**
     * Converts an integer representation of a subset to a set of node indices.
     * <p>
     * Each bit in the integer represents whether the corresponding node is included
     * in the subset (1) or not (0).
     * </p>
     *
     * @param num an integer representing the subset in binary
     * @param size the total number of nodes
     * @return a set of integers corresponding to the indices of nodes in the subset
     */
    private static Set<Integer> intToSet(int num, int size) {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            if ((num & (1 << i)) != 0) {
                set.add(i);
            }
        }
        return set;
    }
}
