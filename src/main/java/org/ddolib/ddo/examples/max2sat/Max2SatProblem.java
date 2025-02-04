package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Max2SatProblem implements Problem<Max2SatState> {

    final Max2SatState state;
    private final int numVar;
    final HashMap<BinaryClause, Integer> weight;

    public Max2SatProblem(int numVar, HashMap<BinaryClause, Integer> weight) {
        this.numVar = numVar;
        this.weight = weight;
        this.state = new Max2SatState(new int[numVar], 0);

    }

    @Override
    public int nbVars() {
        return numVar;
    }

    @Override
    public Max2SatState initialState() {
        return state;
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(Max2SatState state, int var) {
        if (var < state.depth) return List.of(0).iterator();
        else return List.of(1, 0).iterator();
    }

    @Override
    public Max2SatState transition(Max2SatState state, Decision decision) {
        return null;
    }

    @Override
    public int transitionCost(Max2SatState state, Decision decision) {
        return 0;
    }


    private int toBinaryClauseVariable(int x) {
        return x + 1;
    }

    private int t(int x) {
        return toBinaryClauseVariable(x);
    }

    private int f(int x) {
        return -toBinaryClauseVariable(x);
    }

}
