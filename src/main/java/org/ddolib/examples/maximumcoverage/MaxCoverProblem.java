package org.ddolib.examples.maximumcoverage;

import org.ddolib.ddo.core.Decision;
import org.ddolib.examples.smic.SMICState;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import javax.xml.XMLConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        this.optimal = Optional.empty();
    }

    public String instanceFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d%n%d%n%d%n", nbItems, nbSubSets, nbSubSetsToChoose));
        optimal.ifPresent(aDouble -> sb.append(String.format("%d%n", (int) Math.ceil(aDouble))));
        sb.append("\n");
        for (int i = 0; i < nbSubSets; i++) {
            for (int j = subSets[i].nextSetBit(0); j >= 0; j = subSets[i].nextSetBit(j + 1)) {
                sb.append(String.format("%d ", j));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal.map(x -> -x );
    }


    @Override
    public int nbVars() {
        return nbSubSetsToChoose;
    }

    @Override
    public MaxCoverState initialState() {
        return new MaxCoverState(new BitSet(nbItems));
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(MaxCoverState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        for (int i = 0; i < nbSubSets; i++) {
            BitSet ss = (BitSet) subSets[i].clone();
            if (!isInclude(ss, state.coveredItems())) {
                domain.add(i);
            }
        }
        return domain.iterator();
    }

    @Override
    public MaxCoverState transition(MaxCoverState state, Decision decision) {
        int val = decision.val();
        BitSet coveredItems = (BitSet) state.coveredItems().clone();
        coveredItems.or(subSets[val]);
        return new MaxCoverState(coveredItems);
    }

    @Override
    public double transitionCost(MaxCoverState state, Decision decision) {
        int val = decision.val();
        BitSet coveredItems = (BitSet) state.coveredItems().clone();
        coveredItems.or(subSets[val]);
        coveredItems.andNot(state.coveredItems());
        return -coveredItems.cardinality();
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars()));
        }
        BitSet coveredItems = new BitSet(nbItems);
        for (int selected: solution) {
            coveredItems.or(subSets[selected]);
        }

        return -coveredItems.cardinality();
    }

    private boolean isInclude(BitSet A, BitSet B) {
        BitSet temp = (BitSet) A.clone();
        temp.andNot(B);
        return temp.isEmpty();
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
                minDistance = dist;
                min = i;
            }
        }
        return min;
    }

    

    @Override
    public String toString() {
        return name + " " + nbItems + " " + nbSubSets + " " + nbSubSetsToChoose + " " + Arrays.toString(subSets);
    }

    /**
     * Load the MaxCoverProblem from a file
     * @param fname the path to the file describing the instance
     * @return a MaxCoverProblem representing the instance
     * @throws IOException if the file cannot be found or is not readable
     */
    public MaxCoverProblem(final String fname) throws IOException{
        final File f = new File(fname);
        int context = 0;
        int nElem = 0;
        int nSet = 0;
        int budget = 0;
        Optional<Double> optimal = Optional.empty();
        BitSet[] sets = null;
        int setCount = 0;
        String s;
        try (final BufferedReader br = new BufferedReader(new FileReader(f))) {
            while ((s = br.readLine()) != null) {
                if (context == 0) {
                    context++;

                    String[] tokens = s.split("\\s");
                    nElem = Integer.parseInt(tokens[0]);

                } else if (context == 1) {
                   context++;

                    String[] tokens = s.split("\\s");
                    nSet = Integer.parseInt(tokens[0]);
                    sets = new BitSet[nSet];

                } else if (context == 2) {
                    context++;
                    String[] tokens = s.split("\\s");
                    budget  = Integer.parseInt(tokens[0]);
                } else if (context == 3) {
                    context++;
                    if (!s.isBlank()) {
                        String[] tokens = s.split("\\s");
                        optimal = Optional.of(Double.parseDouble(tokens[0]));
                    }
                }
                else {
                    if (setCount< nSet) {
                        if (!s.isBlank()) {
                            String[] tokens = s.split("\\s");

                            sets[setCount] = new BitSet(nElem);
                            for (String token : tokens) {
                                sets[setCount].set(Integer.parseInt(token));
                            }
                            setCount++;
                        }
                    }
                }
            }
        }
        this.name = Optional.of("maxCoverage_" + nElem + "_" + nSet + "_" + budget);
        this.nbItems = nElem;
        this.nbSubSets = nSet;
        this.nbSubSetsToChoose = budget;
        this.subSets = sets;
        this.optimal = optimal;
    }
}





