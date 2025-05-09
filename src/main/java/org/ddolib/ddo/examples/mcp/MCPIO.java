package org.ddolib.ddo.examples.mcp;

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Contains methods to generates and write instances.
 */
public class MCPIO {

    /**
     * Generates a random adjacency matrix.
     *
     * @param numNodes       The number of nodes expected in the graph.
     * @param connectedProba Each pair of node has a probability of {@code 1 / connectedProba} to not be connected.
     * @param seed           The seed by the random number generator.
     * @return A random matrix generator.
     */
    private static int[][] generateAdjacencyMatrix(int numNodes, int connectedProba, long seed) {
        int[][] matrix = new int[numNodes][numNodes];
        Random rng = new Random(seed);
        for (int i = 0; i < numNodes - 1; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                int adjacent = rng.nextInt(connectedProba);
                int w = adjacent == 0 ? 0 : 1 + rng.ints(-10, 10)
                        .filter(x -> x != 0).findFirst().getAsInt();
                matrix[i][j] = w;
                matrix[j][i] = w;
            }
        }
        return matrix;
    }


    /**
     * Randomly generates and save instance of MCP into the given file.
     *
     * @param fileName       The file where saving the instance.
     * @param numNodes       The number of nodes of the associated graph.
     * @param connectedProba Each pair of node has a probability of {@code 1 / connectedProba} to not be connected.
     * @param solve          Whether the optimal solution must be computed and saved. Warning, the problem is solved naively. Be sure the
     *                       set to {@code true} only on small instances.
     * @param seed           The seed used by the random number generator.
     * @throws IOException If something goes wrong while writing files.
     */
    public static void writeInstance(String fileName, int numNodes, int connectedProba, boolean solve, long seed) throws IOException {
        int[][] matrix = generateAdjacencyMatrix(numNodes, connectedProba, seed);

        int opti = solve ? NaiveMCPSolver.getOptimalSolution(matrix) : 0;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            if (solve) {
                bw.write(String.format("Nodes: %d Opti: %d%n%n", numNodes, opti));
            } else {
                bw.write(String.format("Nodes: %d%n%n", numNodes));
            }

            String matrixStr = Arrays.stream(matrix).map(row -> Arrays.stream(row)
                            .mapToObj(x -> String.format("%3s", x))
                            .collect(Collectors.joining(" ")))
                    .collect(Collectors.joining("\n"));
            bw.write(matrixStr);
        }
    }

    /**
     * Randomly generates and save instance of MCP into the given file.
     *
     * @param fileName       The file where saving the instance.
     * @param numNodes       The number of nodes of the associated graph.
     * @param connectedProba Each pair of node has a probability of {@code 1 / connectedProba} to not be connected.
     * @param solve          Whether the optimal solution must be computed and saved. Warning, the problem is solved naively. Be sure the
     *                       set to {@code true} only on small instances.
     * @throws IOException If something goes wrong while writing files.
     */
    public static void writeInstance(String fileName, int numNodes, int connectedProba, boolean solve) throws IOException {
        Random rng = new Random();
        writeInstance(fileName, numNodes, connectedProba, solve, rng.nextLong());
    }

    /**
     * Read a file containing an instance of MCP.
     *
     * @param fileName The path to the file containing the instance
     * @return A {@link MCPProblem}
     * @throws IOException If something goes wring while reading file.
     */
    public static MCPProblem readInstance(String fileName) throws IOException {
        int[][] matrix = new int[0][0];
        int optimal = 0;

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
                    if (tokens.length >= 4) {
                        optimal = Integer.parseInt(tokens[3]);
                    }
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
        return new MCPProblem(g, optimal);
    }
}
