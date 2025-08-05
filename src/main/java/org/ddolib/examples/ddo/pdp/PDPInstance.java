package org.ddolib.examples.ddo.pdp;

import org.ddolib.util.Permutations;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class PDPInstance {

    final int n;
    final double[][] distanceMatrix;
    public final double optimal;

    HashMap<Integer, Integer> pickupToAssociatedDelivery;
    HashMap<Integer, Integer> deliveryToAssociatedPickup;

    Set<Integer> unrelatedNodes;

    @Override
    public String toString() {
        return "PDPInstance(\n\tn:" + n + "\n" +
                "\tpdp:" + pickupToAssociatedDelivery.keySet().stream().map(p -> p + "->" + pickupToAssociatedDelivery.get(p)).toList() + "\n" +
                "\tunrelated:" + unrelatedNodes.stream().toList() + "\n" +
                "\t" + Arrays.stream(distanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList();
    }

    public double eval(int[] solution) {
        double toReturn = 0;
        for (int i = 1; i < solution.length; i++) {
            toReturn += distanceMatrix[solution[i - 1]][solution[i]];
        }
        toReturn += distanceMatrix[solution[solution.length - 1]][0]; //final come back
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

    public PDPInstance(final double[][] distanceMatrix, HashMap<Integer, Integer> pickupToAssociatedDelivery) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
        this.optimal = -1;

        this.pickupToAssociatedDelivery = pickupToAssociatedDelivery;
        this.unrelatedNodes = new HashSet<>(IntStream.range(0, n).boxed().toList());

        deliveryToAssociatedPickup = new HashMap<>();
        for (int p : pickupToAssociatedDelivery.keySet()) {
            int d = pickupToAssociatedDelivery.get(p);
            unrelatedNodes.remove(p);
            unrelatedNodes.remove(d);
            deliveryToAssociatedPickup.put(d, p);
        }
    }

    public PDPInstance(String fileName) throws IOException {
        int numNodes;
        double[][] matrix = new double[0][0];
        OptionalDouble opti = OptionalDouble.empty();
        HashMap<Integer, Integer> pickupToAssociatedDelivery = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
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
                        opti = OptionalDouble.of(Integer.parseInt(tokens[3]));
                    }
                } else if (skip == 1) { // read distance matrix
                    int node = linesCount - skip - 1;
                    String[] tokens = line.split("\\s+");
                    double[] row =
                            Arrays.stream(tokens).filter(s -> !s.isEmpty()).mapToDouble(Double::parseDouble).toArray();
                    matrix[node] = row;
                } else { // read pick-up and delivery pairs
                    System.out.println(linesCount);
                    System.out.println(skip);
                    String[] tokens = line.split(" -> ");
                    pickupToAssociatedDelivery.put(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
                }

                linesCount++;
            }
        }

        this.distanceMatrix = matrix;
        this.pickupToAssociatedDelivery = pickupToAssociatedDelivery;

        this.n = distanceMatrix.length;
        this.optimal = opti.isPresent() ? opti.getAsDouble() : -1;

        this.unrelatedNodes = new HashSet<>(IntStream.range(0, n).boxed().toList());

        deliveryToAssociatedPickup = new HashMap<>();
        for (int p : pickupToAssociatedDelivery.keySet()) {
            int d = pickupToAssociatedDelivery.get(p);
            unrelatedNodes.remove(p);
            unrelatedNodes.remove(d);
            deliveryToAssociatedPickup.put(d, p);
        }
    }

    /**
     * Generate all the permutation (without symmetry) and find the optimal value.
     * <p>
     * <b>Note:</b> Must be used only for test and small instances.
     *
     * @return The optimal value
     */
    public double solveNaively() {
        TSPSolution best = new TSPSolution();
        Permutations.generateAllPermutations(0, n, permutation -> {
            System.out.println("best: " + best);
            double newVal = eval(permutation);
            System.out.printf("current %s - value: %f\n", permutation, newVal);
            if (newVal < best.value) {
                best.value = newVal;
                best.solution = permutation;
            }
        });

        System.out.println("\nfinal best: " + best);
        System.out.println("eval best: " + eval(best.solution));
        return best.value;
    }

    static class TSPSolution {
        List<Integer> solution = new ArrayList<>();
        double value = Double.POSITIVE_INFINITY;

        @Override
        public String toString() {
            return solution.toString() + " - value: " + value;
        }
    }

    public void writeInstance(String fileName, boolean solve) throws IOException {
        double opti = solve ? solveNaively() : 0.0;
        DecimalFormat df = new DecimalFormat("#.##########");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            if (solve) {
                bw.write(String.format("Nodes: %d Opti: %s%n%n", n, df.format(opti)));
            } else {
                bw.write(String.format("Nodes: %d%n%n", n));
            }

            String matrixStr = Arrays.stream(distanceMatrix).map(row -> Arrays.stream(row)
                            .mapToObj(x -> String.format("%3s", x))
                            .collect(Collectors.joining(" ")))
                    .collect(Collectors.joining("\n"));
            bw.write(matrixStr);
            bw.write("\n\n");

            for (Map.Entry<Integer, Integer> entry : pickupToAssociatedDelivery.entrySet()) {
                bw.write(String.format("%d -> %d%n", entry.getKey(), entry.getValue()));
            }
        }
    }


    public static void main(String[] args) throws IOException {
        PDPInstance instance = new PDPInstance("data/PDP/6_nodes.txt");
        //System.out.println(instance);
        System.out.println(instance.optimal);
        System.out.println(instance.solveNaively());
    }
}
