package org.ddolib.examples.pdp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.Math.max;
/**
 * Utility class for generating instances of the <b>Pickup and Delivery Problem (PDP)</b>
 * with a single vehicle.
 * <p>
 * This generator creates a TSP-like problem where nodes are grouped into pickup-delivery pairs.
 * In each pair, the pickup node must be visited before its associated delivery node.
 * Additionally, the problem can include "unrelated nodes" that are not part of any pickup-delivery pair.
 * </p>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *     <li>Generates random coordinates for all nodes and computes Euclidean distances between them.</li>
 *     <li>Automatically creates pickup-delivery pairs based on the number of unrelated nodes.</li>
 *     <li>Supports defining a vehicle capacity for the PDP instance.</li>
 *     <li>Can write generated instances to a file in a human-readable format.</li>
 * </ul>
 *
 * @see PDPProblem
 */
public class PDPGenerator {
    /**
     * Generates a random PDP instance with the given parameters.
     * <p>
     * Nodes are grouped into pickup-delivery pairs. Any remaining nodes are treated as unrelated nodes.
     * The distance between nodes is computed using Euclidean distance.
     * </p>
     *
     * @param n         the total number of nodes in the PDP instance
     * @param unrelated the number of nodes that are not part of any pickup-delivery pair
     *                  (there may be one more unrelated node than specified)
     * @param maxCapa   the maximum capacity of the vehicle
     * @param random    a {@link Random} object used for generating coordinates
     * @return a {@link PDPProblem} instance representing the generated PDP
     */
    public static PDPProblem genInstance(int n, int unrelated, int maxCapa, Random random) {

        int[] x = new int[n];
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            x[i] = random.nextInt(100);
            y[i] = random.nextInt(100);
        }

        double[][] distance = new double[n][];
        for (int i = 0; i < n; i++) {
            distance[i] = new double[n];
            for (int j = 0; j < n; j++) {
                distance[i][j] = dist(x[i] - x[j], y[i] - y[j]);
            }
        }

        HashMap<Integer, Integer> pickupToAssociatedDelivery = new HashMap<>();

        int numberOfPairs = Math.floorDiv(n - max(1, unrelated), 2);
        int firstDelivery = numberOfPairs + 1;
        for (int p = 1; p < firstDelivery; p++) {
            int d = firstDelivery + p - 1;
            pickupToAssociatedDelivery.put(p, d);
        }

        return new PDPProblem(distance, pickupToAssociatedDelivery, maxCapa);
    }
    /**
     * Computes the Euclidean distance between two points.
     *
     * @param dx the difference in x-coordinates
     * @param dy the difference in y-coordinates
     * @return the Euclidean distance as an integer
     */
    public static int dist(int dx, int dy) {
        return (int) Math.sqrt(dx * dx + dy * dy);
    }
    /**
     * Generates a PDP instance and writes it to a file in a human-readable format.
     * <p>
     * The file includes:
     * </p>
     * <ul>
     *     <li>The total number of nodes.</li>
     *     <li>The distance matrix between all nodes.</li>
     *     <li>The mapping of pickup nodes to their associated delivery nodes.</li>
     * </ul>
     *
     * @param fileName  the path to the output file
     * @param n         the total number of nodes in the PDP instance
     * @param unrelated the number of nodes not involved in any pickup-delivery pair
     * @param maxCapa   the maximum vehicle capacity
     * @param random    a {@link Random} object used for generating coordinates
     * @throws IOException if an I/O error occurs while writing the file
     */
    public void writeInstance(String fileName, int n, int unrelated, int maxCapa, Random random) throws IOException {

        PDPProblem problem = genInstance(n, unrelated, maxCapa, random);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(String.format("Nodes: %d%n%n", n));

            String matrixStr = Arrays.stream(problem.distanceMatrix).map(row -> Arrays.stream(row)
                            .mapToObj(x -> String.format("%3s", x))
                            .collect(Collectors.joining(" ")))
                    .collect(Collectors.joining("\n"));
            bw.write(matrixStr);
            bw.write("\n\n");

            for (Map.Entry<Integer, Integer> entry : problem.pickupToAssociatedDelivery.entrySet()) {
                bw.write(String.format("%d -> %d%n", entry.getKey(), entry.getValue()));
            }
        }
    }
}
