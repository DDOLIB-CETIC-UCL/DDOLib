package org.ddolib.ddo.core.cache;
/**
 * Represents a threshold value associated with a state in a dynamic programming or search model.
 * <p>
 * Each {@code Threshold} has a numeric value and a boolean flag indicating whether the state has been explored.
 * Thresholds can be compared to each other based on their value and exploration status.
 * </p>
 * <p>
 * This class is typically used in caching, pruning, or dominance checks in optimization algorithms.
 * </p>
 */
public class Threshold implements Comparable<Threshold> {
    /** The numeric value of the threshold. */
    private double value;

    /** Indicates whether the corresponding state has been explored. */
    private boolean explored;

    /**
     * Constructs a new threshold with a given value and exploration status.
     *
     * @param value the numeric threshold value
     * @param explored true if the state is explored, false otherwise
     */
    public Threshold(int value, boolean explored) {
        this.value = value;
        this.explored = explored;
    }
    /**
     * Returns the numeric value of this threshold.
     *
     * @return the threshold value
     */
    public double getValue() {
        return value;
    }
    /**
     * Returns whether the state has been explored.
     *
     * @return true if explored, false otherwise
     */
    public boolean getExplored() {
        return this.explored;
    }
    /**
     * Returns whether the state has been explored.
     * <p>
     * Alias for {@link #getExplored()}.
     * </p>
     *
     * @return true if explored, false otherwise
     */
    public boolean isExplored() {
        return explored;
    }
    /**
     * Sets the numeric value of this threshold.
     *
     * @param val the new threshold value
     */
    public void setValue(double val) {
        this.value = val;
    }
    /**
     * Sets the exploration status of the state.
     *
     * @param expl true if the state has been explored, false otherwise
     */
    public void setExplored(boolean expl) {
        this.explored = expl;
    }
    /**
     * Compares this threshold to another threshold.
     * <p>
     * Thresholds are compared first by value (ascending), and then by exploration status (false then true)
     * if the values are equal.
     * </p>
     *
     * @param other the threshold to compare with
     * @return a negative integer, zero, or a positive integer as this threshold
     *         is less than, equal to, or greater than the specified threshold
     */
    @Override
    public int compareTo(Threshold other) {
        if (this.value != other.value) {
            return Double.compare(this.value, other.value);
        } else {
            return Boolean.compare(this.explored, other.explored);
        }
    }
    /**
     * Returns a string representation of this threshold.
     *
     * @return a string in the format "Threshold [value=..., explored=...]"
     */
    @Override
    public String toString() {
        return "Threshold [value=" + value + ", explored=" + explored + "]";
    }
}
