package org.ddolib.examples.mcp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Contains methods to generates and write instances.
 */
public class MCPGenerator {

    /**
     * Generates a random adjacency matrix.
     *
     * @param numNodes       The number of nodes expected in the graph.
     * @param connectedProba Each pair of node has a probability of {@code 1 / connectedProba} to not be connected.
     * @param seed           The seed by the random number generator.
     * @return A random matrix generator.
     */
    private static int[][] generateRandomAdjacencyMatrix(int numNodes, int connectedProba, long seed) {
        int[][] matrix = new int[numNodes][numNodes];
        Random rng = new Random(seed);
        for (int i = 0; i < numNodes - 1; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                int adjacent = rng.nextInt(connectedProba);
                int w = adjacent == 0 ? 0 : 1 + rng.ints(-10, 10)
                        .filter(x -> x != 0).findFirst().orElse(1);
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
    public static void writeRandomInstance(String fileName, int numNodes, int connectedProba, boolean solve, long seed) throws IOException {
        int[][] matrix = generateRandomAdjacencyMatrix(numNodes, connectedProba, seed);

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
    public static void writeRandomInstance(String fileName, int numNodes, int connectedProba, boolean solve) throws IOException {
        Random rng = new Random();
        writeRandomInstance(fileName, numNodes, connectedProba, solve, rng.nextLong());
    }

}
