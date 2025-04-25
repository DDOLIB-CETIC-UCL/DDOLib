package org.ddolib.ddo.examples.lcs;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Problem;
import org.ddolib.ddo.core.Relaxation;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.heuristics.WidthHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/** The LCS problem consists in finding the Longest Common Subsequence between several strings of characters.*/
public final class LCS {

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
        Optional<Integer> optimal;
        if (splitFirst.length == 3) optimal = Optional.of(Integer.parseInt(splitFirst[2]));
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

        return new LCSProblem(stringNb, diffCharNb, stringsAsInt, stringsLength, nextCharPos, remChar, charToId, idToChar, optimal);
    }

    public static void main(String[] args) throws IOException {
        final String file = args.length == 0 ? "src/test/resources/LCS/LCS_3_26_50-60_test.txt" : args[0];
        final int maxWidth = args.length >= 2 ? Integer.parseInt(args[1]) : 250;

        LCSProblem problem = extractFile(file);
        LCSRelax relax = new LCSRelax(problem);
        LCSRanking ranking = new LCSRanking();

        final FixedWidth<LCSState> width = new FixedWidth<>(maxWidth);
        final VariableHeuristic<LCSState> varH = new DefaultVariableHeuristic<>();
        final Frontier<LCSState> frontier = new SimpleFrontier<>(ranking);

        ParallelSolver<LCSState,Integer> solver = new ParallelSolver<>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varH,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize(1);
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] values = new int[problem.nbVars()];
                    for (Decision d : decisions) {
                        values[d.var()] = d.val();
                    }
                    return values;
                })
                .orElse(new int[0]);

        int[] filteredSolution = Arrays.stream(solution).filter(x -> x >=0).toArray();
        Character[] charNbSolution = Arrays.stream(filteredSolution).
                mapToObj(x -> problem.idToChar[x]).toArray(Character[]::new);

        System.out.printf("Instance : %s%n", file);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().orElse(Integer.MIN_VALUE));
        System.out.printf("Upper Bnd : %s%n", solver.upperBound());
        System.out.printf("Lower Bnd : %s%n", solver.lowerBound());
        System.out.printf("Explored : %s%n", solver.explored());
        System.out.printf("Max width : %d%n", maxWidth);
        System.out.printf("Solution : %s%n", Arrays.toString(charNbSolution));
    }

}