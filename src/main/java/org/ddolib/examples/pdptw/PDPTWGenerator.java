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
    public static PDPTWProblem genInstance(int n, int unrelated, int maxCapa, Random random) {

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
        for (int i = 1; i <= n-1; i++) {
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
        int nbUnrelated = n - 2*numberOfPairs;

        TimeWindow[] timeWindows = new TimeWindow[n];
        double currentTime = 0;  //startTime is  0; also earlyLine for node0
        int currentNode = 0;
        int currentContent = 0;

        int numberOfNodesToAssign = n;
        for(int nextNode : solution){
            numberOfNodesToAssign -= 1;
            double arrivalTime = currentTime + timeMatrix[currentNode][nextNode];
            double earlyLine = arrivalTime - (squareSide/2) + random.nextDouble(squareSide);
            if(earlyLine < 0) {
                earlyLine = 0;
            }
            currentTime  = new TimeWindow(earlyLine, 0).entryTime(arrivalTime);
            double deadline = currentTime + random.nextInt(squareSide/2);
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
            if (currentContent == maxCapa){
                nbNodesForPickup = 0;
            }else {
                nbNodesForPickup = numberOfNodesToAssign - nbNodesForUnrelated - nbNodesForDelivery;
            }

            //random draw
            switch (biasedRandom(random,new int[]{nbNodesForUnrelated,nbNodesForPickup,nbNodesForDelivery})) {
                case 0: //unrelated
                    if(nbNodesForUnrelated ==0) throw new Error("A");
                    unrelatedNodes.add(currentNode);

                    break;
                case 1: //pickup
                    if(nbNodesForPickup ==0) throw new Error("B");
                    openPickups.add(currentNode);
                    currentContent += 1;
                    break;
                case 2: //delivery
                    if(nbNodesForDelivery == 0) throw new Error("C");
                    //get a pickup point
                    int pickup = (int) openPickups.toArray()[random.nextInt(openPickups.size())];

                    currentContent -= 1;
                    pickupToAssociatedDelivery.put(pickup,currentNode);
                    deliveryToAssociatedPickup.put(currentNode,pickup);
                    openPickups.remove(pickup);

                    //we delete one of the two timeWindows, to make the problem more challenging
                    if(random.nextBoolean()){
                        timeWindows[pickup] = new TimeWindow(0, Integer.MAX_VALUE);
                    }else{
                        timeWindows[currentNode] = new TimeWindow(0, Integer.MAX_VALUE);
                    }
            }
        }

        double arrivalTime = currentTime + timeMatrix[currentNode][0];
        double deadline = arrivalTime + random.nextInt(100);
        timeWindows[0] = new TimeWindow(0, deadline);

        PDPTWProblem instance = new PDPTWProblem(timeMatrix, pickupToAssociatedDelivery, maxCapa, timeWindows, Optional.of(arrivalTime));

        //construct the PDPTW solution object
//        int[]fullSolutionArray = new int[n+1];
//        for(int i = 0 ; i < n-1 ; i++){
//            fullSolutionArray[i+1] = solution.get(i);
//        }
//        fullSolutionArray[0]= 0;
//        fullSolutionArray[n] = 0;
//        PDPTWSolution solution2 = new PDPTWSolution(new PDPTWProblem(instance), fullSolutionArray, arrivalTime);

        return instance;
    }

    static double dist(int dx, int dy) {
        //we take floor to ensure that the matrix respects the triangular inequality
        return Math.floor(Math.sqrt(dx * dx + dy * dy));
    }

    private static int biasedRandom(Random random, int[] valuesAndBias){
        int summedBias = Arrays.stream(valuesAndBias).sum();
        int draw = random.nextInt(summedBias);
        //draw < summedBias
        for(int i = 0 ; i < valuesAndBias.length; i++){
            draw = draw - valuesAndBias[i];
            if(draw <= 0 && valuesAndBias[i] != 0){
                return i;
            }
        }
        //if we get there, there has been a problem
        throw new Error("error in random");
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

        PDPTWProblem problem = genInstance(n, unrelated, maxCapa, random);

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
            for(int node= 0 ; node < n; node ++){
                bw.write(String.format("%n : [%d;%e]",node, problem.timeWindows[node].start(),problem.timeWindows[node].end()));
            }
            bw.write("\n\n");
        }
    }
}
