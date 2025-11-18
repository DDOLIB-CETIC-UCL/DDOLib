package org.ddolib.examples.maxcover;

import java.io.IOException;
import java.util.BitSet;

public class MaxCoverDdoMain {
    public static void main(String[] args) throws IOException {
        int n = 5; int m = 4; int k = 2;
        BitSet[] ss = new BitSet[m];
        ss[0] = new BitSet(n);  ss[0].set(0); ss[0].set(1);
        ss[1] = new BitSet(n);  ss[1].set(1); ss[1].set(2);
        ss[2] = new BitSet(n);  ss[2].set(2); ss[2].set(3);
        ss[3] = new BitSet(n);  ss[3].set(3); ss[3].set(4);
        MaxCoverProblem problem = new MaxCoverProblem(n, m, k, ss);


    }
}
