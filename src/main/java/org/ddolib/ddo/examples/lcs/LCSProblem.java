package org.ddolib.ddo.examples.lcs;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;

public class LCSProblem implements Problem<LCSState> {
    // Name of the instance
    String instance;
    // Number of string to compare.
    int stringNb;
    // Number of different chars encountered in the problem.
    int diffCharNb;
    // Input strings converted into integers.
    int[][] stringsAsInt;
    // Length of each string.
    int[] stringsLength;
    // Minimal string length
    int minLength;
    // nextCharPos[string][char][pos]
    // Next char position in string after pos.
    int[][][] nextCharPos;
    // remChar[string][char][pos]
    // Remaining occurrence of char in string after pos.
    int[][][] remChar;
    // Mapping from original character to its id.
    HashMap<Character, Integer> charToId = new HashMap<>();
    // Mapping from id to its original character.
    Character[] idToChar;
    // DP tables for 2-strings problems.
    int[][][] tables;
    // Optimal solution
    Optional<Integer> optimal;

    final int GO_TO_END_OF_STRINGS = -1;


    LCSProblem(String instance, int stringNb, int diffCharNb, int[][] stringsAsInt, int[] stringsLength, int[][][] nextCharPos,
               int[][][] remChar, HashMap<Character, Integer> charToId, Character[] idToChar, Optional<Integer> optimal) {
        this.instance = instance;
        this.stringNb = stringNb;
        this.diffCharNb = diffCharNb;
        this.stringsAsInt = stringsAsInt;
        this.stringsLength = stringsLength;
        this.nextCharPos = nextCharPos;
        this.remChar = remChar;
        this.charToId = charToId;
        this.idToChar = idToChar;
        this.minLength = Arrays.stream(stringsLength).min().orElse(0);
        this.optimal = optimal;

        int maxStringLength = Collections.max(Arrays.stream(stringsAsInt).map(x -> x.length).toList());

        this.tables = new int[stringNb - 1][maxStringLength][maxStringLength];
        for (int s = 0; s < stringNb - 1; s++) {
            LCSDp dp = new LCSDp(diffCharNb, stringsAsInt[s], stringsAsInt[s + 1]);
            tables[s] = dp.solve();
        }
    }

    public Optional<Integer> getOptimal() {
        return optimal;
    }

    @Override
    public int nbVars() {
        return minLength;
    }

    @Override
    public LCSState initialState() {
        int[] position = new int[stringNb];
        Arrays.fill(position, 0);
        return new LCSState(position);
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(LCSState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        boolean foundChar = false;

        // Keeps character that still exists in all the strings.
        // (based on current position in strings)
        for (int c = 0; c < diffCharNb; c++) {
            boolean valid = true;
            for (int s = 0; s < stringNb; s++) {
                if (remChar[s][c][state.position[s]] == 0) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                domain.add(c);
                foundChar = true;
            }
        }

        if (foundChar) return domain.iterator();
        else return List.of(GO_TO_END_OF_STRINGS).iterator();
    }

    @Override
    public LCSState transition(LCSState state, Decision decision) {
        int[] position = Arrays.copyOf(stringsLength, stringsLength.length);

        if (decision.val() != GO_TO_END_OF_STRINGS) {
            int c = decision.val();
            // Move current position of each string based on selected character.
            for (int s = 0; s < stringNb; s++) {
                position[s] = nextCharPos[s][c][state.position[s]] + 1;
            }
        }
        return new LCSState(position);
    }

    @Override
    public int transitionCost(LCSState state, Decision decision) {
        if (decision.val() == GO_TO_END_OF_STRINGS) return 0;
        return 1;
    }
}
