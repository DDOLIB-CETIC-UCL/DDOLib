package org.ddolib.examples.tsalt;


import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;


public class TSCoordinates implements StateCoordinates<TSState> {
    final private TSProblem instance;

    public TSCoordinates(TSProblem instance) {
        this.instance = instance;
    }

    @Override
    public double[] getCoordinates(TSState state) {
        double[] coordinates = new double[instance.nbScene + instance.nbActors];
        for (int i = 0; i < instance.nbScene; i++) {
            if (state.remainingScenes().get(i))
                coordinates[i] = 1;
            else
                coordinates[i] = 0;
        }
        for (int i = 0; i < instance.nbActors; i++) {
            if (state.onLocationActors().get(i))
                coordinates[instance.nbScene + i] = 1;
            else
                coordinates[instance.nbScene + i] = 0;
        }
        return coordinates;
    }
}
