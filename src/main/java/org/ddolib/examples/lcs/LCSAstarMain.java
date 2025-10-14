package org.ddolib.examples.lcs;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * The LCS problem consists in finding the Longest Common Subsequence between several strings of characters.
 */
public final class LCSAstarMain {

    public static void main(String[] args) throws IOException {
        final String file = "src/test/resources/LCS/LCS_3_3_10_test.txt";
        Model<LCSState> model = new Model<>() {
            private LCSProblem problem;

            @Override
            public Problem<LCSState> problem() {
                try {
                    problem = extractFile(file);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public LCSFastLowerBound lowerBound() {
                return new LCSFastLowerBound(problem);
            }
        };

        Solver<LCSState> solver = new Solver<>();
        SearchStatistics stats = solver.minimizeAstar(model);
        System.out.println(stats);

    }

    public static LCSProblem extractFile(String fileName) throws IOException {
        final File f = new File(fileName);

        HashMap<Character, Integer> charToId = new HashMap<>();

        int stringNb;
        String firstLine;
        List<String> lines = new ArrayList<>();
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
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

        return new LCSProblem(fileName, stringNb, diffCharNb, stringsAsInt, stringsLength, nextCharPos, remChar, charToId, idToChar, optimal);
    }


}