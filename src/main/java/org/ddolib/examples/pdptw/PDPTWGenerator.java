package org.ddolib.examples.pdptw;

import org.ddolib.examples.pdp.PDPProblem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;

/**
 * Utility class for generating instances of the <b>Pickup and Delivery Problem with time window (PDPTW)</b>
 * with a single vehicle.
 * <p>
 * This generator creates a TSP-like problem where nodes are grouped into pickup-delivery pairs.
 * In each pair, the pickup node must be visited before its associated delivery node.
 * Additionally, the problem can include "unrelated nodes" that are not part of any pickup-delivery pair.
 * Nodes also have an associated time window. If the vehicle comes too early, it has to wait. If the vehicle comes too late it is a violation.
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
 * @see PDPTWProblem
 */
public class PDPTWGenerator {

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
    public static PDPTWProblem genInstance(int n, int unrelated, int maxCapa, Random random, Boolean strengthenTimeWindows) {

        int squareSide = 1000;
        int[] x = new int[n];
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            x[i] = random.nextInt(squareSide);
            y[i] = random.nextInt(squareSide);
        }

        double[][] timeMatrix = new double[n][];
        for (int i = 0; i < n; i++) {
            timeMatrix[i] = new double[n];
            for (int j = 0; j < n; j++) {
                timeMatrix[i][j] = dist(x[i] - x[j], y[i] - y[j]);
            }
        }

        //generate a solution; based on random sort

        List<Integer> solution = new ArrayList<>();
        for (int i = 1; i <= n - 1; i++) {
            //solution does not include zero
            solution.add(i);
        }
        Collections.shuffle(solution, random);


        HashMap<Integer, Integer> pickupToAssociatedDelivery = new HashMap<>();
        HashMap<Integer, Integer> deliveryToAssociatedPickup = new HashMap<>();
        HashSet<Integer> unrelatedNodes = new HashSet<Integer>();
        unrelatedNodes.add(0);
        HashSet<Integer> openPickups = new HashSet<Integer>();

        int numberOfPairs = Math.floorDiv(n - max(1, unrelated), 2);
        int nbUnrelated = n - 2 * numberOfPairs;

        TimeWindow[] timeWindows = new TimeWindow[n];
        double currentTime = 0;  //startTime is  0; also earlyLine for node0
        int currentNode = 0;
        int currentContent = 0;

        int numberOfNodesToAssign = n;
        for (int nextNode : solution) {
            numberOfNodesToAssign -= 1;
            double arrivalTime = currentTime + timeMatrix[currentNode][nextNode];
            double earlyLine = arrivalTime - (squareSide / 2) + random.nextDouble(squareSide);
            if (earlyLine < 0) {
                earlyLine = 0;
            }
            currentTime = new TimeWindow(earlyLine, 0).entryTime(arrivalTime);
            double deadline = currentTime + random.nextInt(squareSide * 2);
            timeWindows[nextNode] = new TimeWindow(earlyLine, deadline);
            currentNode = nextNode;

            //what do we do with this node?
            //if capa is full, it is either a delivery or an unrelated node
            //otherwise, it is either a pickup, a delivery or an unrelated node
            int nbNodesForUnrelated = nbUnrelated - unrelatedNodes.size();

            //can it be a delivery? yes if there are openPickups
            int nbNodesForDelivery = openPickups.size();
            //can it be a pickup? yes if
            int nbNodesForPickup;
            if (currentContent == maxCapa) {
                nbNodesForPickup = 0;
            } else {
                nbNodesForPickup = numberOfNodesToAssign - nbNodesForUnrelated - nbNodesForDelivery;
            }

            //random draw
            switch (biasedRandom(random, new int[]{nbNodesForUnrelated, nbNodesForPickup, nbNodesForDelivery})) {
                case 0: //unrelated
                    if (nbNodesForUnrelated == 0) throw new Error("A");
                    unrelatedNodes.add(currentNode);

                    break;
                case 1: //pickup
                    if (nbNodesForPickup == 0) throw new Error("B");
                    openPickups.add(currentNode);
                    currentContent += 1;
                    break;
                case 2: //delivery
                    if (nbNodesForDelivery == 0) throw new Error("C");
                    //get a pickup point
                    int pickup = (int) openPickups.toArray()[random.nextInt(openPickups.size())];

                    currentContent -= 1;
                    pickupToAssociatedDelivery.put(pickup, currentNode);
                    deliveryToAssociatedPickup.put(currentNode, pickup);
                    openPickups.remove(pickup);

                    //we delete one of the two timeWindows, to make the problem more challenging
                    switch (random.nextInt(2)) {
                        case 0:
                            timeWindows[pickup] = new TimeWindow(0, Integer.MAX_VALUE);
                            break;
                        case 1:
                            timeWindows[currentNode] = new TimeWindow(0, Integer.MAX_VALUE);
                            break;
                        case 2:
                            timeWindows[pickup] = new TimeWindow(0, Integer.MAX_VALUE);
                            timeWindows[currentNode] = new TimeWindow(0, Integer.MAX_VALUE);
                            break;
                    }
            }
        }

