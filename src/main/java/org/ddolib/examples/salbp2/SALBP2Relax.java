package org.ddolib.examples.salbp2;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

public class SALBP2Relax implements Relaxation<SALBP2State> {
    final SALBP2Problem problem;
    public SALBP2Relax(final SALBP2Problem problem) {
        this.problem = problem;
    }

    @Override
    public SALBP2State mergeStates(final Iterator<SALBP2State> states) {
        BitSet[] stations = new BitSet[problem.nbStations];
        for (int i = 0; i < problem.nbStations; i++) {
            stations[i] = new BitSet(problem.nbTasks);
        }
        double[] cyclePerStation = new double[problem.nbStations];
        double cycle = Double.MIN_VALUE;
        while (states.hasNext()) {
            SALBP2State state = states.next();
            for (int i = 0; i < problem.nbStations; i++) {
                stations[i].or(state.stations()[i]);
            }
        }
        for (int i = 0; i < problem.nbStations; i++) {
            for (int j = stations[i].nextSetBit(0); j < stations[i].nextSetBit(0); j++) {
                cyclePerStation[j] += problem.durations[j];
            }
            cycle = Math.max(cycle, cyclePerStation[i]);
        }
        return new SALBP2State(stations, cyclePerStation, cycle);
    }

    @Override
    public double relaxEdge(SALBP2State from, SALBP2State to, SALBP2State merged, Decision d, double cost) {
        return cost;
    }
}
