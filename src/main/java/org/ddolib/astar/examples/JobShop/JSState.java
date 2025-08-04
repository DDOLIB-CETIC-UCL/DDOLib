package org.ddolib.astar.examples.JobShop;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

public class JSState {
    int[][] est;                                                        //2D vector of the earliest starting time of each operation
    BitSet done;
    int[] order;

    public JSState(int[][] est) {
        this.est = est;
        this.done = new BitSet(est.length * est[0].length);
        this.order = new int[est.length * est[0].length];
    }

    public JSState(int[][] est, BitSet done, int[] order) {
        this.est = new int[est.length][est[0].length];
        this.done = (BitSet) done.clone();
        for (int i = 0; i < est.length; i++) {
            for (int j = 0; j < est[0].length; j++) {
                this.est[i][j] = est[i][j];
            }
        }
        this.order = order.clone();
    }

    @Override
    public int hashCode() {
        int[] estd = new int[done.cardinality()];
        int idx = 0;
        for (int i = this.done.nextSetBit(0); i >= 0; i = this.done.nextSetBit(i + 1)) {
            int jobId = i / this.est[0].length;
            int opId = i % this.est[0].length;
            estd[idx] = this.est[jobId][opId];
            idx++;
        }
        return Objects.hash(done, Arrays.hashCode(estd));

    }

    @Override
    public boolean equals(Object obj) {
        JSState other = (JSState) obj;
        if (this.done.equals(other.done)) {
            for (int i = this.done.nextSetBit(0); i >= 0; i = this.done.nextSetBit(i + 1)) {
                int jobId = i / this.est[0].length;
                int opId = i % this.est[0].length;
                if (this.est[jobId][opId] != other.est[jobId][opId]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    @Override
    public String toString() {
        return "Earliest start :" + Arrays.deepToString(this.est) + "\n" + "Done: " + done.toString() + "\n" + "Order :"+ Arrays.toString(this.order) + "\n";
    }
}
