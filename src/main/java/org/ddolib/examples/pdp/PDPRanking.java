package org.ddolib.examples.pdp;

import org.ddolib.modeling.StateRanking;
/**
 * Implements a state ranking strategy for the Pickup and Delivery Problem (PDP).
 * <p>
 * This ranking is used to order states during search algorithms, such as DDO or ACS,
 * to prioritize exploration of certain states over others.
 * </p>
 *
 * <p>
 * Currently, all states are considered equal in rank (compare always returns 0).
 * The commented-out line hints at a possible ranking based on the uncertainty
 * of the vehicle's content, where states with more uncertain content could be
 * ranked differently.
 * </p>
 *
 * <p>
 * This class implements the {@link StateRanking} interface, which allows it
 * to be plugged into search models for PDP optimization.
 * </p>
 */
public class PDPRanking implements StateRanking<PDPState> {
    /**
     * Compares two PDP states to determine their relative rank.
     *
     * @param o1 the first PDPState to compare
     * @param o2 the second PDPState to compare
     * @return 0 indicating both states are currently considered of equal rank
     */
    @Override
    public int compare(final PDPState o1, final PDPState o2){
        //children of merged are good candidates for merged as well
        //there are the one with imprecise min and maxContent
        return 0; //Integer.compare(o1.uncertaintyOnContent(), o2.uncertaintyOnContent());
    }
}
