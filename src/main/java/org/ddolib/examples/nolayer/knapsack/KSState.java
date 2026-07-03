package org.ddolib.examples.nolayer.knapsack;

/**
 * State for the NoLayer formulation of the Knapsack Problem.
 *
 * @param currentItem       The index of the item currently being considered.
 * @param remainingCapacity The remaining capacity in the knapsack.
 */
public record KSState(int currentItem, int remainingCapacity) {
    @Override
    public String toString() {
        return String.format("KSState(currentItem=%d, remainingCapacity=%d)", currentItem, remainingCapacity);
    }
}
