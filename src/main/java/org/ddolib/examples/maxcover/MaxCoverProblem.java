package org.ddolib.examples.maxcover;

import org.ddolib.ddo.core.Decision;
import org.ddolib.examples.smic.SMICState;
import org.ddolib.modeling.Problem;

import javax.xml.XMLConstants;
import java.util.*;

/**
 * "n": 20,
 *   "m": 6,
 *   "k": 2,
 *   "sets": [
 *     [1,2,3,4],
 *     [2,5,6],
 *     [3,7,8,9],
 *     [1,3,5,9],
 *     [10,11,12],
 *     [1,2,10,11,12]
 *   ]
 */

public class MaxCoverProblem implements Problem<MaxCoverState> {
    public final int nbItems;
    public final int nbSubSets;
    public final int nbSubSetsToChoose;
    public final BitSet[] subSets;
    public Optional<String> name;
    public Optional<Double> optimal;
    public MaxCoverProblem(Optional<String> name, int nbItems, int nbSubSets, int nbSubSetsToChoose, BitSet[] subSets, Optional<Double> optimal) {
        this.name = name;
        this.nbItems = nbItems;
        this.nbSubSets = nbSubSets;
        this.nbSubSetsToChoose = nbSubSetsToChoose;
        this.subSets = subSets;
        this.optimal = optimal;
    }

    public MaxCoverProblem(int nbItems, int nbSubSets, int nbSubSetsToChoose, BitSet[] subSets, Optional<Double> optimal) {
        this.name = Optional.empty();
        this.nbItems = nbItems;
        this.nbSubSets = nbSubSets;
        this.nbSubSetsToChoose = nbSubSetsToChoose;
        this.subSets = subSets;
        this.optimal = optimal;
    }

    public MaxCoverProblem(int nbItems, int nbSubSets, int nbSubSetsToChoose, BitSet[] subSets) {
        this.name = Optional.empty();
        this.nbItems = nbItems;
        this.nbSubSets = nbSubSets;
        this.nbSubSetsToChoose = nbSubSetsToChoose;
        this.subSets = subSets;
        this.optimal = Optional.empty();
    }

    public MaxCoverProblem(int n, int m, int k, double maxR) {
        Random rand = new Random();
        double[] xcoord = new double[n];
        double[] ycoord = new double[n];
        for (int i = 0; i < n; i++) {
            xcoord[i] = rand.nextDouble();
            ycoord[i] = rand.nextDouble();
        }
        BitSet[] subSets = new BitSet[m];
        for (int i = 0; i < m; i++) {
            subSets[i] = new BitSet(n);
            for (int j = 0; j < n; j++) {
                double dist = distance(xcoord, ycoord, i, j);
                if (dist <= maxR) {
                    subSets[i].set(j, true);
                }
            }
        }
        for (int i = 0; i < n; i++) {
            if (!isContained(subSets, i)) {
                int f = minItems(xcoord, ycoord, subSets, i);
                subSets[f].set(i, true);
            }
        }
        this.name = Optional.of("maxCoverage_" + n + "_" + m + "_" + k);
        this.nbItems = n;
        this.nbSubSets = m;
        this.nbSubSetsToChoose = k;
        this.subSets = subSets;
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public String toString() {
        return name.get();
    }

    @Override
    public int nbVars() {
        return nbSubSetsToChoose;
    }

    @Override
    public MaxCoverState initialState() {
        BitSet coveredItems = new BitSet(nbItems);
        return new MaxCoverState(coveredItems);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(MaxCoverState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        for (int i = 0; i < nbSubSets; i++) {
            if (!isInclude(subSets[i], state.coveredItems())) {
                domain.add(i);
            }
        }
        return domain.iterator();
    }

    @Override
    public MaxCoverState transition(MaxCoverState state, Decision decision) {
        int val = decision.val();
        BitSet coveredItems = state.coveredItems();
        coveredItems.or(subSets[val]);
        return new MaxCoverState(coveredItems);
    }

    @Override
    public double transitionCost(MaxCoverState state, Decision decision) {
        int val = decision.val();
        BitSet coveredItems = state.coveredItems();
        coveredItems.or(subSets[val]);
        coveredItems.andNot(state.coveredItems());
        return coveredItems.cardinality();
    }

    private boolean isInclude(BitSet A, BitSet B) {
        BitSet temp = (BitSet) A.clone();
        temp.andNot(B);
        if (temp.isEmpty()) {
            return true;
        }
        return false;
    }

    private double distance(double[] x, double[] y, int i, int j) {
        double dx = x[j] - x[i];
        double dy = y[j] - y[i];
        return Math.sqrt(dx * dx + dy * dy);
    }

    private boolean isContained(BitSet[] subSets, int j) {
        for (int i = 0; i < subSets.length; i++) {
            if (subSets[i].get(j)) {
                return true;
            }
        }
        return false;
    }

    private int minItems(double[] x, double[] y, BitSet[] subSets, int j) {
        int min = -1;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < subSets.length; i++) {
            double dist = distance(x, y, i, j);
            if (dist < minDistance) {
                min = i;
            }
        }
        return min;
    }
}





