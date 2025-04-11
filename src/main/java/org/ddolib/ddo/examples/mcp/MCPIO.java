package org.ddolib.ddo.examples.mcp;

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public class MCPIO {

    private static int[][] generateAdjacencyMatrix(int numNodes, long seed) {
        int[][] matrix = new int[numNodes][numNodes];
        Random rng = new Random(seed);
        for (int i = 0; i < numNodes - 1; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                int adjacent = rng.nextInt(4);
                int w = adjacent == 0 ? 0 : 1 + rng.ints(-10, 10)
                        .filter(x -> x != 0).findFirst().getAsInt();
                matrix[i][j] = w;
                matrix[j][i] = w;
            }
        }
        return matrix;
    }

    public static void writeInstance(String fileName, int numNodes, long seed) throws IOException {
        int[][] matrix = generateAdjacencyMatrix(numNodes, seed);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(String.format("Nodes: %d%n%n", numNodes));
            String matrixStr = Arrays.stream(matrix).map(row -> Arrays.stream(row)
                            .mapToObj(x -> String.format("%3s", x))
                            .collect(Collectors.joining(" ")))
                    .collect(Collectors.joining("\n"));
            bw.write(matrixStr);
        }
    }

    public static void writeInstance(String fileName, int numNodes) throws IOException {
        Random rng = new Random();
        writeInstance(fileName, numNodes, rng.nextLong());
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

    public static void main(String[] args) throws IOException {
        writeInstance("data/MCP/nodes_20.txt", 20);
        MCPProblem problem = readInstance("data/MCP/nodes_20.txt");
        System.out.println(problem.graph);
        NaiveMCPSolver solver = new NaiveMCPSolver(problem);
        solver.maximize();
        System.out.println(solver.best());
        System.out.println(Arrays.toString(solver.bestSolution()));
    }
}
