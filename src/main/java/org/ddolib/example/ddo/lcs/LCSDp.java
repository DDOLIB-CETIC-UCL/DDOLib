package org.ddolib.example.ddo.lcs;

import java.util.Arrays;

/**
 * Naive DP solver the 2-strings longest common subsequence problem, used
 * to build a heuristic for the m-strings problem.
 */
public class LCSDp {
    int charNb;
    int[] s1;
    int[] s2;

    LCSDp(int charNb, int[] s1, int[] s2) {
        this.charNb = charNb;
        this.s1 = s1;
        this.s2 = s2;
    }

    /**
     * Solves the 2-strings LCS problem using a naive DP method.
     * Initiates the matrix and calls the heuristic.
     *
     * @return A Matrix containing the remaining LCS length based on the position in the strings.
     */
    int[][] solve() {
        // All cells initiated at -1
        int[][] table = new int[s1.length + 1][s2.length + 1];
        for (int i = 0; i <= s1.length; i++) {
            int[] initRow = new int[s2.length + 1];
            Arrays.fill(initRow, -1);
            table[i] = initRow;
        }

        // Changes the bottom row and right column values to 0
        for (int i = 0; i < s1.length; i++) {
            table[i][s2.length] = 0;
        }
        for (int j = 0; j <= s2.length; j++) {
            table[s1.length][j] = 0;
        }
        _solve(table, 0, 0);
        return table;
    }

    int _solve(int[][] table, int i, int j) {
        if (table[i][j] != -1) return table[i][j];


        // Max of (below, right, current + 1 (if same id))
        table[i][j] =
                Math.max(
                        Math.max(_solve(table, i + 1, j), _solve(table, i, j + 1)),
                        _solve(table, i + 1, j + 1) + ((s1[i] == s2[j]) ? 1 : 0));
        return table[i][j];
    }
}
