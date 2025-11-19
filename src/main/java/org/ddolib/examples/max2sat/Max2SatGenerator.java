package org.ddolib.examples.max2sat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Methods to generate random instance of the Max2Sat problem.
 */
public class Max2SatGenerator {

    public static void generateRandomInstance(int numVar, String fileName, long seed) throws IOException {

        ArrayList<Integer> literal = new ArrayList<>();
        for (int i = 1; i <= numVar; i++) {
            literal.add(i);
            literal.add(-i);
        }

        ArrayList<BinaryClause> pairs = new ArrayList<>();
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

        List<BinaryClause> selected = pairs.subList(0, rng.nextInt(0, pairs.size() + 1));
        Collections.sort(selected);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write("" + numVar);
            for (BinaryClause bc : selected) {
                bw.newLine();
                int w = rng.nextInt(1, 11);
                String line = String.format("%d %d %d", bc.i, bc.j, w);
                bw.write(line);
            }
        }
    }

    public static void generateRandomInstance(int numVar, String fileName) throws IOException {
        Random random = new Random();
        long seed = random.nextLong();
        random.setSeed(seed);

        System.out.printf("Used seed: %d%n", seed);
        generateRandomInstance(numVar, fileName, random.nextLong());
    }

    public static void main(String[] args) throws IOException {
        generateRandomInstance(50, "data/Max2Sat/wcnf_var_50.txt");
    }

}
