package org.ddolib.ddo.implem.mdd;
import org.ddolib.ddo.core.Relaxation;
import org.ddolib.ddo.heuristics.StateCoordinates;

import static java.util.Collections.shuffle;

import java.util.Arrays;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import static java.lang.Math.sqrt;
import static java.lang.Math.pow;

public class KMeans {

    /** Computes the euclidean distance between the two array of coordinates */
    private static double euclideanDistance(double[] a, double[] b) {
        assert a.length == b.length;
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += pow(a[i] - b[i], 2);
        }
        return sqrt(sum);
    }

    private static <T> double[][] computeRange(List<T> nodes, StateCoordinates<T> coords) {
        int dimensions = coords.getCoordinates(nodes.getFirst()).length;
        double[][] range = new double[dimensions][2]; // min and max for each dimension
        boolean isFirst = true;
        for (T node: nodes) {
            double[] coordinates = coords.getCoordinates(node);
            for (int dimension = 0; dimension < dimensions; dimension++) {
                if (isFirst || range[dimension][0] > coordinates[dimension])
                    range[dimension][0] = coordinates[dimension];
                if (isFirst || range[dimension][1] < coordinates[dimension])
                    range[dimension][1] = coordinates[dimension];
                if (isFirst) isFirst = false;
            }
        }

        return range;
    }

    private static <T> int closestCentroid(T node, StateCoordinates<T> coords, double[][] centroides) {
        int assignment = -1;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < centroides.length; i++) { // O(W)
            double distance = euclideanDistance(coords.getCoordinates(node), centroides[i]); // O(d)
            if (distance < minDistance) {
                minDistance = distance;
                assignment = i;
            }
        }

        return assignment;
    }

    public static <T> int[] kMeans(List<T> nodes, StateCoordinates<T> coords, int k, int maxIterations, Random rnd){
        int dimensions = coords.getCoordinates(nodes.getFirst()).length;

        double[][] range = computeRange(nodes, coords);

        // each lines correspond to the coordinates of one centroide
        double[][] centroides = new double[k][dimensions]; // O(W)

        // Select random nodes to be the first centroides
        for (int i = 0 ; i < k; i++) { // O(W)
            for (int d = 0; d < dimensions; d++) {
                centroides[i][d] = range[d][0] + rnd.nextDouble() * (range[d][1] - range[d][0]);
            }
        }

        // assignments[i] contains the index of the cluster containing i
        int[] assignments = new int[nodes.size()];
        int[] clustersSize = new int[k];

        for(int iter = 0; iter < maxIterations; iter++) { // 50 iterations
            boolean changed = false;

            // Assign each node to its closest centroide
            for (int i = 0; i < nodes.size(); i++) { // O(n)
                int newAssignment = closestCentroid(nodes.get(i), coords, centroides);
                if (newAssignment != assignments[i]) {
                    changed = true;
                    assignments[i] = newAssignment;
                }
            }


            // Update the centroides by computing the mean coordinates of each node in the cluster
            for (double[] centroideCoord: centroides) { // O(w)
                Arrays.fill(centroideCoord, 0); // O(d)
            }

            Arrays.fill(clustersSize, 0);
            for (int i = 0; i < assignments.length; i++) { // O(n)
                int cluster = assignments[i];
                double[] point = coords.getCoordinates(nodes.get(i)); // O(d)
                for (int j = 0; j < dimensions; j++) { // O(d)
                    centroides[cluster][j] += point[j];
                }
                clustersSize[cluster]++;
            }

            for (int cluster = 0; cluster < k; cluster++) { // O(W)

                // If the cluster is empty then a new centroid is randomly picked
                if (clustersSize[cluster] == 0) {
                    for (int d = 0; d < dimensions; d++) {
                        centroides[cluster][d] = range[d][0] + rnd.nextDouble() * (range[d][1] - range[d][0]);
                        changed = true;
                    }
                } else {
                    // Otherwise its location is updated
                    for (int d = 0; d < dimensions; d++) { // O(d)
                        centroides[cluster][d] /= clustersSize[cluster];
                    }
                }
            }

            // If algorithm has converged
            if (!changed) break;
        }

        // Compile the clusters
        /*List<T>[] clusters = new List[k];
        for (int i = 0; i < clusters.length; i++) {
            clusters[i] = new ArrayList<>();
        }
        for (int node = 0; node < nodes.size(); node++) {
            clusters[assignments[node]].add(nodes.get(node));
        }*/

        return assignments;
    }

}
