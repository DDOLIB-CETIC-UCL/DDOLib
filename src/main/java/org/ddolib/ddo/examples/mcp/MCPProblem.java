package org.ddolib.ddo.examples.mcp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import java.lang.Integer;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Math.abs;


public class MCPProblem implements Problem<MCPState> {

    public final int S = 0;
    public final int T = 1;

    final Graph graph;

    public MCPProblem(Graph graph) {
        this.graph = graph;
    }


    @Override
    public int nbVars() {
        return graph.numNodes;
    }

    @Override
    public MCPState initialState() {
        return new MCPState(new ArrayList<>(Collections.nCopies(nbVars(), 0)), 0);
    }

    @Override
    public int initialValue() {
        int val = 0;
        for (int i = 0; i < nbVars(); i++) {
            for (int j = i + 1; j < nbVars(); j++) {
                val += negativeOrNull(graph.weightOf(i, j));
            }
        }
        return val;
    }

    @Override
    public Iterator<Integer> domain(MCPState state, int var) {
        if (state.depth() == 0) return List.of(S).iterator();
        else return List.of(S, T).iterator();
    }

    @Override
    public MCPState transition(MCPState state, Decision decision) {
        ArrayList<Integer> newBenefits = new ArrayList<>(Collections.nCopies(nbVars(), 0));
        int k = decision.var();
        if (decision.val() == S) {
            for (int l = k + 1; l < nbVars(); l++) {
                int benef = state.netBenefit().get(l) + graph.weightOf(k, l);
                newBenefits.set(l, benef);
            }
        } else {
            for (int l = k + 1; l < nbVars(); l++) {
                int benef = state.netBenefit().get(l) - graph.weightOf(k, l);
                newBenefits.set(l, benef);
            }
        }
        return new MCPState(newBenefits, state.depth() + 1);
    }

    @Override
    public int transitionCost(MCPState state, Decision decision) {
        if (state.depth() == 0) return 0;
        else if (decision.val() == S) return branchOnS(state, decision.var());
        else return branchOnT(state, decision.var());
    }

    private int branchOnS(MCPState state, int k) {
        int cost = positiveOrNull(-state.netBenefit().get(k));

        for (int l = k + 1; l < nbVars(); l++) {
            int skl = state.netBenefit().get(l);
            int wkl = graph.weightOf(k, l);

            if (skl * wkl <= 0) cost += min(abs(skl), abs(wkl));
        }

        return cost;
    }

    private int branchOnT(MCPState state, int k) {
        int cost = positiveOrNull(state.netBenefit().get(k));

        for (int l = k + 1; l < nbVars(); l++) {
            int skl = state.netBenefit().get(l);
            int wkl = graph.weightOf(k, l);

            if (skl * wkl >= 0) cost += min(abs(skl), abs(wkl));
        }

        return cost;
    }

    private int positiveOrNull(int x) {
        return max(x, 0);
    }

    private int negativeOrNull(int x) {
        return min(x, 0);
    }
}
