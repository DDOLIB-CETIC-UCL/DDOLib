package org.ddolib.astar.examples.JSTSP;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.*;

public class JSTSPProblem  implements Problem<JSTSPState> {

    JSTSPInstance instance;

    double[][] distanceMatrix;

    ArrayList<Edge> edges;

    HashMap<Long, Integer> kruskal_value;

    boolean[] ret;

    UF unionFind;

    public JSTSPProblem( JSTSPInstance instance, double[][] distanceMatrix) {
        this.instance = instance;
        this.distanceMatrix = distanceMatrix;
        this.edges = new ArrayList<>(this.instance.n * this.instance.n);
        for (int i = 1; i < this.instance.n; i++) {
            for (int j = i + 1; j < this.instance.n; j++) {
                edges.add(new Edge(i, j, this.distanceMatrix[i][j]));
            }
        }
        edges.sort(Edge::compareTo);
        this.kruskal_value = new HashMap<Long, Integer>();
        this.ret = new boolean[this.instance.n];
        this.unionFind = new UF(this.instance.n);

    }
    @Override
    public int nbVars() {
        return this.instance.n - 1;
    }

    @Override
    public JSTSPState initialState() {
        long remaining = 0L;
        for (int i = 1; i < this.instance.n; i++) {
            remaining |= 1L << i;
        }
        return new JSTSPState(0, new FreeTools(this.instance.c), remaining);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(JSTSPState state, int var) {
        List<Integer> ret = new ArrayList<>();
        long active_tools = 0L;
        for (int i = 0; i < this.instance.n; i++) {
            if ((state.remaining & (1L << i)) == 1L << i) {
                ret.add(i);
            }
            else{
                active_tools = active_tools | this.instance.jobs[i];
            }
        }
        if (Long.bitCount(active_tools)<= this.instance.c){
            for (int i:ret){
                if ((active_tools |this.instance.jobs[i]) == active_tools ){
                    return Collections.singletonList(i).iterator();
                }
            }
        }

        return ret.iterator();
    }

    @Override
    public JSTSPState transition(JSTSPState state, Decision decision) {
        FreeTools fnew = new FreeTools(state.f);
        int cost = 0;
        for (int t : this.instance.tools[decision.val()]) {
            // if the tool is not in the current
            if ((this.instance.jobs[state.currentJobIdx] & (1L << t)) == 0L) {
                int i = fnew.contains(t);
                if (i != -1) {
                    fnew.remove(i, t);
                } else {
                    cost++;
                }
            }
        }
        int slack = this.instance.c - this.instance.tools[decision.val()].size();
        long free = this.instance.jobs[state.currentJobIdx] & ~this.instance.jobs[decision.val()];
        if (slack > 0) {
            if (slack < fnew.size) {
                fnew.removeFrom(slack);
            }
            if (free != 0L) {
                fnew.add(slack, free);
            }
        } else {
            fnew.reset();
        }
        return new JSTSPState(decision.val(), fnew, state.remaining & ~(1L << decision.val()));
    }

    @Override
    public double transitionCost(JSTSPState state, Decision decision) {
        FreeTools fnew = new FreeTools(state.f);
        int cost = 0;
        for (int t : this.instance.tools[decision.val()]) {
            // if the tool is not in the current
            if ((this.instance.jobs[state.currentJobIdx] & (1L << t)) == 0L) {
                int i = fnew.contains(t);
                if (i != -1) {
                    fnew.remove(i, t);
                } else {
                    cost++;
                }
            }
        }

        return -cost;
    }
}
