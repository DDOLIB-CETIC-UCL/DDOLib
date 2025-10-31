package org.ddolib.examples.pdp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a Pickup and Delivery Problem (PDP) instance with a single vehicle.
 * <p>
 * In this problem:
 * </p>
 * <ul>
 *     <li>Nodes may be grouped into pickup-delivery pairs, where a pickup node must be visited before its associated delivery node.</li>
 *     <li>There may also be unrelated nodes that are not part of any pair.</li>
 *     <li>The vehicle has a capacity limit that restricts how many pickups can be carried simultaneously.</li>
 *     <li>The problem is represented as a TSP-like graph with distances between nodes.</li>
 * </ul>
 *
 * <p>The class implements the {@link Problem} interface, providing methods for:</p>
 * <ul>
 *     <li>Number of variables {@link #nbVars()}</li>
 *     <li>Initial state {@link #initialState()}</li>
 *     <li>Transition function {@link #transition(PDPState, Decision)}</li>
 *     <li>Transition cost {@link #transitionCost(PDPState, Decision)}</li>
 *     <li>Domain of possible decisions {@link #domain(PDPState, int)}</li>
 * </ul>
 *
 *
 * <p>States are represented by {@link PDPState}, including:</p>
 * <ul>
 *     <li>The set of currently visited nodes</li>
 *     <li>The set of nodes still to visit</li>
 *     <li>The current vehicle load</li>
 * </ul>
 */
public class PDPProblem implements Problem<PDPState> {
    /**
     * Number of nodes in the problem.
     */
    public int n;

    /**
     * Distance matrix between all nodes.
     */
    public final double[][] distanceMatrix;

    /**
     * Maximum capacity of the vehicle.
     */
    public final int maxCapa;

    /**
     * Map of pickup nodes to their associated delivery nodes.
     */
    public HashMap<Integer, Integer> pickupToAssociatedDelivery;

    /**
     * Map of delivery nodes to their associated pickup nodes.
     */
    HashMap<Integer, Integer> deliveryToAssociatedPickup;

    /**
     * Set of nodes that are not part of any pickup-delivery pair.
     */
    public Set<Integer> unrelatedNodes;

    /**
     * Optional known optimal value of the problem.
     */
    private Optional<Double> optimal;

    /**
     * Optional name of the instance to ease readability in tests.
     */
    private Optional<String> name = Optional.empty();

    /**
     * Constructs a PDPProblem from a distance matrix, a map of pickup-delivery pairs, and a maximum vehicle capacity.
     *
     * @param distanceMatrix             distance matrix between all nodes
     * @param pickupToAssociatedDelivery mapping from pickup nodes to delivery nodes
     * @param maxCapa                    maximum capacity of the vehicle
     */

    public PDPProblem(final double[][] distanceMatrix,
                      HashMap<Integer, Integer> pickupToAssociatedDelivery, int maxCapa) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
        this.maxCapa = maxCapa;

        this.pickupToAssociatedDelivery = pickupToAssociatedDelivery;
        this.unrelatedNodes = new HashSet<>(IntStream.range(0, n).boxed().toList());

        deliveryToAssociatedPickup = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : pickupToAssociatedDelivery.entrySet()) {
            int p = entry.getKey();
            int d = entry.getValue();
            unrelatedNodes.remove(p);
            unrelatedNodes.remove(d);
            deliveryToAssociatedPickup.put(d, p);
        }
    }

    /**
     * Constructs a PDPProblem by reading an instance from a file.
     * <p>
     * The file format should include:
     * </p>
     * <ul>
     *     <li>The number of nodes and optionally the optimal value.</li>
     *     <li>The distance matrix between nodes.</li>
     *     <li>The pickup-delivery pairs.</li>
     * </ul>
     *
     * @param fname path to the instance file
     * @throws IOException if the file cannot be read
     */
    public PDPProblem(String fname) throws IOException {
        int numNodes;
        double[][] matrix = new double[0][0];
        Optional<Double> opti = Optional.empty();
        HashMap<Integer, Integer> pickupToAssociatedDelivery = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
            int linesCount = 0;
            int skip = 0;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    skip++;
                } else if (linesCount == 0) { // read num nodes and optimal value
                    String[] tokens = line.split("\\s+");
                    numNodes = Integer.parseInt(tokens[1]);
                    matrix = new double[numNodes][numNodes];
                    if (tokens.length >= 4) {
                        opti = Optional.of(Double.parseDouble(tokens[3]));
                    }
                } else if (skip == 1) { // read distance matrix
                    int node = linesCount - skip - 1;
                    String[] tokens = line.split("\\s+");
                    double[] row =
                            Arrays.stream(tokens).filter(s -> !s.isEmpty()).mapToDouble(Double::parseDouble).toArray();
                    matrix[node] = row;
                } else { // read pick-up and delivery pairs
                    String[] tokens = line.split(" -> ");
                    pickupToAssociatedDelivery.put(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
                }

                linesCount++;
            }
        }

        this.distanceMatrix = matrix;
        this.pickupToAssociatedDelivery = pickupToAssociatedDelivery;

        this.n = distanceMatrix.length;
        this.optimal = opti;

        this.unrelatedNodes = new HashSet<>(IntStream.range(0, n).boxed().toList());
        deliveryToAssociatedPickup = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : pickupToAssociatedDelivery.entrySet()) {
            int p = entry.getKey();
            int d = entry.getValue();
            unrelatedNodes.remove(p);
            unrelatedNodes.remove(d);
            deliveryToAssociatedPickup.put(d, p);
        }

        this.maxCapa = Integer.MAX_VALUE;

        this.name = Optional.of(fname);
    }

    /**
     * Returns the number of variables (decisions) in the problem.
     * <p>
     * Note: the last decision corresponds to returning to the depot (node 0).
     * </p>
     *
     * @return number of variables
     */
    @Override
    public int nbVars() {
        return n; //the last decision will be to come back to point zero
    }

    /**
     * Returns the initial state of the problem.
     *
     * @return initial {@link PDPState} with vehicle at the depot and all other nodes unvisited
     */
    @Override
    public PDPState initialState() {
        BitSet openToVisit = new BitSet(n);
        openToVisit.set(1, n);

        for (int p : pickupToAssociatedDelivery.keySet()) {
            openToVisit.clear(pickupToAssociatedDelivery.get(p));
        }

        BitSet allToVisit = new BitSet(n);
        allToVisit.set(1, n);

        return new PDPState(singleton(0), openToVisit, allToVisit, 0, 0);
    }

    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(n);
        toReturn.set(singletonValue);
        return toReturn;
    }

    /**
     * Returns the initial value of the problem.
     *
     * @return 0
     */
    @Override
    public double initialValue() {
        return 0;
    }

    /**
     * Returns the domain of possible decisions (nodes to visit) from a given state and variable index.
     *
     * @param state current {@link PDPState}
     * @param var   index of the decision variable
     * @return iterator over possible node indices for the decision
     */
    @Override
    public Iterator<Integer> domain(PDPState state, int var) {
        if (var == n - 1) {
            //the final decision is to come back to node zero
            return singleton(0).stream().iterator();
        } else {

            boolean canIncludePickups = state.minContent < maxCapa;
            boolean canIncludeDeliveries = state.maxContent != 0;

            return state
                    .openToVisit
                    .stream()
                    .filter(point ->
                            ((canIncludePickups | !pickupToAssociatedDelivery.containsKey(point))
                                    && (canIncludeDeliveries | !deliveryToAssociatedPickup.containsKey(point))))
                    .boxed()
                    .iterator();
        }
    }

    /**
     * Computes the next state given a current state and a decision.
     *
     * @param state    current {@link PDPState}
     * @param decision the {@link Decision} made
     * @return new {@link PDPState} after applying the decision
     */
    @Override
    public PDPState transition(PDPState state, Decision decision) {
        int node = decision.val();
        BitSet newOpenToVisit = (BitSet) state.openToVisit.clone();
        newOpenToVisit.clear(node);

        BitSet newAllToVisit = (BitSet) state.allToVisit.clone();
        newAllToVisit.clear(node);
        int newMinContent = state.minContent;
        int newMaxContent = state.maxContent;
        if (pickupToAssociatedDelivery.containsKey(node)) {
            newOpenToVisit.set(pickupToAssociatedDelivery.get(node));
            newMinContent += 1;
            newMaxContent += 1;
        }

        if (deliveryToAssociatedPickup.containsKey(node)) {
            int p = deliveryToAssociatedPickup.get(node);
            if (newOpenToVisit.get(p)) {
                newOpenToVisit.clear(p);
            }
            newMinContent -= 1;
            newMaxContent -= 1;
        }

        if (newMinContent < 0) newMinContent = 0;
        if (newMaxContent > maxCapa) newMaxContent = maxCapa;
        return new PDPState(
                state.singleton(node),
                newOpenToVisit,
                newAllToVisit,
                newMinContent,
                newMaxContent);
    }

    /**
     * Computes the cost of transitioning from a state via a decision.
     * <p>
     * Typically corresponds to the travel distance from the current node to the chosen node.
     * </p>
     *
     * @param state    current {@link PDPState}
     * @param decision the {@link Decision} made
     * @return cost of the transition
     */
    @Override
    public double transitionCost(PDPState state, Decision decision) {
        return state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                .mapToDouble(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsDouble();
    }

    /**
     * Returns a string representation of the PDP instance.
     *
     * @return string describing the PDP instance
     */
    @Override
    public String toString() {
        String str = "PDP(\n\tn:" + n + "\n" +
                "\tpdp:" + pickupToAssociatedDelivery.keySet().stream().map(p -> p + "->" + pickupToAssociatedDelivery.get(p)).toList() + "\n" +
                "\tunrelated:" + unrelatedNodes.stream().toList() + "\n" +
                "\t" + Arrays.stream(distanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList();

        return name.orElse(str);
    }

    /**
     * Returns the known optimal value of the problem, if available.
     *
     * @return optional optimal value
     */
    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    /**
     * Evaluates a solution represented as an array of node indices.
     *
     * @param solution array of node indices representing the route
     * @return total distance of the solution, or -1 if it violates vehicle capacity
     */
    public double eval(int[] solution) {
        int vehicleContent = 0;
        double toReturn = 0;
        for (int i = 1; i < solution.length; i++) {
            toReturn = toReturn + distanceMatrix[solution[i - 1]][solution[i]];
            if (pickupToAssociatedDelivery.containsKey(solution[i])) {
                vehicleContent += 1;
            } else if (deliveryToAssociatedPickup.containsKey(solution[i])) {
                vehicleContent -= 1;
            }
            if (vehicleContent > maxCapa) {
                return -1;
            }
        }
        toReturn = toReturn + distanceMatrix[solution[solution.length - 1]][0]; //final come back
        return toReturn;
    }


    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not match " +
                    "the number %d variables", Arrays.toString(solution), nbVars()));
        }

        Map<Integer, Long> count = Arrays.stream(solution)
                .boxed()
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        if (count.values().stream().anyMatch(x -> x != 1)) {
            String msg = "The solution has duplicated nodes and does not reache each node";
            throw new InvalidSolutionException(msg);
        }


        int[] posInRoute = new int[nbVars()];
        for (int i = 0; i < nbVars(); i++) {
            int node = solution[i];
            posInRoute[node] = i;
        }

        for (int node : solution) {
            if (pickupToAssociatedDelivery.containsKey(node)) {
                int delivery = pickupToAssociatedDelivery.get(node);
                int pickUpPos = posInRoute[node];
                int deliveryPos = posInRoute[delivery];
                if (pickUpPos >= deliveryPos) {
                    String msg = String.format("The precedance constraint %d -> %d is not " +
                            "respected in %s", node, delivery, Arrays.toString(solution));
                    throw new InvalidSolutionException(msg);
                }

            }
        }

        double value = distanceMatrix[0][solution[0]]; //Start from the depot.
        int vehicleContent = 0;
        if (pickupToAssociatedDelivery.containsKey(solution[0])) vehicleContent++;
        else if (deliveryToAssociatedPickup.containsKey(solution[0])) vehicleContent--;
        for (int i = 1; i < nbVars(); i++) {
            value += distanceMatrix[solution[i - 1]][solution[i]];
            if (pickupToAssociatedDelivery.containsKey(solution[i])) vehicleContent++;
            else if (deliveryToAssociatedPickup.containsKey(solution[i])) vehicleContent--;
            if (vehicleContent > maxCapa) {
                String msg = String.format("The capacity of %s (%d) exceeds the capacity " +
                        "of the vehicle (%d)", Arrays.toString(solution), vehicleContent, maxCapa);
                throw new InvalidSolutionException(msg);
            } else if (vehicleContent < 0) {
                String msg = String.format("The capacity of %s (%d) goes below 0", Arrays.toString(solution), vehicleContent);
                throw new InvalidSolutionException(msg);
            }
        }


        return value;
    }
}
