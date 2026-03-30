package org.ddolib.examples.gruler;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

/**
 * Lower bound for the Golomb Ruler.
 *
 * <p> It assumes that the next marks will add the smallest missing distances.</p>
 *
 */
public class GRFastLowerBound implements FastLowerBound<GRState> {


    @Override
    public double fastLowerBound(GRState state, Set<Integer> variables) {
        int missingMarks = variables.size();

        int i = 0;
        int cost = 0;
        while (missingMarks != 0) {
            if (i != 0 && !state.getDistances().get(i)) {
                cost += i;
                missingMarks--;
            }
            i++;
        }
        return cost;
    }
}
