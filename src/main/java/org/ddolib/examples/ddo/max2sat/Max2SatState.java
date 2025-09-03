package org.ddolib.examples.ddo.max2sat;

import java.util.ArrayList;

/**
 * Class to contain data for the Max2Sat sate. The state contains its depth in the associated MDD and a list of net
 * benefits.<br>
 * <p>
 * This list contains:
 * <ul>
 *     <li> for each {@code k >= depth}, the net benefits by setting the variable {@code K} to
 *     {@code true} knowing the assignment of the previous variables (a negative value is the net
 *     benefit to assign the variable to <code>false</code>);
 *     </li>
 *     <li>for each {@code k < depth}, {@code 0}, modeling that these variables have been
 *     assigned in previous layer and cannot influence the objective anymore.</li>
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
}
