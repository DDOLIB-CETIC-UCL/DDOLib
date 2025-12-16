package org.ddolib.ddo.core.cache;

import org.ddolib.modeling.Problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A simple implementation of a {@link Cache} for storing threshold values associated with states in a dynamic programming model.
 * <p>
 * The cache maintains a list of layers, where each layer corresponds to a depth in the problem.
 * Each layer stores thresholds for states at that depth and is thread-safe via a {@link ReadWriteLock}.
 * </p>
 *
 * @param <T> the type of state stored in the cache
 */
public class SimpleCache<T> implements Cache<T> {
    /**
     * List of layers, each representing a depth in the problem.
     */
    private final ArrayList<Layer<T>> thresholdsByLayer = new ArrayList<>();

    /**
     * Constructs an empty SimpleCache.
     */
    public SimpleCache() {
    }

    /**
     * Initializes the cache based on the number of variables of the problem.
     * Creates a layer for each variable (depth).
     *
     * @param problem the problem for which the cache is initialized
     */
    @Override
    public void initialize(Problem<T> problem) {
        int nbVariables = problem.nbVars();
        for (int i = 0; i < nbVariables; i++) {
            thresholdsByLayer.add(new Layer<T>());
        }
    }

    /**
     * Returns the layer at a given depth.
     *
     * @param depth the depth of the layer
     * @return the layer object
     */
    @Override
    public SimpleCache.Layer<T> getLayer(int depth) {
        return thresholdsByLayer.get(depth);
    }

    /**
     * Retrieves the threshold associated with a given state at a specific depth.
     *
     * @param state the state to query
     * @param depth the depth of the state in the DP model
     * @return an {@link Optional} containing the threshold if present, otherwise empty
     */
    @Override
    public Optional<Threshold> getThreshold(T state, int depth) {
        for (T s : thresholdsByLayer.get(depth).map.keySet()) {
            if (s.equals(state)) {
                return Optional.of(thresholdsByLayer.get(depth).map.get(s));
            }
        }
        return thresholdsByLayer.get(depth).get(state);
    }

    /**
     * Updates the threshold for a given state at a specified depth.
     * If the new threshold is greater than the current threshold (or no current threshold exists),
     * the threshold is updated.
     *
     * @param state     the state to update
     * @param depth     the depth of the state
     * @param threshold the new threshold value
     */
    @Override
    public void updateThreshold(T state, int depth, Threshold threshold) {
        if (depth >= thresholdsByLayer.size()) return;
        thresholdsByLayer.get(depth).update(state, threshold);
    }

    /**
     * Clears all thresholds in a specific layer.
     *
     * @param depth the depth of the layer to clear
     */
    @Override
    public void clearLayer(int depth) {
        thresholdsByLayer.get(depth).clear();
    }

    /**
     * Clears all layers of the cache up to the given number of variables.
     *
     * @param nbVariables number of layers to clear
     */
    @Override
    public void clear(int nbVariables) {
        for (int depth = 0; depth < nbVariables; depth++) {
            thresholdsByLayer.get(depth).clear();
        }
    }

    /**
     * Returns statistics about the cache, including number of hits, tests, and total entries.
     *
     * @return a string summarizing cache statistics
     */
    public String stats() {
        int nbTests = 0;
        int nbHits = 0;
        int size = 0;
        for (Layer l : thresholdsByLayer) {
            nbHits += l.nbHits;
            nbTests += l.nbTests;
            size += l.map.size();
        }
        return "stats(nbHits: " + nbHits + ", nbTests: " + nbTests + ", size:" + size + ")";
    }

    /**
     * Inner class representing a synchronized cache layer for a specific depth.
     * <p>
     * Each layer maps states to thresholds and provides thread-safe read/write operations
     * using a {@link ReadWriteLock}. It also tracks hit/miss statistics for queries.
     * </p>
     *
     * @param <T> the type of state stored in the layer
     */
    public static class Layer<T> {
        /**
         * Map of states to their thresholds.
         */
        private final Map<T, Threshold> map = new HashMap<>();

        /**
         * Lock for thread-safe access.
         */
        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        /**
         * Number of queries to this layer.
         */
        public int nbTests = 0;

        /**
         * Number of cache hits.
         */
        public int nbHits = 0;

        /**
         * Retrieves the threshold for a given state in a thread-safe manner.
         *
         * @param state the state to query
         * @return an {@link Optional} containing the threshold if present, otherwise empty
         */

        public Optional<Threshold> get(T state) {
            lock.readLock().lock();
            try {
                return Optional.ofNullable(map.get(state));
            } finally {
                lock.readLock().unlock();
            }
        }

        /**
         * Updates the threshold for a state if the new threshold is greater than the current.
         *
         * @param state        the state to update
         * @param newThreshold the new threshold value
         */
        public void update(T state, Threshold newThreshold) {
            lock.writeLock().lock();
            try {
                Threshold current = map.get(state);
                if (current == null || newThreshold.compareTo(current) > 0) {
                    map.put(state, newThreshold);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        /**
         * Checks whether the layer contains a given state, updating hit/test statistics.
         *
         * @param state the state to check
         * @return true if the state exists in the layer, false otherwise
         */
        public boolean containsKey(T state) {
            lock.writeLock().lock();
            nbTests += 1;
            try {
                if (map.containsKey(state)) {
                    nbHits += 1;
                    return true;
                }
            } finally {
                lock.writeLock().unlock();
            }
            return false;
        }

        /**
         * Clears all entries in the layer.
         */
        public void clear() {
            lock.writeLock().lock();
            try {
                map.clear();
            } finally {
                lock.writeLock().unlock();
            }
        }

        @Override
        public String toString() {
            return map.keySet().toString() + " ---> " + map.values().toString();
        }
    }
}