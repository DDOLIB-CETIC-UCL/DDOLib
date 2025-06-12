package org.ddolib.ddo.implem.heuristics;

import org.ddolib.ddo.heuristics.StateDistance;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class FastMap<T> {
    private final Map<T,double[]> coordinates;
    private final int dimensions;
    // final T[][] pivotArray;
    private final StateDistance<T> distanceFunction;
    private int currentColumn;
    private double currentPivotDist;

    FastMap(List<T> states, int dimensions, StateDistance<T> distanceFunction) {
        this.coordinates = new HashMap<>(states.size());
        this.dimensions = dimensions;
        for (T state : states) {
            coordinates.put(state, new double[dimensions]);
        }
        this.distanceFunction = distanceFunction;
        currentColumn = -1;
        computeProjection();
    }

    /**
     * Select the state that is the farthest from the reference according to the distance function of the actual hyperplan
     * @param ref the reference
     * @return
     */
    private T selectFarthest(T ref) {
        double maxDistance = -1;
        T farthest = null;
        for (T state : coordinates.keySet()) {
            double currentDistance = computeDistance(state, ref);
            if (currentDistance > maxDistance) {
                maxDistance = currentDistance;
                farthest = state;
            }
        }
        currentPivotDist = maxDistance;
        return farthest;
    }

    /**
     * Computes the distance between two states in the current hyperplan
     * @param a
     * @param b
     * @return
     */
    private double computeDistance(T a, T b) {
        // TODO maybe caching already computed distances could save some computational cost
        double dist = distanceFunction.distance(a, b);
        dist = pow(dist, 2);
        for (int i = 0; i < currentColumn; i++) {
            dist -= pow((coordinates.get(a)[i] - coordinates.get(b)[i]), 2);
        }
        return sqrt(dist);
    }

    /**
     * Computes the coordinate of the current state on the line between the two pivot
     * @param state
     * @param pivotA
     * @param pivotB
     * @return
     */
    private double computeCoordinate(T state, T pivotA, T pivotB) {
        double distance = pow(computeDistance(state, pivotA), 2);
        distance += pow(currentPivotDist, 2);
        distance -= pow(computeDistance(state, pivotB), 2);
        distance = distance / (2 * currentPivotDist);
        return distance;
    }

    /**
     * Project the states in an Euclidian space with k dimensions
     */
    private void computeProjection() {
        int k = this.dimensions;
        currentColumn = 0;
        while (k > 0) {

            T b = coordinates.keySet().iterator().next();
            T a = null;
            for (int i = 0; i < 5; i++) {
                a = selectFarthest(b);
                b = selectFarthest(a);
            }

            if (computeDistance(a, b) == 0) break;

            for (T state : coordinates.keySet()) {
                coordinates.get(state)[currentColumn] = computeCoordinate(state, a, b);
            }

            k -= 1;
            currentColumn++;
        }
    }

    public double[] getCoordinates(T state) {
        return coordinates.get(state);
    }

}