        double arrivalTime = currentTime + timeMatrix[currentNode][0];
        double deadline = arrivalTime + random.nextInt(100);
        timeWindows[0] = new TimeWindow(0, deadline);

        return new PDPTWProblem(timeMatrix, pickupToAssociatedDelivery, maxCapa, timeWindows, Optional.of(arrivalTime), strengthenTimeWindows);
    }

    static double dist(int dx, int dy) {
        //we take floor to ensure that the matrix respects the triangular inequality
        return Math.floor(Math.sqrt(dx * dx + dy * dy));
    }

    private static int biasedRandom(Random random, int[] valuesAndBias) {
        int summedBias = Arrays.stream(valuesAndBias).sum();
        int draw = random.nextInt(summedBias);
        //draw < summedBias
        for (int i = 0; i < valuesAndBias.length; i++) {
            draw = draw - valuesAndBias[i];
            if (draw <= 0 && valuesAndBias[i] != 0) {
                return i;
            }
        }
        //if we get there, there has been a problem
        throw new Error("error in random");
    }

    /**
     * Generates a PDPTW instance.
     * This method explicitly builds a valid optimal route (respecting capacity and precedence)
     * before computing time windows, ensuring the optimal solution is exact and known.
     *
     * @param n                     Total number of nodes
     * @param unrelated             Number of unrelated nodes
     * @param maxCapa               Maximum capacity of the vehicle
     * @param random                Random object for reproducibility
     * @param strengthenTimeWindows Option to strengthen time windows
     * @return The PDPTW problem instance
     */
    public static PDPTWProblem constructInstanceWithSolution(int n, int unrelated, int maxCapa, Random random, Boolean strengthenTimeWindows) {

        // 1. Initialize coordinates and time matrix
        int squareSide = 1000;
        int[] x = new int[n];
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            x[i] = random.nextInt(squareSide);
            y[i] = random.nextInt(squareSide);
        }

        double[][] timeMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                timeMatrix[i][j] = dist(x[i] - x[j], y[i] - y[j]);
            }
        }

        // 2. Determine node roles
        int numberOfPairs = Math.max(0, (n - 1 - unrelated) / 2);
        int actualUnrelated = n - 1 - (numberOfPairs * 2);

        List<Integer> availableNodesIds = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            availableNodesIds.add(i);
        }
        Collections.shuffle(availableNodesIds, random);

        HashMap<Integer, Integer> pickupToDelivery = new HashMap<>();
        List<Integer> unrelatedNodesList = new ArrayList<>();
        List<Integer> allPickups = new ArrayList<>();

        int index = 0;

        // Assign unrelated nodes
        for (int i = 0; i < actualUnrelated; i++) {
            unrelatedNodesList.add(availableNodesIds.get(index++));
        }

        // Assign pickup and delivery pairs
        for (int i = 0; i < numberOfPairs; i++) {
            int pickup = availableNodesIds.get(index++);
            int delivery = availableNodesIds.get(index++);
            pickupToDelivery.put(pickup, delivery);
            allPickups.add(pickup);
        }

        // 3. Iteratively build a valid optimal route
        List<Integer> optimalRoute = new ArrayList<>();

        // Nodes that are allowed to be visited at current time T
        List<Integer> visitableNodes = new ArrayList<>(unrelatedNodesList);
        visitableNodes.addAll(allPickups); // Initially, we can visit unrelated nodes and pickups

        int currentLoad = 0;

        while (!visitableNodes.isEmpty()) {
            List<Integer> selectableNodes = new ArrayList<>();

            // Filter visitable nodes to respect max capacity
            for (int node : visitableNodes) {
                if (allPickups.contains(node)) {
                    if (currentLoad < maxCapa) {
                        selectableNodes.add(node); // Can only pick up if there is space
                    }
                } else {
                    selectableNodes.add(node); // Deliveries and unrelated nodes are always selectable
                }
            }

            // Randomly select the next valid node
            int chosenNode = selectableNodes.get(random.nextInt(selectableNodes.size()));
            optimalRoute.add(chosenNode);
            visitableNodes.remove(Integer.valueOf(chosenNode));

            // Update vehicle load and visitable nodes
            if (allPickups.contains(chosenNode)) {
                currentLoad++;
                // The associated delivery becomes visitable
                visitableNodes.add(pickupToDelivery.get(chosenNode));
            } else if (pickupToDelivery.containsValue(chosenNode)) {
                currentLoad--; // Unloaded a requested item
            }
        }

        // 4. Calculate exact timing on this perfect route to set time windows
        TimeWindow[] timeWindows = new TimeWindow[n];
        double currentTime = 0; // Departure from depot at t=0
        int currentNode = 0;

        for (int nextNode : optimalRoute) {
            double arrivalTime = currentTime + timeMatrix[currentNode][nextNode];

            // Create a random tolerance margin (e.g., between 20 and 100 time units)
            double margin = 20 + random.nextInt(80);

            // earlyLine = time before which the vehicle must wait
            double earlyLine = Math.max(0, arrivalTime - random.nextInt((int) margin));
            // deadline = time after which the solution becomes invalid
            double deadline = arrivalTime + random.nextInt((int) margin);

            timeWindows[nextNode] = new TimeWindow(earlyLine, deadline);

            // Vehicle departs: if it arrived too early, it waited until earlyLine
            currentTime = Math.max(arrivalTime, earlyLine);
            currentNode = nextNode;
        }

        // 5. Return to the depot
        double finalArrivalTime = currentTime + timeMatrix[currentNode][0];
        double depotDeadline = finalArrivalTime + random.nextInt(100);
        timeWindows[0] = new TimeWindow(0, depotDeadline);

        // Return the problem instance with the guaranteed valid optimal value
        return new PDPTWProblem(timeMatrix, pickupToDelivery, maxCapa, timeWindows, Optional.of(finalArrivalTime), strengthenTimeWindows);
    }

    /**
     * Generates a PDPTW instance and writes it to a file in a human-readable format.
     * <p>
     * The file includes:
     * </p>
     * <ul>
     *     <li>The total number of nodes.</li>
     *     <li>The distance matrix between all nodes.</li>
     *     <li>The mapping of pickup nodes to their associated delivery nodes.</li>
     *     <li>The tine window for each node.</li>
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

        PDPTWProblem problem = genInstance(n, unrelated, maxCapa, random, false);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(String.format("Nodes: %d%n%n", n));

            String matrixStr = Arrays.stream(problem.timeMatrix).map(row -> Arrays.stream(row)
                            .mapToObj(x -> String.format("%3s", x))
                            .collect(Collectors.joining(" ")))
                    .collect(Collectors.joining("\n"));
            bw.write(matrixStr);
            bw.write("\n\n");

            for (Map.Entry<Integer, Integer> entry : problem.pickupToAssociatedDelivery.entrySet()) {
                bw.write(String.format("%d -> %d%n", entry.getKey(), entry.getValue()));
            }
            bw.write("\n\n");
            for (int node = 0; node < n; node++) {
                bw.write(String.format("%n : [%d;%e]", node, problem.timeWindows[node].start(), problem.timeWindows[node].end()));
            }
            bw.write("\n\n");
        }
    }
}
