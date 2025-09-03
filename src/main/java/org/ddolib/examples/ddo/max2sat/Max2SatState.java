package org.ddolib.examples.ddo.max2sat;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Class to contain data for the Max2Sat sate. The state contains its depth in the associated MDD and a list of net
 * benefits.<br>
 * <p>
 * This list contains:
 * <ul>
 *     <li> for each <code>k >= depth</code>, the net benefits by setting the variable <code>K</code> to
 *     <code>true</code> knowing the assignment of the previous variables (a negative value is the net benefit to
 *     assign the variable to <code>false</code>);
 *     </li>
 *     <li>for each <code>k < depth</code>, <code>0</code>, modeling that these variables have been assigned in
 *     previous layer and cannot influence the objective anymore.</li>
 * </ul>
 *
 * @param netBenefit
 * @param depth
 */
public record Max2SatState(ArrayList<Integer> netBenefit, int depth) {

    @Override
    public String toString() {
        return netBenefit.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Max2SatState(ArrayList<Integer> otherBenefit, int otherDepth)) {
            return this.depth == otherDepth && this.netBenefit == otherBenefit;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(netBenefit, depth);
    }
}
