package org.ddolib.examples.mks;
import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;
import org.ddolib.ddo.core.mdd.NodeSubProblem;

public class MKSCoordinates implements StateCoordinates<MKSState> {
    @Override
    public double[] getCoordinates(MKSState state) {
        return state.capacities.clone();
    }

    @Override
    public double[] getCoordinates(NodeSubProblem<MKSState> node) {
        MKSState state = node.state;
        double[] coordinates = new double[state.capacities.length + 1];
        System.arraycopy(state.capacities, 0, coordinates, 0, state.capacities.length);
        coordinates[coordinates.length - 1] = node.getValue();
        return coordinates;
    }


}
