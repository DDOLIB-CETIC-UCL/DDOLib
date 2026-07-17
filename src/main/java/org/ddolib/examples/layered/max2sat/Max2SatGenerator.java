package org.ddolib.examples.layered.max2sat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Methods to generate random instance of the Max2Sat problem.
 */
public class Max2SatGenerator {

    private Max2SatGenerator() {
    }

    /**
     * Generates and writes a random instance of the Max2Sat.
     *
     * @param fileName  the file to save the instance
     * @param numVar    the number of variables in the problem
     * @param nbClauses the number of disjunctive clauses in the problem
     * @param seed      the seed of the random number generator
     * @throws IOException if something goes wrong while writing the file
     */
    public static void generateRandomInstance(String fileName,
                                              int numVar,
                                              int nbClauses,
                                              long seed) throws IOException {

        List<Integer> literal = new ArrayList<>();
        for (int i = 1; i <= numVar; i++) {
            literal.add(i);
            literal.add(-i);
        }

        List<BinaryClause> pairs = new LinkedList<>();
        for (int i = 0; i < literal.size(); i++) {
            for (int j = i + 1; j < literal.size(); j++) {
                int xi = literal.get(i);
                int xj = literal.get(j);
                if (Math.abs(xi) != Math.abs(xj)) {
                    pairs.add(new BinaryClause(xi, xj));
                }
            }
        }
        Random rng = new Random(seed);
        Collections.shuffle(pairs, rng);

        int nbSelected = nbClauses < 0 ? rng.nextInt(pairs.size()) + 1 : nbClauses;

        List<BinaryClause> selected = pairs.subList(0, nbSelected);
        Collections.sort(selected);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write("" + numVar);
            for (BinaryClause bc : selected) {
                bw.newLine();
                int w = rng.nextInt(1, 11);
                String line = String.format("%d %d %d", bc.i(), bc.j(), w);
                bw.write(line);
            }
        }
    }

    /**
     * Generates and writes a random instance of the Max2Sat.
     *
     * @param fileName  the file to save the instance
     * @param numVar    the number of variables in the problem
     * @param nbClauses the number of disjunctive clauses in the problem
     * @throws IOException if something goes wrong while writing the file
     */
    public static void generateRandomInstance(String fileName, int numVar, int nbClauses) throws IOException {
        Random random = new Random();
        long seed = random.nextLong();
        random.setSeed(seed);

        System.out.printf("Used seed: %d%n", seed);
        generateRandomInstance(fileName, numVar, nbClauses, random.nextLong());
    }

    /**
     * Entry point of the program. Generates a random Max2Sat instance with 42 variables and 500
     * clauses and writes it to {@code data/Max2Sat/wcnf_var_42.txt}.
     *
     * @param args command-line arguments (not used)
     * @throws IOException if something goes wrong while writing the file
     */
    public static void main(String[] args) throws IOException {
        generateRandomInstance("data/Max2Sat/wcnf_var_42.txt", 42, 500, 42);
    }

}
