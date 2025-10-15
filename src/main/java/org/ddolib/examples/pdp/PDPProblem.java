package org.ddolib.examples.pdp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class PDPProblem implements Problem<PDPState> {
    public int n;


    public final double[][] distanceMatrix;
    public final int maxCapa;

    public HashMap<Integer, Integer> pickupToAssociatedDelivery;
    HashMap<Integer, Integer> deliveryToAssociatedPickup;

    public Set<Integer> unrelatedNodes;

    private Optional<Double> optimal;

    /**
     * A name to ease the readability of the tests.
     */
    private Optional<String> name = Optional.empty();

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


    @Override
    public int nbVars() {
        return n; //the last decision will be to come back to point zero
    }

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

    @Override
    public double initialValue() {
        return 0;
    }

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

    @Override
    public double transitionCost(PDPState state, Decision decision) {
        return state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                .mapToDouble(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsDouble();
    }

    @Override
    public String toString() {
        String str = "PDP(\n\tn:" + n + "\n" +
                "\tpdp:" + pickupToAssociatedDelivery.keySet().stream().map(p -> p + "->" + pickupToAssociatedDelivery.get(p)).toList() + "\n" +
                "\tunrelated:" + unrelatedNodes.stream().toList() + "\n" +
                "\t" + Arrays.stream(distanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList();

        return name.orElse(str);
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }


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

    public double eval(List<Integer> solution) {
        double toReturn = 0;
        for (int i = 1; i < solution.size(); i++) {
            toReturn += distanceMatrix[solution.get(i - 1)][solution.get(i)];
        }
        toReturn += distanceMatrix[solution.getLast()][0];
        return toReturn;

    }
}
