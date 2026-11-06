package org.ddolib.examples.layered.misp;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.SimpleDominanceChecker;

/**
 * Dominance checker of the Maximum Independent Set Problem (MISP), for the {@link MispDominance}
 * relation: a state is dominated by a state with a better or equal value whose set of remaining
 * vertices includes its own.
 *
 * <p>The dominance by inclusion forbids grouping the states by a key (see {@link
 * MispDominance#getKey}), so that a {@link SimpleDominanceChecker} compares each new state with
 * all the states of its layer. This checker groups the states of a layer by their number of
 * remaining vertices instead: a set can only be included in a set with at least as many
 * vertices. A new state is thus only compared with the states having at least as many remaining
 * vertices to know if it is dominated, and with the states having at most as many remaining
 * vertices to remove the ones it dominates.
 *
 * <p>It prunes exactly the same states as a {@link SimpleDominanceChecker} with a {@link
 * MispDominance}. The states are not copied: they must not be modified after their creation,
 * which is the case of the states built by {@link MispProblem#transition}.
 */
public class MispDominanceChecker extends DominanceChecker<BitSet> {

    /** For each depth, the non-dominated states indexed by their number of remaining vertices. */
    private final List<TreeMap<Integer, List<Entry>>> fronts;

    /**
     * Creates a dominance checker for a problem with the given number of variables.
     *
     * @param nbVars the number of variables of the problem
     */
    public MispDominanceChecker(int nbVars) {
        super(new MispDominance());
        this.fronts = new ArrayList<>(nbVars + 1);
        for (int i = 0; i <= nbVars; i++) {
            fronts.add(new TreeMap<>());
        }
    }

    @Override
    public boolean updateDominance(BitSet state, int depth, double objValue) {
        TreeMap<Integer, List<Entry>> front = fronts.get(depth);
        int card = state.cardinality();
        // only a state with at least as many remaining vertices can dominate the new one
        for (List<Entry> entries : front.tailMap(card, true).values()) {
            for (Entry e : entries) {
                if (e.value <= objValue && dominance.isDominatedOrEqual(state, e.state)) {
                    return true;
                }
            }
        }
        // the new state can only dominate the states with at most as many remaining vertices
        for (List<Entry> entries : front.headMap(card, true).values()) {
            entries.removeIf(
                    e -> objValue <= e.value && dominance.isDominatedOrEqual(e.state, state));
        }
        front.computeIfAbsent(card, k -> new ArrayList<>()).add(new Entry(state, objValue));
        return false;
    }

    @Override
    public void clear() {
        for (Map<Integer, List<Entry>> front : fronts) {
            front.clear();
        }
    }

    /** A non-dominated state and its value. */
    private record Entry(BitSet state, double value) {}
}
