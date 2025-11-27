package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;
import smile.clustering.CentroidClustering;
import smile.clustering.KMeans;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * This strategy uses Generalized Hyperplan partitioning to create the clusters.
 * It requires a problem-specific StateCoordinates function that computes the coordinates of each state
 * @param <T>
 */
public class Kmeans<T> implements ReductionStrategy<T> {
    final private StateCoordinates<T> coordinates;
    private int maxIterations;
    private final boolean includeObj;

    public Kmeans(StateCoordinates<T> coordinates) {
        this.coordinates = coordinates;
        this.maxIterations = 50;
        this.includeObj = false;
    }

    public Kmeans(StateCoordinates<T> coordinates, int maxIterations, boolean includeObj) {
        this.coordinates = coordinates;
        this.maxIterations = maxIterations;
        this.includeObj = includeObj;
    }

    /**
     * Set the maximal number of iterations for the kmeans algorithm.
     * Default is 50.
     * @param maxIterations
     */
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * Computes maxWidth clusters using Kmeans.
     * Add the end all the nodes in the layer are added to a cluster, and the layer is empty.
     * @param layer the layer
     * @param maxWidth the desired maximal width after the restriction and relaxation
     * @return an array of List representing the clusters.
     */
    @Override
    public List<NodeSubProblem<T>>[] defineClusters(List<NodeSubProblem<T>> layer, int maxWidth) {
        int dimensions;
        if (includeObj)
            dimensions = coordinates.getCoordinates(layer.getFirst()).length;
        else
            dimensions = coordinates.getCoordinates(layer.getFirst().state).length;
        double[][] data = new double[layer.size()][dimensions];
        for (int node = 0; node < layer.size(); node++) {
            if (includeObj)
                data[node] = coordinates.getCoordinates(layer.get(node)).clone();
            else
                data[node] = coordinates.getCoordinates(layer.get(node).state).clone();
        }
        KMeans kmeans = KMeans.fit(data, maxWidth, maxIterations, 1.0E-20);
        // CentroidClustering<double[], double[]> clustering = KMeans.fit(data, maxWidth, maxIterations, 1.0E-10);
        List<NodeSubProblem<T>>[] clusters = new List[maxWidth];
        for (int i = 0; i < clusters.length; i++) {
            clusters[i] = new ArrayList<>();
        }
        for (int node = 0; node < layer.size(); node++) {
            double[] coords = data[node];
            int clusterIndex = kmeans.predict(coords);
            clusters[clusterIndex].add(layer.get(node));
        }
        return clusters;
    }
}
