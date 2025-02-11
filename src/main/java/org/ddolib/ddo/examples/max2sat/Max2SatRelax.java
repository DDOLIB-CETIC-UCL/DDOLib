package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.ArrayList;
import java.util.Iterator;

public class Max2SatRelax implements Relaxation<ArrayList<Integer>> {
    @Override
    public ArrayList<Integer> mergeStates(Iterator<ArrayList<Integer>> states) {
        return null;
    }

    @Override
    public int relaxEdge(ArrayList<Integer> from, ArrayList<Integer> to, ArrayList<Integer> merged, Decision d, int cost) {
        return 0;
    }
}
