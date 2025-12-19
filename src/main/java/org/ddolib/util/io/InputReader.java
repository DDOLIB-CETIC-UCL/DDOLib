/*
 * MaxiCP is under MIT License
 * Copyright (c)  2023 UCLouvain
 */


package org.ddolib.util.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * Utility class to read formatted input from a file.
 * <p>
 * This class provides convenient methods to read integers, doubles, lines of integers,
 * matrices of numbers, and individual strings from a text file. It uses a
 * {@link BufferedReader} for efficient line-by-line reading and a {@link StringTokenizer}
 * to parse tokens from each line.
 * <p>
 * The methods throw a {@link RuntimeException} when an I/O error occurs or when
 * attempting to read beyond the end of the file.
 */
public class InputReader {

    private BufferedReader in;
    private StringTokenizer tokenizer;
    /**
     * Constructs an InputReader for the specified file.
     *
     * @param file the path to the input file
     * @throws RuntimeException if the file cannot be opened
     */
    public InputReader(String file) {
        try {

            FileInputStream istream = new FileInputStream(file);
            in = new BufferedReader(new InputStreamReader(istream));
            tokenizer = new StringTokenizer("");
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
    /**
     * Reads the next integer from the input.
     *
     * @return the next integer
     * @throws RuntimeException if the end of file is reached or parsing fails
     */
    public Integer getInt() throws RuntimeException {
        if (!tokenizer.hasMoreTokens()) {
            try {
                String line;
                do {
                    line = in.readLine();
                    if (line == null) {
                        //System.out.println("No more line to read");
                        throw new RuntimeException("End of file");
                    }
                    tokenizer = new StringTokenizer(line);
                } while (!tokenizer.hasMoreTokens());

            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }
        return Integer.parseInt(tokenizer.nextToken());
    }
    /**
     * Reads the next double from the input.
     *
     * @return the next double
     * @throws RuntimeException if the end of file is reached or parsing fails
     */
    public double getDouble() throws RuntimeException {
        if (!tokenizer.hasMoreTokens()) {
            try {
                String line;
                do {
                    line = in.readLine();
                    if (line == null) {
                        //System.out.println("No more line to read");
                        throw new RuntimeException("End of file");
                    }
                    tokenizer = new StringTokenizer(line);
                } while (!tokenizer.hasMoreTokens());

            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }
        return Double.parseDouble(tokenizer.nextToken());
    }
    /**
     * Reads a matrix of doubles with the given dimensions.
     *
     * @param n number of rows
     * @param m number of columns
     * @return a 2D array containing the matrix
     * @throws RuntimeException if reading fails
     */
    public double[][] getDoubleMatrix(int n, int m) throws RuntimeException {
        double[][] matrix = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j] = getDouble();
            }
        }
        return matrix;
    }
    /**
     * Reads a matrix of integers with the given dimensions.
     *
     * @param n number of rows
     * @param m number of columns
     * @return a 2D array containing the matrix
     * @throws RuntimeException if reading fails
     */
    public int[][] getIntMatrix(int n, int m) throws RuntimeException {
        int[][] matrix = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j] = getInt();
            }
        }
        return matrix;
    }

    /**
     * Reads a full line of integers.
     *
     * @return an array of integers representing the line
     * @throws RuntimeException if reading fails
     */
    public Integer[] getIntLine() throws RuntimeException {
        if (!tokenizer.hasMoreTokens()) {
            try {
                String line;
                do {
                    line = in.readLine();
                    if (line == null) {
                        //System.out.println("No more line to read");
                        throw new RuntimeException("End of file");
                    }
                    tokenizer = new StringTokenizer(line);
                } while (!tokenizer.hasMoreTokens());

            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }
        Integer[] res = new Integer[tokenizer.countTokens()];
        for (int i = 0; i < res.length; i++) {
            res[i] = Integer.parseInt(tokenizer.nextToken());
        }
        return res;
    }
    /**
     * Skips the next line in the input.
     *
     * @throws RuntimeException if end of file is reached or reading fails
     */
    public void skipLine() throws RuntimeException {
        try {
            String line;
            do {
                line = in.readLine();
                if (line == null) {
                    //System.out.println("No more line to read");
                    throw new RuntimeException("End of file");
                }
                tokenizer = new StringTokenizer(line);
            } while (!tokenizer.hasMoreTokens());

        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }
    /**
     * Reads the next token as a string.
     *
     * @return the next token
     * @throws RuntimeException if end of file is reached
     */
    public String getString() throws RuntimeException {
        if (!tokenizer.hasMoreTokens()) {
            try {
                String line;
                do {
                    line = in.readLine();
                    if (line == null) {
                        //System.out.println("No more line to read");
                        throw new RuntimeException("End of file");
                    }
                    tokenizer = new StringTokenizer(line);
                } while (!tokenizer.hasMoreTokens());

            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }
        return tokenizer.nextToken();
    }

}
