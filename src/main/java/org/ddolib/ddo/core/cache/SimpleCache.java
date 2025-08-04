package org.ddolib.ddo.core.cache;

import org.ddolib.modeling.Problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimpleCache<T> implements Cache<T> {
    private final ArrayList<Layer<T>> thresholdsByLayer = new ArrayList<>();

    public SimpleCache() {
    }

    @Override
    public void initialize(Problem<T> problem) {
        int nbVariables = problem.nbVars();
        for (int i = 0; i < nbVariables; i++) {
            thresholdsByLayer.add(new Layer<T>());
        }
    }

    @Override
    public Optional<Threshold> getThreshold(T state, int depth) {
        for (T s : thresholdsByLayer.get(depth).map.keySet()) {
            if (s.equals(state)) {
                return Optional.of(thresholdsByLayer.get(depth).map.get(s));
            }
        }
        return thresholdsByLayer.get(depth).get(state);
    }

    @Override
    public void updateThreshold(T state, int depth, Threshold threshold) {
        if (depth >= thresholdsByLayer.size()) return;
        thresholdsByLayer.get(depth).update(state, threshold);
    }

    @Override
    public void clearLayer(int depth) {
        thresholdsByLayer.get(depth).clear();
    }

    @Override
    public void clear(int nbVariables) {
        for (int depth = 0; depth < nbVariables; depth++) {
            thresholdsByLayer.get(depth).clear();
        }
    }

    @Override
    public SimpleCache.Layer<T> getLayer(int depth) {
        return thresholdsByLayer.get(depth);
    }


    // Inner class to represent a synchronized layer with a read-write lock
    public static class Layer<T> {
        private final Map<T, Threshold> map = new HashMap<>();
        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        public Optional<Threshold> get(T state) {
            lock.readLock().lock();
            try {
                return Optional.ofNullable(map.get(state));
            } finally {
                lock.readLock().unlock();
            }
        }

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

        public boolean containsKey(T state) {
            lock.writeLock().lock();
            try {
                if (map.containsKey(state)) {
                    return true;
                }
            } finally {
                lock.writeLock().unlock();
            }
            return false;
        }


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