package org.ddolib.ddo.examples.srflp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.*;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

/**
 * Implementation of the relaxation for the SRFLP.
 */
public class SRFLPRelax implements Relaxation<SRFLPState> {

    private final SRFLPProblem problem;

    /**
     * Pairs of departments sorted by their flow in decreasing order.
     */
    private final ArrayList<PairAndFlow> pairsSortedByFlow = new ArrayList<>();
    /**
     * Departments sorted by their length in increasing order.
     */
    private final ArrayList<DepartmentAndLength> departmentsSortedByLength = new ArrayList<>();

    /**
     * Constructs a new instance of a relaxation for the SRFLP.
     *
     * @param problem The problem instance that must be solved.
     */
    public SRFLPRelax(SRFLPProblem problem) {
        this.problem = problem;

        for (int i = 0; i < problem.nbVars(); i++) {
            for (int j = i + 1; j < problem.nbVars(); j++) {
                pairsSortedByFlow.add(new PairAndFlow(i, j, problem.flows[i][j]));
            }
            departmentsSortedByLength.add(new DepartmentAndLength(i, problem.lengths[i]));
        }

        Collections.sort(pairsSortedByFlow);
        Collections.reverse(pairsSortedByFlow);
        Collections.sort(departmentsSortedByLength);
    }


    @Override
    public SRFLPState mergeStates(Iterator<SRFLPState> states) {
        BitSet mergedMust = new BitSet(problem.nbVars());
        mergedMust.set(0, problem.nbVars(), true);
        BitSet mergedMaybes = new BitSet(problem.nbVars());
        int[] mergedCut = new int[problem.nbVars()];
        Arrays.fill(mergedCut, Integer.MAX_VALUE);
        int mergedDepth = 0;

        while (states.hasNext()) {
            SRFLPState state = states.next();
            mergedMust.and(state.must());
            mergedMaybes.or(state.must());
            mergedMaybes.or(state.maybe());
            mergedDepth = max(mergedDepth, state.depth());

            for (int i = state.must().nextSetBit(0); i >= 0; i = state.must().nextSetBit(i + 1)) {
                mergedCut[i] = min(mergedCut[i], state.cut()[i]);
            }

            for (int i = state.maybe().nextSetBit(0); i >= 0; i = state.maybe().nextSetBit(i + 1)) {
                mergedCut[i] = min(mergedCut[i], state.cut()[i]);
            }
        }

        mergedMaybes.andNot(mergedMust);

        return new SRFLPState(mergedMust, mergedMaybes, mergedCut, mergedDepth);
    }

    @Override
    public int relaxEdge(SRFLPState from, SRFLPState to, SRFLPState merged, Decision d, int cost) {
        return cost;
    }

    @Override
    public int fastUpperBound(SRFLPState state, Set<Integer> variables) {
        int complete = problem.nbVars() - state.depth();
        int maxFromMaybe = complete - state.must().cardinality();
        int free = freeLB(selectLength(state, complete, maxFromMaybe), selectFlow(state, complete, maxFromMaybe),
                complete);
        int fixed = fixedLB(selectCutRatio(state, maxFromMaybe));

        return -(free + fixed);
    }

    /**
     * Computes a lower bound based on the remaining free departments. This bound is cumulatively computed, based on
     * the length of the free department (sorted in increasing order) and the flows between each pair of free
     * departments.
     *
     * @param selectedLength The department's lengths that will be used to compute the bound.
     * @param selectedFLows  The traffic flows that will be used to compute the bound.
     * @param complete       How many department must be placed to complete the solution.
     * @return A lower bound based on the remaining free departments.
     */
    private int freeLB(ArrayList<DepartmentAndLength> selectedLength,
                       ArrayList<PairAndFlow> selectedFLows, int complete) {
        int bound = 0;
        int cumulative = 0;
        int id = 0;
        for (int k = 0; k < complete - 1; k++) {
            for (int x = 0; x < complete - k - 1; x++) {
                bound += cumulative * selectedFLows.get(id).flow;
                id++;
            }
            cumulative += selectedLength.get(k).len;
        }

        return bound;
    }

    /**
     * Computes a lower bound based on the already fixed departments. This bound is cumulatively computed, based on
     * the cut (see {@link SRFLPState}) of the input state.
     *
     * @param ratios The pairs {@code (length, cut)} sorted in decreasing order according to the ration {@code cut / length} that
     *               will be used to compute the bound.
     * @return A lower bound based on the already fixed departments.
     */
    private int fixedLB(ArrayList<CutRatio> ratios) {
        int bound = 0;
        int cumulative = 0;

        for (CutRatio r : ratios) {
            bound += cumulative * r.cut;
            cumulative += r.length;
        }

        return bound;
    }

    /**
     * Selects the departments and lengths that will be used to computes the free lower bound.
     *
     * @param state        The state on which compute the lower bound.
     * @param complete     How many department must be placed to complete the solution.
     * @param maxFromMaybe The maximum number of departments that can selected.
     * @return The departments and lengths, sorted in increasing order, that will be used to compute the free lower
     * bound.
     */
    private ArrayList<DepartmentAndLength> selectLength(SRFLPState state, int complete, int maxFromMaybe) {

        ArrayList<DepartmentAndLength> selectedLengths = new ArrayList<>();

        int selectedFromMaybe = maxFromMaybe;
        for (DepartmentAndLength dl : departmentsSortedByLength) {
            if (state.must().get(dl.dep)) {
                selectedLengths.add(dl);
            } else if (selectedFromMaybe > 0 && state.maybe().get(dl.dep)) {
                selectedLengths.add(dl);
                selectedFromMaybe--;
            }

            if (selectedLengths.size() == complete) {
                break;
            }
        }

        return selectedLengths;
    }

