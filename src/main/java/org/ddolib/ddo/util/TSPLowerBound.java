package org.ddolib.ddo.util;

import java.util.*;

public class TSPLowerBound {

    /**
     * Given a nxn cost matrix,
     * compute a lower-bound for all possible subsets of {0,...,n-1}.
     * @param costMatrix
     * @return an array of integers, where the i-th element is lower-bound
     *          for the subset corresponding to the binary representation of i.
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

    private static int sumMinIncident(Set<Integer> members, int [][] changeover) {
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
