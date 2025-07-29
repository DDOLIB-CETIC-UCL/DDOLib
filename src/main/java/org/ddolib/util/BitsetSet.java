package org.ddolib.util;

import java.util.*;

/**
 * Set of integer implemented using a BitSet.
 * Can be used to convert a BitSet to a set of integer for example.
 */
public class BitsetSet implements Set<Integer> {
    public BitSet set;

    public BitsetSet(BitSet set) {
        this.set = set;
    }

    public BitsetSet() {
        this(new BitSet());
    }

    @Override
    public int size() {
        return set.cardinality();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer e) {
            return set.get(e);
        }
        return false;
    }

    @Override
    public Iterator<Integer> iterator() {
        return set.stream().iterator();
    }

    @Override
    public Object[] toArray() {
        return set.stream().boxed().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a == null) throw new NullPointerException();
        int size = size();
        if (a.length < size) a = (T[]) new Object[size];
        if (a.length > size) a[size] = null;
        int i = 0;
        for (int e : this) a[i++] = (T)(Integer)e;
        return a;
    }

    @Override
    public boolean add(Integer e) {
        boolean contained = set.get(e);
        set.set(e);
        return !contained;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Integer e) {
            boolean contained = set.get(e);
            set.clear(e);
            return contained;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        boolean changed = false;
        for (Integer e : c) {
            if (add(e)) changed = true;
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        BitSet newSet = new BitSet(set.size());
        for (Object o : c) {
            if (o instanceof Integer e && set.get(e)) {
                newSet.set(e);
            }
        }
        if (set.equals(newSet)) return false;
        set = newSet;
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            if (remove(o)) changed = true;
        }
        return changed;
    }

    @Override
    public void clear() {
        set.clear();
    }
}
