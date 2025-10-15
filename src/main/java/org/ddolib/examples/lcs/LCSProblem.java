package org.ddolib.examples.lcs;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

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
    Optional<Double> optimal;

    final int GO_TO_END_OF_STRINGS = -1;

    private Optional<String> name = Optional.empty();

    public LCSProblem(String instance, int stringNb, int diffCharNb, int[][] stringsAsInt, int[] stringsLength, int[][][] nextCharPos,
               int[][][] remChar, HashMap<Character, Integer> charToId, Character[] idToChar, Optional<Double> optimal) {
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

    public LCSProblem(final String filename) throws IOException {
        HashMap<Character, Integer> charToId = new HashMap<>();

        int stringNb;
        String firstLine;
        List<String> lines = new ArrayList<>();
        try (final BufferedReader bf = new BufferedReader(new FileReader(filename))) {
            firstLine = bf.readLine();
            String line;
            while ((line = bf.readLine()) != null) lines.add(line);
        }

        // Extracts first line data :
        // Number of different chars; Number of strings; Optimal (if known)
        String[] splitFirst = firstLine.split("\\s+");
        stringNb = Integer.parseInt(splitFirst[0]);
        int diffCharNb = Integer.parseInt(splitFirst[1]);
        int[] stringsLength = new int[stringNb];
        Character[] idToChar = new Character[diffCharNb];
        Optional<Double> optimal;
        if (splitFirst.length == 3) optimal = Optional.of(-Double.parseDouble(splitFirst[2]));
        else optimal = Optional.empty();


        // Extracts input strings.
        // String size; string
        Character[][] stringsAsChars = lines.stream().map(ls -> {
            String[] sls = ls.split("\\s+");
            char[] charArray = sls[1].toCharArray();
            Character[] characterArray = new Character[charArray.length];
            for (int i = 0; i < charArray.length; i++) {
                characterArray[i] = charArray[i];
                // Fills the char <-> id conversion arrays.
                if (!charToId.containsKey(characterArray[i])) {
                    idToChar[charToId.size()] = characterArray[i];
                    charToId.put(characterArray[i], charToId.size());
                }
            }
            return characterArray;
        }).toArray(Character[][]::new);

        // Maps the array of chars to their ids.
        int[][] stringsAsInt =
                Arrays.stream(stringsAsChars).map(x ->
                        Arrays.stream(x).mapToInt(charToId::get).toArray()
                ).toArray(int[][]::new);
        for (int i = 0; i < stringNb; i++) {
            stringsLength[i] = stringsAsChars[i].length;
        }

        // Instantiates other structures.
        // For each string S, char C, pos P; The position of the next C after P in S
        int[][][] nextCharPos =
                IntStream.range(0, stringNb).mapToObj(s ->
                                IntStream.range(0, diffCharNb).mapToObj(c ->
                                        IntStream.range(0, stringsLength[s] + 1).map(p -> stringsLength[s]).toArray()
                                ).toArray(int[][]::new))
                        .toArray(int[][][]::new);
        // For each string S, char C, pos P; The number of remaining C after P in S
        int[][][] remChar =
                IntStream.range(0, stringNb).mapToObj(s ->
                                IntStream.range(0, diffCharNb).mapToObj(c ->
                                        IntStream.range(0, stringsLength[s] + 1).map(p -> 0).toArray()
                                ).toArray(int[][]::new))
                        .toArray(int[][][]::new);

        // Populates other structures.
        for (int s = 0; s < stringNb; s++) {
            for (int c = stringsLength[s] - 1; c >= 0; c--) {
                int id = charToId.get(stringsAsChars[s][c]);
                for (int p = 0; p <= c; p++) {
                    remChar[s][id][p] += 1;
                    nextCharPos[s][id][p] = c;
                }
            }
        }
        this.instance = filename;
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



    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public String toString() {
        return name.orElse(super.toString());
    }

    @Override
    public Optional<Double> optimalValue() {
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
    public double initialValue() {
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
    public double transitionCost(LCSState state, Decision decision) {
        if (decision.val() == GO_TO_END_OF_STRINGS) return 0;
        return -1;
    }
}
