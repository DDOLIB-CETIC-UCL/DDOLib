package org.ddolib.examples.ddo.carseq;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Instance generator for the Car Sequencing problem
 */
public class CSInstance {
    public static void main(String[] args) throws IOException {
        // Generate instances
        int nCars = 80;
        int[][] blockSize = {
            { 2, 3, 3, 5, 5 },
            { 4, 2, 4, 5, 3 },
            { 5, 4, 3, 3, 2 },
            { 2, 2, 3, 5, 4 },
            { 4, 3, 3, 4, 2 },
            { 5, 3, 5, 3, 5 },
            { 4, 3, 6, 2, 6 },
            { 2, 6, 5, 3, 3 },
            { 2, 4, 7, 7, 2 },
            { 7, 7, 7, 7, 7 }
        };
        int[][] blockMax = {
            { 1, 2, 1, 2, 1 },
            { 1, 1, 2, 3, 2 },
            { 2, 3, 1, 2, 1 },
            { 1, 1, 2, 2, 2 },
            { 3, 1, 2, 1, 1 },
            { 4, 1, 3, 2, 2 },
            { 3, 1, 4, 1, 5 },
            { 1, 3, 2, 2, 2 },
            { 1, 3, 2, 6, 1 },
            { 2, 3, 4, 5, 6 }
        };

        for (int i = 0; i < blockSize.length; i++) {
            CSProblem problem = generate(nCars, blockSize[i], blockMax[i]);
            write(problem, String.format("data/CarSeq/instance_%d_%d_%d.txt", nCars, blockSize[i].length, i));
        }
    }


    /**
     * Generate a problem with given options and number of cars
     * @param nCars Total number of cars to build
     * @param blockSize Size of the block for each option
     * @param blockMax For each option, max number of cars with the option in its block
     * @return The problem
     */
    public static CSProblem generate(int nCars, int[] blockSize, int[] blockMax) {
        if (blockSize.length > 64) { // So we can use longs instead of boolean[]
            throw new IllegalArgumentException("Number of options must be less than 64");
        }
        int nFull = Arrays.stream(blockMax).min().getAsInt();
        long[] options = new long[nCars + nFull];

        // Fill options
        for (int i = 0; i < blockSize.length; i++) {
            for (int j = 0; j < options.length; j += blockSize[i]) {
                for (int k = 0; k < blockMax[i] && j + k < options.length; k++) {
                    options[j + k] |= 1L << i;
                }
            }
        }

        // Find classes
        ArrayList<Integer> classSize = new ArrayList<>();
        ArrayList<boolean[]> classOptions = new ArrayList<>();
        HashMap<Long, Integer> firstOccurrence = new HashMap<>();
        for (int i = nFull; i < options.length; i++) {
            long car = options[i];
            Integer carClass = firstOccurrence.get(car);
            if (carClass == null) {
                firstOccurrence.put(car, classSize.size());
                classSize.add(1);
                boolean[] carOptions = new boolean[blockSize.length];
                for (int j = 0; j < blockSize.length; j++) {
                    if ((car & (1L << j)) != 0) {
                        carOptions[j] = true;
                    }
                }
                classOptions.add(carOptions);
            }
            else classSize.set(carClass, classSize.get(carClass) + 1);
        }

        return new CSProblem(
            classSize.stream().mapToInt(Integer::valueOf).toArray(),
            blockSize,
            blockMax,
            classOptions.toArray(new boolean[classOptions.size()][])
        );
    }


    /**
     * Write a problem to a file
     * @param problem The problem
     * @param filePath Path to the file
     */
    public static void write(CSProblem problem, String filePath) throws IOException {
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write number of cars, options and classes
            writer.write(String.format("%d %d %d\n", problem.nCars, problem.nOptions(), problem.nClasses()));

            // Write blockSize and blockMax
            writer.write(Arrays.stream(problem.blockMax).mapToObj(String::valueOf).collect(Collectors.joining(" ")) + "\n");
            writer.write(Arrays.stream(problem.blockSize).mapToObj(String::valueOf).collect(Collectors.joining(" ")) + "\n");

            // Write classSize and carOptions
            for (int i = 0; i < problem.nClasses(); i++) {
                final int fI = i;
                writer.write(String.format(
                    "%d %d %s\n", i, problem.classSize[i],
                    IntStream.range(0, problem.nOptions()).mapToObj(j -> problem.carOptions[fI][j] ? "1" : "0").collect(Collectors.joining(" "))
                ));
            }
        }
    }


    /**
     * Read a problem from a file
     * @param filePath Path to the file
     * @return The problem
     */
    public static CSProblem read(String filePath) throws IOException {
        File file = new File(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Read number of cars, options and classes
            String line = reader.readLine();
            String[] elements = line.split(" ");
            if (elements.length != 3) throw new IllegalArgumentException("Invalid input");
            int[] blockSize = new int[Integer.parseInt(elements[1])];
            int[] blockMax = new int[blockSize.length];
            int[] classSize = new int[Integer.parseInt(elements[2])];
            boolean[][] carOptions = new boolean[classSize.length][];

            // Read blockSize and blockMax
            line = reader.readLine();
            elements = line.split(" ");
            if (elements.length != blockMax.length) throw new IllegalArgumentException("Invalid input");
            for (int i = 0; i < blockMax.length; i++) {
                blockMax[i] = Integer.parseInt(elements[i]);
            }
            line = reader.readLine();
            elements = line.split(" ");
            if (elements.length != blockSize.length) throw new IllegalArgumentException("Invalid input");
            for (int i = 0; i < blockSize.length; i++) {
                blockSize[i] = Integer.parseInt(elements[i]);
            }

            // Read classSize and carOptions
            for (int i = 0; i < classSize.length; i++) {
                line = reader.readLine();
                elements = line.split(" ");
                if (elements.length != 2 + blockSize.length) throw new IllegalArgumentException("Invalid input");
                classSize[i] = Integer.parseInt(elements[1]);
                carOptions[i] = new boolean[blockSize.length];
                for (int j = 0; j < blockSize.length; j++) {
                    carOptions[i][j] = Integer.parseInt(elements[2 + j]) == 1;
                }
            }

            return new CSProblem(classSize, blockSize, blockMax, carOptions);
        }
    }
}