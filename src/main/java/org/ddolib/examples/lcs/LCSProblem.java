package org.ddolib.examples.lcs;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Definition of the Longest Common Subsequence (LCS) problem.
 * <p>
 * The LCS problem consists in finding the longest sequence of characters that appears
 * in the same relative order in a set of strings. This class models the problem in a
 * way suitable for state-space search solvers.
 * </p>
 * <p>
 * The state of the problem is represented by the current position in each string,
 * and the decisions correspond to selecting a character to include in the LCS next.
 * The class precomputes several auxiliary structures to efficiently determine:
 * </p>
 * <ul>
 *     <li>Next occurrence of each character after a given position in each string.</li>
 *     <li>Remaining occurrences of each character after a given position in each string.</li>
 *     <li>Dynamic programming tables for optimal LCS of string pairs.</li>
 * </ul>
 */
public class LCSProblem implements Problem<LCSState> {
    /**
     * Name or identifier of the instance.
     */
    String instance;

    /**
     * Number of strings in the problem.
     */
    int stringNb;

    /**
     * Number of different characters in the input strings.
     */
    int diffCharNb;

    /**
     * Input strings converted to integer IDs per character.
     */
    int[][] stringsAsInt;

    /**
     * Length of each string.
     */
    int[] stringsLength;

    /**
     * Minimal string length among all strings.
     */
    int minLength;

    /**
     * Next occurrence of a character after a given position for each string.
     */
    int[][][] nextCharPos;

    /**
     * Number of remaining occurrences of a character after a given position in each string.
     */
    int[][][] remChar;

    /**
     * Mapping from character to integer ID.
     */
    HashMap<Character, Integer> charToId = new HashMap<>();

    /**
     * Mapping from integer ID to original character.
     */
    Character[] idToChar;

    /**
     * Precomputed dynamic programming tables for pairwise string LCS.
     */
    int[][][] tables;

    /**
     * Known optimal solution, if available.
     */
    Optional<Double> optimal;

    /**
     * Special value indicating no more characters to select.
     */
    final int GO_TO_END_OF_STRINGS = -1;

    private Optional<String> name = Optional.empty();

    /**
     * Constructs an LCS problem instance with all precomputed structures.
     *
     * @param instance      The instance name or path.
     * @param stringNb      Number of strings.
     * @param diffCharNb    Number of distinct characters.
     * @param stringsAsInt  Input strings encoded as integer arrays.
     * @param stringsLength Length of each string.
     * @param nextCharPos   Next occurrence of each character after each position.
     * @param remChar       Remaining occurrences of each character after each position.
     * @param charToId      Mapping from character to ID.
     * @param idToChar      Mapping from ID to character.
     * @param optimal       Optional optimal solution value.
     */
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

    /**
     * Constructs an LCS problem instance from a file.
     * <p>
     * The file should contain the number of strings, number of characters,
     * and optionally the optimal solution in the first line, followed by
     * each string in the format: length string_content.
     * </p>
     *
     * @param filename Path to the file defining the LCS instance.
     * @throws IOException If reading the file fails.
     */

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
            //TODO remove -
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

    /**
     * Sets a human-readable name for this problem instance.
     *
     * @param name The name to assign.
     */
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

    /**
     * Computes the domain of possible next decisions (characters) for a given state.
     * <p>
     * Only characters that exist in all strings from their current positions are included.
     * If no such character exists, a special value {@link #GO_TO_END_OF_STRINGS} is returned.
     * </p>
     *
     * @param state The current LCS state.
     * @param var   Index of the variable to decide (unused, all positions considered).
     * @return An iterator over valid next character IDs.
     */
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

    /**
     * Computes the next state resulting from applying a decision at the current state.
     *
     * @param state    The current LCS state.
     * @param decision The decision applied.
     * @return The next LCS state after the decision.
     */
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

    /**
     * Computes the transition cost of a decision from the current state.
     * <p>
     * The cost is -1 for selecting a character to maximize the LCS length, and 0
     * if no character is selected (end of strings).
     * </p>
     *
     * @param state    The current LCS state.
     * @param decision The decision applied.
     * @return The cost associated with the transition.
     */
    @Override
    public double transitionCost(LCSState state, Decision decision) {
        if (decision.val() == GO_TO_END_OF_STRINGS) return 0;
        return -1;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not match " +
                    "the number %d variables", Arrays.toString(solution), nbVars()));
        }


        int[] end = Arrays.stream(solution).dropWhile(x -> x != -1).toArray();
        if (Arrays.stream(end).anyMatch(x -> x != -1)) {
            String msg = String.format("The %d characters of %s are not all at the end.", -1,
                    Arrays.toString(solution));
            throw new InvalidSolutionException(msg);
        }

        return solution.length - end.length;
    }
}
