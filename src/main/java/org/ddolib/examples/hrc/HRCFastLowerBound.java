package org.ddolib.examples.hrc;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

/**
 * Admissible lower-bound heuristic for the HRC scheduling problem.
 * <p>
 * Two complementary bounds are combined:
 * <ol>
 *     <li>{@code max(tH, tR)} — the dummy variable will contribute at least this much.</li>
 *     <li>{@code (tH + tR + S) / 2} where {@code S} is the sum of the minimum
 *         per-task durations over the three modes. This bound follows from the
 *         inequality {@code T_C + max(A, B) ≥ (A + B) / 2 + T_C} and the fact
 *         that the total work across all three pools is at least {@code S}.</li>
 * </ol>
 * The returned value is the maximum of the two, which is always admissible
 * (i.e. &le; the true remaining cost).
 * </p>
 */
public class HRCFastLowerBound implements FastLowerBound<HRCState> {

    private final HRCProblem problem;

    public HRCFastLowerBound(HRCProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(HRCState state, Set<Integer> variables) {
        if (variables.isEmpty()) {
            return 0;
        }
        return Math.max(state.tH(), state.tR());

    }
}

