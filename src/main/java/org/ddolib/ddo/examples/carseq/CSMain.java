package org.ddolib.ddo.examples.carseq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class CSMain {
    public static void main(String[] args) throws IOException {

    }


    public static CSProblem readInstance(final String fname) throws IOException {
        File f = new File(fname);
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            // Parse number of cars, options and classes
            String line = bf.readLine();
            String[] elements = line.split(" ");
            if (elements.length != 3) throw new IllegalArgumentException("Invalid input");
            int[] blockSize = new int[Integer.parseInt(elements[1])];
            int[] blockMax = new int[blockSize.length];
            int[] classSize = new int[Integer.parseInt(elements[2])];
            boolean[][] carOptions = new boolean[classSize.length][];

            // Parse blockSize and blockMax
            line = bf.readLine();
            elements = line.split(" ");
            if (elements.length != blockMax.length) throw new IllegalArgumentException("Invalid input");
            for (int i = 0; i < blockMax.length; i++) {
                blockMax[i] = Integer.parseInt(elements[i]);
            }
            line = bf.readLine();
            elements = line.split(" ");
            if (elements.length != blockSize.length) throw new IllegalArgumentException("Invalid input");
            for (int i = 0; i < blockSize.length; i++) {
                blockSize[i] = Integer.parseInt(elements[i]);
            }

            // Parse carClass and carOptions
            for (int i = 0; i < classSize.length; i++) {
                line = bf.readLine();
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
