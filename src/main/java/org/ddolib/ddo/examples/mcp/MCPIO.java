package org.ddolib.ddo.examples.mcp;

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public class MCPIO {

    private static int[][] generateAdjacencyMatrix(int numNodes, int adjacentProba, long seed) {
        int[][] matrix = new int[numNodes][numNodes];
        Random rng = new Random(seed);
        for (int i = 0; i < numNodes - 1; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                int adjacent = rng.nextInt(adjacentProba);
                int w = adjacent == 0 ? 0 : 1 + rng.ints(-10, 10)
                        .filter(x -> x != 0).findFirst().getAsInt();
                matrix[i][j] = w;
                matrix[j][i] = w;
            }
        }
        return matrix;
    }

    public static void writeInstance(String fileName, int numNodes, int adjacentProba, long seed) throws IOException {
        int[][] matrix = generateAdjacencyMatrix(numNodes, adjacentProba, seed);
        Graph graph = new Graph(matrix);
        MCPProblem problem = new MCPProblem(graph);
        System.out.println("Solver problem");
        NaiveMCPSolver solver = new NaiveMCPSolver(problem);
        solver.maximize();
        int opti = solver.best();
        System.out.printf("Solution found: %d\n", opti);
        System.out.println("Writing output file");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(String.format("Nodes: %d Opti: %d%n%n", numNodes, opti));
            String matrixStr = Arrays.stream(matrix).map(row -> Arrays.stream(row)
                            .mapToObj(x -> String.format("%3s", x))
                            .collect(Collectors.joining(" ")))
                    .collect(Collectors.joining("\n"));
            bw.write(matrixStr);
        }
    }

    public static void writeInstance(String fileName, int numNodes, int adjacentProba) throws IOException {
        Random rng = new Random();
        writeInstance(fileName, numNodes, adjacentProba, rng.nextLong());
    }

    public static MCPProblem readInstance(String fileName) throws IOException {
        int[][] matrix = new int[0][0];

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            int linesCount = 0;
            int skip = 0;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    skip++;
                } else if (linesCount == 0) {
                    String[] tokens = line.split("\\s+");
                    int n = Integer.parseInt(tokens[1]);
                    matrix = new int[n][n];
                } else {
                    int node = linesCount - skip - 1;
                    String[] tokens = line.split("\\s+");
                    int[] row = Arrays.stream(tokens).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();
                    matrix[node] = row;
                }
                linesCount++;
            }
        }
        Graph g = new Graph(matrix);
        return new MCPProblem(g);
    }
}
