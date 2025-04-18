package org.ddolib.ddo.examples.mcp;

import java.util.ArrayList;

/**
 * Class to contain data for the Max2Sat sate. The state contain its depth in the associated MDD and a list of net
 * benefits.<br>
 * <p>
 * This list contains:
 * <ul>
 *     <li> for each {@code k >= depth}, the net benefits by setting the variable {@code k} to the
 *     partition {@code T}
 *     knowing the assignment of the previous variables;
 *     </li>
 *     <li>for each {@code k < depth}, {@code 0}, modeling that these variable has been assigned in
 *     previous layer and cannot influence the objective anymore.</li>
 * </ul>
 */
public record MCPState(ArrayList<Integer> netBenefit, int depth) {

    @Override
    public String toString() {
        return netBenefit.toString();
    }
}
