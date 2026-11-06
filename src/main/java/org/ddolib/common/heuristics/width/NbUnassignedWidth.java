package org.ddolib.common.heuristics.width;

/**
 * Width heuristic giving to the decision diagrams compiled from a subproblem a maximum width equal
 * to its number of unassigned variables, i.e. the number of variables minus the depth of the
 * subproblem (at least 1). It is the default width heuristic of ddo ({@code NbUnassignedWidth}):
 * the diagrams compiled close to the root are wide, those compiled deep in the search are narrow,
 * where they are cheap to compile exactly.
 *
 * <p>The width can be scaled by a constant factor, like the {@code Times} heuristic of ddo.
 *
 * @param <T> the type of the states
 */
public class NbUnassignedWidth<T> implements WidthHeuristic<T> {

    private final int nbVars;

    private final int factor;

    /**
     * Creates a width heuristic equal to the number of unassigned variables.
     *
     * @param nbVars the number of variables of the problem
     */
    public NbUnassignedWidth(int nbVars) {
        this(nbVars, 1);
    }

    /**
     * Creates a width heuristic equal to the number of unassigned variables times a factor.
     *
     * @param nbVars the number of variables of the problem
     * @param factor the factor applied to the number of unassigned variables (at least 1)
     * @throws IllegalArgumentException if {@code nbVars} is negative or {@code factor < 1}
     */
    public NbUnassignedWidth(int nbVars, int factor) {
        if (nbVars < 0 || factor < 1) {
            throw new IllegalArgumentException(
                    "nbVars must be non-negative and factor at least 1, got "
                            + nbVars
                            + " and "
                            + factor);
        }
        this.nbVars = nbVars;
        this.factor = factor;
    }

    /**
     * Returns the width at the root, where no variable is assigned: the number of variables (times
     * the factor). The solvers call {@link #maximumWidth(Object, int)} instead.
     *
     * @param state the state of the subproblem
     * @return the width at the root
     */
    @Override
    public int maximumWidth(T state) {
        return maximumWidth(state, 0);
    }

    @Override
    public int maximumWidth(T state, int depth) {
        long width = (long) Math.max(1, nbVars - depth) * factor;
        return (int) Math.min(Integer.MAX_VALUE, width);
    }

    @Override
    public String toString() {
        return factor == 1
                ? "NbUnassignedWidth(" + nbVars + ")"
                : "NbUnassignedWidth(" + nbVars + ", x" + factor + ")";
    }
}
