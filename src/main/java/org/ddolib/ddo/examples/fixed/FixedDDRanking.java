package org.ddolib.ddo.examples.fixed;

import org.ddolib.ddo.heuristics.StateRanking;

import java.util.Set;

public class FixedDDRanking implements StateRanking<Set<Integer>> {

    @Override
    public int compare(Set<Integer> o1, Set<Integer> o2) {
        // The state with the most remaining nodes is most interesting
        return Integer.compare(o1.size(), o2.size());
    }
}