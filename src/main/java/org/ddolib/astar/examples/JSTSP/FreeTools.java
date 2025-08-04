package org.ddolib.astar.examples.JSTSP;

import java.util.Arrays;
import java.util.Objects;

public class FreeTools extends Object {

    protected long[] bitsets;
    protected int size = 0;
    protected long hash;
    protected  boolean modified;

    public FreeTools(FreeTools f) {
        this.bitsets = Arrays.copyOf(f.bitsets, f.bitsets.length);
        this.size = f.size;
        this.modified = false;

        this.hash = f.hash;

//        String test="";
//        for (int i = 1; i < size; i++) {
//            test += Long.toString(bitsets[i]);
//            test += ";";
//        }
//        this.hash = test.hashCode();



    }

    public FreeTools(int c) {
        bitsets = new long[c];
        size = 0;
        this.hash = 0;
        this.modified = false;

    }

    public int contains(int t) {
        for (int i = 1; i < size; i++) {
            if ((1L << t & bitsets[i]) != 0) {
                return i;
            }
        }
        return -1;
    }

    public void add(int i, int t) {
        assert (i >= 1);
        bitsets[i] |= 1L << t;
        size = Math.max(size, i + 1);
        modified = true;
    }

    public void add(int i, long tools) {
        assert (i >= 1);
        bitsets[i] |= tools;
        size = Math.max(size, i + 1);
        modified = true;
    }

    public void remove(int i, int t) {
        assert (i >= 1);
        bitsets[i] &= ~(1L << t);
        bitsets[i - 1] |= bitsets[i];
        for (int j = i; j < size - 1; j++) {
            bitsets[j] = 0L;
            bitsets[j] = bitsets[j + 1];
        }
        bitsets[size - 1] = 0L;
        size--;
        modified = true;
    }

    public void removeFrom(int i) {
        for (int j = i + 1; j < size; j++) {
            bitsets[i] |= bitsets[j];
            bitsets[j] = 0L;
        }
        size = i + 1;
        modified = true;
    }

    public void reset() {
        for (int j = 0; j < size; j++) {
            bitsets[j] = 0L;
        }
        size = 0;
        modified = true;
    }

    @Override
    public int hashCode() {
        if (modified){
            this.hash = 1234;
            for (int i = bitsets.length; --i >= 0; ){
                this.hash ^= bitsets[i] * (i + 1);
            }
            this.hash = (this.hash >> 32) ^ this.hash;


            this.modified = false;
        }
        return (int)this.hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FreeTools) {
            FreeTools o = (FreeTools) other;
            if (this.size != o.size) {
                return false;
            }
            if (this.hash != o.hash) {
                return false;
            }
            for (int i = 1; i < size; i++) {
                if (bitsets[i] != o.bitsets[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String toString() {
        return Arrays.toString(bitsets);
    }

}
