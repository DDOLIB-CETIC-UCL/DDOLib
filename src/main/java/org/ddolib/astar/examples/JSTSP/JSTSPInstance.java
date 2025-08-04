package org.ddolib.astar.examples.JSTSP;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import static java.lang.Math.max;

public class JSTSPInstance {
    int n; // number of jobs
    int m; // number of tools

    int c; // number of tools that can be carried at once
    List<List<Boolean>> matrix; // required tools for each job (n x m)
    long [] jobs;
    List<Integer> [] tools;

    public JSTSPInstance(int n, int m, List<List<Boolean>> matrix, int c) {
        this.n = n;
        this.m = m;
        this.matrix = matrix;
        this.c = c;

        jobs = new long[n];
        tools = new List[n];

        for (int i = 0; i < n; i++) {
            jobs [i] = 0L;
            tools[i] = new ArrayList<>();
            for (int j = 0; j < m; j++) {
                if (matrix.get(i).get(j)) {
                    jobs[i] |= 1L << j;
                    tools[i].add(j);
                }
            }
        }
    }

    public static JSTSPInstance readFile(String filename, String problemId) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(filename));
        } catch (IOException e) {
            return null;// Convert error to null
        }

        int n = 0, m = 0, min = 0, max = 0, c = 0;
        List<List<Boolean>> matrix = new ArrayList<>();
        String selectedProblem = problemId; // Adjust as necessary
        boolean parsingData = false;

        for (String line : lines) {
            if (line.contains("=")) {
                String[] parts = line.split("=");
                switch (parts[0].trim()) {
                    case "n":
                        n = Integer.parseInt(parts[1].trim());
                        break;
                    case "m":
                        m = Integer.parseInt(parts[1].trim());
                        break;
                    case "min":
                        min = Integer.parseInt(parts[1].trim());
                        break;
                    case "max":
                        max = Integer.parseInt(parts[1].trim());
                        break;
                    case "c":
                        c = Integer.parseInt(parts[1].trim());
                        break;
                    default:
                        break;
                }
            } else if (line.contains(selectedProblem)) {
                parsingData = true;
            } else if (line.contains("problem") && !line.contains(selectedProblem)) {
                parsingData = false;
            } else if (parsingData && !line.contains("-") && !line.trim().isEmpty()) {
                List<Boolean> row = parseLine(line);
                if (row != null) {
                    matrix.add(row);
                    if (matrix.size() == m) {
                        break;
                    }
                }
            }
        }

        // Transpose the matrix if necessary
        List<List<Boolean>> transposedMatrix = new ArrayList<>();
        for (int i = 0; i < matrix.get(0).size(); i++) {
            List<Boolean> row = new ArrayList<>();
            for (List<Boolean> col : matrix) {
                row.add(col.get(i));
            }
            transposedMatrix.add(row);
        }

        return introduceDummy(new JSTSPInstance(n, m, transposedMatrix, c));
    }

    private static List<Boolean> parseLine(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0 || parts[0].length() > 1) {
            return null;
        }
        List<Boolean> result = new ArrayList<>();
        for (String part : parts) {
            switch (part) {
                case "0":
                    result.add(false);
                    break;
                case "1":
                    result.add(true);
                    break;
                default:
                    return null;
            }
        }
        return result;
    }
    private static JSTSPInstance introduceDummy(JSTSPInstance data) {
        List<List<Boolean>> matrix = new LinkedList<>();
        List<Boolean> tools = new LinkedList<>();
        for (int i = 0; i < data.m; i++) {
            tools.add(false);
        }
        matrix.add(tools);
        for (int i = 0; i < data.n; i++) {
            matrix.add(data.matrix.get(i));
        }
        return new JSTSPInstance(data.n + 1, data.m, matrix, data.c);
    }

    public double[][] getMin_cost() {
        double[][] min_cost = new double[this.n][this.n];
        for (int i = 0; i < this.n; i++) {

            for (int j = 0; j < this.n; j++) {
                if (i==j){
                    min_cost[i][j] = Double.MAX_VALUE;
                }else {
                    long union = this.jobs[i] | this.jobs[j];
                    int size_union = 0;
                    for (int k = 0; k < this.m; k++) {
                        if ((union & (1L << k)) == (1L << k)) {
                            size_union++;
                        }
                    }
                    min_cost[i][j] = max(size_union-this.c, 0);
                }
            }
        }
        return min_cost;
    }
}
