package org.ddolib.examples.lcs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Contains method to generate instances of the LCS
 */
public class LCSGenerator {

    /**
     * Generates and saves a random instance of the LCS
     *
     * @param fileName     The file the save the instance
     * @param nbString     How many strings to generate.
     * @param nbChar       How many characters can be used to generate the string.
     * @param minStrLength The inclusive minimal size of the generated strings.
     * @param maxStrLength The exclusive maximal size of the generated strings.
     * @param seed         The seed of the random number generator.
     * @throws IOException If something goes wrong while writing the file.
     */
    public static void generateRandomInstance(String fileName,
                                              int nbString,
                                              int nbChar,
                                              int minStrLength,
                                              int maxStrLength,
                                              long seed) throws IOException {

        Random rng = new Random(seed);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(nbString + " " + nbChar);
            for (int i = 0; i < nbString; i++) {
                bw.newLine();
                int strLength = rng.nextInt(minStrLength, maxStrLength);
                String str = rng.ints('A', 'A' + nbChar)
                        .limit(strLength)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString();
                bw.write(strLength + " " + str);
            }
        }
    }


    public static void main(String[] args) throws IOException {
        int nbString = 10;
        int nbChar = 5;
        int minStrLength = 100;
        int maxStringLength = 110;
        String fileName = String.format("data/LCS/LCS_%d_%d_%d-%d.txt", nbString, nbChar,
                minStrLength,
                maxStringLength);
        generateRandomInstance(fileName, nbString, nbChar, minStrLength, maxStringLength, 42);
    }
}
