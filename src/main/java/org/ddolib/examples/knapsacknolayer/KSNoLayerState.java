package org.ddolib.examples.knapsacknolayer;

/**
 * State for the NoLayer formulation of the Knapsack Problem.
 *
 * @param currentItem       The index of the item currently being considered.
 * @param remainingCapacity The remaining capacity in the knapsack.
 */
public record KSNoLayerState(int currentItem, int remainingCapacity) {
    @Override
    public String toString() {
        return String.format("KSNoLayerState(currentItem=%d, remainingCapacity=%d)", currentItem, remainingCapacity);
    }
}
