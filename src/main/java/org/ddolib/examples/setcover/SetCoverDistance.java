package org.ddolib.examples.setcover;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import static org.ddolib.util.DistanceUtil.*;

import java.util.Set;

public class SetCoverDistance implements StateDistance<SetCoverState> {

    private final SetCoverProblem instance;

    public SetCoverDistance(SetCoverProblem instance) {
        this.instance = instance;
    }


    /**
     * The distance between two states in the set cover problem is the
     * size of the symmetric difference between the two sets of uncovered elements
     * @param a the first state
     * @param b the second state
     * @return
     */
    @Override
    public double distance(SetCoverState a, SetCoverState b) {
        return jaccardDistance(a.uncoveredItems(), b.uncoveredItems());
        // return symmetricDifference(a.uncoveredElements, b.uncoveredElements);
    }


}
