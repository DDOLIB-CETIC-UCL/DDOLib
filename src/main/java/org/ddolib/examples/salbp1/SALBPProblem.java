package org.ddolib.examples.salbp1;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SALBPProblem implements Problem<SALBPState> {
    public final int nbTasks;
    public final int cycleTime;
    public final int[] durations;
    public final BitSet[] predecessors;
    public final BitSet[] successors;
    public final Optional<String> name;
    public Optional<Double> optimal;
    public SALBPProblem(Optional<String> name, int nbTasks, int cycleTime, int[] durations, BitSet[] predecessors, BitSet[] successors, Optional<Double> optimal) {
        this.name = name;
        this.nbTasks = nbTasks;
        this.cycleTime = cycleTime;
        this.durations = durations;
        this.predecessors = predecessors;
        this.successors = successors;
        this.optimal = optimal;
    }

    public SALBPProblem(int nbTasks, int cycleTime, int[] durations, BitSet[] predecessors, BitSet[] successors, Optional<Double> optimal) {
        this.name = Optional.empty();
        this.nbTasks = nbTasks;
        this.cycleTime = cycleTime;
        this.durations = durations;
        this.predecessors = predecessors;
        this.successors = successors;
        this.optimal = optimal;
    }
    public SALBPProblem(int nbTasks, int cycleTime, int[] durations, BitSet[] predecessors, BitSet[] successors) {
        this.name = Optional.empty();
        this.nbTasks = nbTasks;
        this.cycleTime = cycleTime;
        this.durations = durations;
        this.predecessors = predecessors;
        this.successors = successors;
        this.optimal = Optional.empty();
    }

    public SALBPProblem(final String file) throws IOException {

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
    public SALBPState initialState() {
        BitSet remainingTasks = new BitSet(nbTasks);
        for (int i = 0; i < nbTasks; i++) {
            remainingTasks.set(i);
        }
        BitSet currentStation = new BitSet(nbTasks);
        return new SALBPState(remainingTasks, currentStation, cycleTime);
    }

    @Override
    public Iterator<Integer> domain(SALBPState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        BitSet scheduledTasks = (BitSet) state.remainingTasks().clone();
        scheduledTasks.flip(0, nbTasks-1);
        BitSet remainingTasks = (BitSet) state.remainingTasks().clone();
        for (int i = remainingTasks.nextSetBit(0); i >= 0; i = remainingTasks.nextSetBit(i+1)) {
            if (isIncluded(predecessors[i], scheduledTasks)) {
                domain.add(i);
            }
        }
        return domain.iterator();
    }

    @Override
    public SALBPState transition(SALBPState state, Decision decision) {
        int val = decision.val();
        BitSet currentStation = (BitSet) state.currentStation().clone();
        BitSet current  = new BitSet(nbTasks);
        BitSet remainingTasks = (BitSet) state.remainingTasks().clone();
        if (state.remainingDuration() >= durations[val]) {
            currentStation.set(val, true);
            remainingTasks.clear(val);
            return new SALBPState(remainingTasks, currentStation, state.remainingDuration() - durations[val]);
        } else {
            current.set(val, true);
            remainingTasks.clear(val);
            return new SALBPState(remainingTasks, current, cycleTime - durations[val]);
        }
    }

    @Override
    public double transitionCost(SALBPState state, Decision decision) {
        int val = decision.val();
        BitSet scheduledTasks = (BitSet) state.remainingTasks().clone();
        scheduledTasks.flip(0, nbTasks-1);
        if (state.remainingDuration() >= durations[val]) {
            if (scheduledTasks.cardinality() == nbTasks - 1) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return 1;
        }
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
