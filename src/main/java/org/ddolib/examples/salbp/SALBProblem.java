package org.ddolib.examples.salbp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.examples.salbp1.SALBPState;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SALBProblem implements Problem<SALBState> {
    public final int nbTasks;
    public final int cycleTime;
    public final int[] durations;
    public final BitSet[] predecessors;
    public final BitSet[] successors;
    public final Optional<String> name;
    public Optional<Double> optimal;
    public SALBProblem(Optional<String> name, int nbTasks, int cycleTime, int[] durations, BitSet[] predecessors, BitSet[] successors, Optional<Double> optimal) {
        this.name = name;
        this.nbTasks = nbTasks;
        this.cycleTime = cycleTime;
        this.durations = durations;
        this.predecessors = predecessors;
        this.successors = successors;
        this.optimal = optimal;
    }

    public SALBProblem(int nbTasks, int cycleTime, int[] durations, BitSet[] predecessors, BitSet[] successors, Optional<Double> optimal) {
        this.name = Optional.empty();
        this.nbTasks = nbTasks;
        this.cycleTime = cycleTime;
        this.durations = durations;
        this.predecessors = predecessors;
        this.successors = successors;
        this.optimal = optimal;
    }
    public SALBProblem(int nbTasks, int cycleTime, int[] durations, BitSet[] predecessors, BitSet[] successors) {
        this.name = Optional.empty();
        this.nbTasks = nbTasks;
        this.cycleTime = cycleTime;
        this.durations = durations;
        this.predecessors = predecessors;
        this.successors = successors;
        this.optimal = Optional.empty();
    }

    public SALBProblem(final String file) throws IOException {

        Scanner scanner = new Scanner(new File(file));
        scanner.nextLine();
        int n = scanner.nextInt();
        scanner.nextLine();
        scanner.nextLine();
        scanner.nextLine();
        int cycle = scanner.nextInt();
        int[] durations = new int[n];
        BitSet[] predecessors = new BitSet[n];
        BitSet[] successors = new BitSet[n];
        scanner.nextLine();
        scanner.nextLine();
        scanner.nextLine();
        scanner.nextLine();
        scanner.nextLine();
        scanner.nextLine();
        scanner.nextLine();
        for (int i = 0; i < n; i++) {
            String[] line = scanner.useDelimiter("\\s").nextLine().split(" ");
            durations[i] = Integer.parseInt(line[1]);
            predecessors[i] = new BitSet(n);
            successors[i] = new BitSet(n);
        }
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String[] line = scanner.useDelimiter("\\s").nextLine().split(",");
            if (line.length == 2) {
                int a = Integer.parseInt(line[0]);
                int b = Integer.parseInt(line[1]);
                predecessors[b-1].set(a-1, true);
                successors[a-1].set(b-1, true);
            } else {
                break;
            }
        }
        scanner.close();
        this.name = Optional.of(file);
        this.nbTasks = n;
        this.durations = durations;
        this.predecessors = predecessors;
        this.successors = successors;
        this.cycleTime = cycle;
    }

    @Override
    public int nbVars() {return nbTasks;}

    @Override
    public double initialValue() {return 0;}

    @Override
    public SALBState initialState() {
        BitSet[] stations = new BitSet[nbTasks];
        double[] remainingDurationPerStation = new double[nbTasks];
        double minRemainingDuration = Double.POSITIVE_INFINITY;
        for (int i = 0; i < nbTasks; i++) {
            stations[i] = new BitSet(nbTasks);
            remainingDurationPerStation[i] = cycleTime;
            minRemainingDuration = Math.min(minRemainingDuration, durations[i]);
        }
        return new SALBState(stations, remainingDurationPerStation);
    }

    @Override
    public Iterator<Integer> domain(SALBState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        BitSet bs = new BitSet(nbTasks);
        for (int i = 0; i < nbTasks; i++) {
            bs.or(state.stations()[i]);
            if (isIncluded(predecessors[var], bs) && state.remainingDurationPerStation()[i] >= durations[var]) {
                domain.add(i);
            }
        }
        return domain.iterator();
    }

    @Override
    public SALBState transition(SALBState state, Decision decision) {
        int val = decision.val();
        int var = decision.var();
        BitSet bt = (BitSet) state.stations()[val].clone();
        bt.set(var, true);
        BitSet[] stations = new BitSet[nbTasks];
        double[] remainingDurationPerStation = new double[nbTasks];
        for (int i = 0; i < nbTasks; i++) {
            if (i != val) {
                stations[i] = (BitSet) state.stations()[i].clone();
                remainingDurationPerStation[i] = state.remainingDurationPerStation()[i];
            } else {
                stations[i] = (BitSet) bt.clone();
                remainingDurationPerStation[i] = state.remainingDurationPerStation()[i] - durations[var];
            }
        }
        return new SALBState(stations, remainingDurationPerStation);
    }

    @Override
    public double transitionCost(SALBState state, Decision decision) {
        int val = decision.val();
        int var = decision.var();
        BitSet remainingTasks = new BitSet();
        double remaining = state.remainingDurationPerStation()[val];
        double minRemainingDuration = Double.POSITIVE_INFINITY;
        double cost = 0;
        for (int i = 0; i < nbTasks; i++) {
            remainingTasks.or(state.stations()[i]);
            if (i == val)
                remaining = state.remainingDurationPerStation()[i] - durations[var];
        }
        remainingTasks.flip(0, nbTasks-1);
        remainingTasks.clear(var);
        for (int i = remainingTasks.nextSetBit(0); i >= 0; i = remainingTasks.nextSetBit(i+1)) {
            minRemainingDuration = Math.min(minRemainingDuration, durations[i]);
        }
        for (int i = 0; i < nbTasks; i++) {
            if (i != val) {
                if (state.remainingDurationPerStation()[i] < minRemainingDuration)
                    cost++;
            } else {
                if (remaining < minRemainingDuration) {
                    cost++;
                }
            }
        }
        return cost;
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars()));
        }
        return 0;
    }

    private boolean isIncluded(BitSet A, BitSet B) {
        BitSet clone = (BitSet) A.clone();
        clone.andNot(B);
        return clone.isEmpty();
    }
    @Override
    public String toString() {
        return name + " , " + nbTasks + " , " + cycleTime + " , " + Arrays.toString(durations) + " , " + Arrays.toString(predecessors);
    }
}
