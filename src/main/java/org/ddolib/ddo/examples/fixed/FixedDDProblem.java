package org.ddolib.ddo.examples.fixed;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;
import org.ddolib.ddo.examples.TSPIncrementalHop.EdgeList;
import org.ddolib.ddo.util.FixedDD;

import java.util.*;

public class FixedDDProblem implements Problem<Set<Integer>> {

    FixedDD dd;
    public FixedDDProblem(FixedDD dd) {
        this.dd = dd;
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public int nbVars() {
        return dd.nLayers();
    }

    @Override
    public Set<Integer> initialState() {
        return Set.of(dd.getSource());
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(Set<Integer> state, int var) {
        Set<Integer> incident = new HashSet<>();
        for (int n: state) {
            for (FixedDD.Edge e: this.dd.outEdge(n)) {
                incident.add(e.to());
            }
        }
        return incident.iterator();
    }

    @Override
    public Set<Integer> transition(Set<Integer> state, Decision decision) {
        return Set.of(decision.val());
    }

    @Override
    public int transitionCost(Set<Integer> state, Decision decision) {
        // must compute
        int cost = Integer.MIN_VALUE;
        for (int n: state) {
            for (FixedDD.Edge e: this.dd.outEdge(n)) {
                if (e.to() == decision.val()) {
                    cost = Math.max(cost, e.cost());
                }
            }
        }
        return cost;
    }
}