    /**
     * Selects the pairs of department and flows that will be used to compute the free lower bound.
     *
     * @param state        The state on which compute the lower bound.
     * @param complete     How many department must be placed to complete the solution.
     * @param maxFromMaybe The maximum number of departments that can selected.
     * @return The pairs of departments and flows, sorted in decreasing order, that will be used
     * to compute the free lower bound.
     */
    private ArrayList<PairAndFlow> selectFlow(SRFLPState state, int complete, int maxFromMaybe) {
        int nbFlow = complete * (complete - 1) / 2;

        ArrayList<PairAndFlow> selectedFlows = new ArrayList<>();

        int selectedFromMustAndMaybe = state.must().cardinality() * maxFromMaybe;
        int selectedFromMaybe = maxFromMaybe * (maxFromMaybe - 1) / 2;
        for (PairAndFlow pf : pairsSortedByFlow) {
            if (state.must().get(pf.x) && state.must().get(pf.y)) {
                selectedFlows.add(pf);
            } else if (selectedFromMustAndMaybe > 0 &&
                    (state.must().get(pf.x) && state.maybe().get(pf.y)
                            || state.must().get(pf.y) && state.maybe().get(pf.x))) {
                selectedFlows.add(pf);
                selectedFromMustAndMaybe--;
            } else if (selectedFromMaybe > 0 && state.maybe().get(pf.x) && state.maybe().get(pf.y)) {
                selectedFlows.add(pf);
            }

            if (selectedFlows.size() == nbFlow) {
                break;
            }
        }

        return selectedFlows;
    }

    /**
     * Selects the cuts from the input state that will be used to compute the fixed lower bound.
     *
     * @param state        The state on which compute the lower bound.
     * @param maxFromMaybe The maximum number of departments that can selected.
     * @return The cuts from the inputs state, sorted in decreasing order, that will be used
     * to compute the fixed lower bound.
     */
    private ArrayList<CutRatio> selectCutRatio(SRFLPState state, int maxFromMaybe) {
        ArrayList<DepartmentAndLength> selectedLengthsFromMaybe = new ArrayList<>();
        int selectedFromMaybe = maxFromMaybe;
        for (DepartmentAndLength dl : departmentsSortedByLength) {
            if (selectedFromMaybe > 0 && state.maybe().get(dl.dep)) {
                selectedLengthsFromMaybe.add(dl);
                selectedFromMaybe--;
            }
        }

        ArrayList<CutRatio> selectedCutRatios = new ArrayList<>();
        for (int i = state.must().nextSetBit(0); i >= 0; i = state.must().nextSetBit(i + 1)) {
            selectedCutRatios.add(new CutRatio(problem.lengths[i], state.cut()[i]));
        }

        ArrayList<Integer> maybeCuts = new ArrayList<>();
        for (int i = state.maybe().nextSetBit(0); i >= 0; i = state.maybe().nextSetBit(i + 1)) {
            maybeCuts.add(i);
        }

        Collections.sort(maybeCuts);
        Collections.reverse(maybeCuts);

        for (int i = 0; i < maxFromMaybe; i++) {
            selectedCutRatios.add(new CutRatio(selectedLengthsFromMaybe.get(i).len, maybeCuts.get(i)));
        }

        Collections.sort(selectedCutRatios);
        Collections.reverse(selectedCutRatios);

        return selectedCutRatios;
    }


    /**
     * Contains a pair of department and their flow. It is used to sort the pairs of départment by their flow.
     *
     * @param x    The first department.
     * @param y    The seconde department.
     * @param flow The traffic flow between the first department and the second.
     */
    private record PairAndFlow(int x, int y, int flow) implements Comparable<PairAndFlow> {

        @Override
        public int compareTo(PairAndFlow o) {
            return Integer.compare(this.flow, o.flow);
        }

        @Override
        public String toString() {
            return String.format("(%d, %d, %d)", x, y, flow);
        }
    }

    /**
     * Contains the id of a department and its length. Used to sort the department by their length.
     *
     * @param dep The id of the department.
     * @param len The length of the department.
     */
    private record DepartmentAndLength(int dep, int len) implements Comparable<DepartmentAndLength> {
        @Override
        public int compareTo(DepartmentAndLength o) {
            return Integer.compare(this.len, o.len);
        }
    }

    /**
     * Contains the length and the cut associated to a state. Used to be sort following the ratio {@code cut / length}
     *
     * @param length The length associated to a state.
     * @param cut    The cut value associated to a state (see {@link SRFLPState}
     */
    private record CutRatio(int length, int cut) implements Comparable<CutRatio> {
        @Override
        public int compareTo(CutRatio o) {
            return Double.compare((double) this.cut / this.length, (double) o.cut / o.length);
        }
    }

}
