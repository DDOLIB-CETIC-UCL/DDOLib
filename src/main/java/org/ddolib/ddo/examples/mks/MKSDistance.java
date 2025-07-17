package org.ddolib.ddo.examples.mks;

import org.ddolib.ddo.heuristics.StateDistance;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class MKSDistance implements StateDistance<MKSState> {
    @Override
    public double distance(MKSState a, MKSState b) {
        double distance = 0.0;
        for (int dim = 0; dim < a.capacities.length; dim++) {
            distance += pow(a.capacities[dim] - b.capacities[dim], 2);
        }
        distance = sqrt(distance);

        return distance;
    }
}
