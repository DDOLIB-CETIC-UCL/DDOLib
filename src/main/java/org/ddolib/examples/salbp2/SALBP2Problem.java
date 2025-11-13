package org.ddolib.examples.salbp2;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SALBP2Problem implements Problem<SALBP2State> {
    public final int nbTasks;
    public final int nbStations;
    public final int[] durations;
    public final BitSet[] predecessors;
    public final BitSet[] successors;
    public final Optional<String> name;
    public Optional<Double> optimal;
    public SALBP2Problem(Optional<String> name, int nbTasks, int nbStations, int[] durations, BitSet[] predecessors, BitSet[] successors, Optional<Double> optimal) {
        this.name = name;
        this.nbTasks = nbTasks;
        this.nbStations = nbStations;
        this.durations = durations;
        this.predecessors = predecessors;
        this.successors = successors;
        this.optimal = optimal;
    }
    public SALBP2Problem(int nbTasks, int nbStations, int[] durations, BitSet[] predecessors, BitSet[] successors, Optional<Double> optimal) {
        this.name = Optional.empty();
        this.nbTasks = nbTasks;
        this.nbStations = nbStations;
        this.durations = durations;
        this.predecessors = predecessors;
        this.successors = successors;
        this.optimal = optimal;
    }
    public SALBP2Problem(int nbTasks, int nbStations, int[] durations, BitSet[] predecessors, BitSet[] successors) {
        this.name = Optional.empty();
        this.nbTasks = nbTasks;
        this.nbStations = nbStations;
        this.durations = durations;
        this.predecessors = predecessors;
        this.successors = successors;
        this.optimal = Optional.empty();
    }

    public SALBP2Problem(final String file) throws IOException {
        Scanner scanner = new Scanner(new File(file));
        scanner.nextLine();
        int n = scanner.nextInt();
        scanner.nextLine();
        scanner.nextLine();
        scanner.nextLine();
        int m = scanner.nextInt();
        int[] durations = new int[n];
        BitSet[] predecessors = new BitSet[n];
        BitSet[] successors = new BitSet[n];
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
        this.nbStations = m;
    }

    @Override
    public int nbVars() {return nbTasks;}

    @Override
    public double initialValue() {return 0;}

    @Override
    public SALBP2State initialState() {
        BitSet[] stations = new BitSet[nbStations];
        for (int i = 0; i < nbStations; i++) {
            stations[i] = new BitSet(nbTasks);
        }
        double[] cyclePerStation = new double[nbStations];
        return new SALBP2State(stations, cyclePerStation, 0);
    }

    @Override
    public Iterator<Integer> domain(SALBP2State state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        for (int i = 0; i < nbStations; i++) {
            if (isIncluded(predecessors[var], state.stations()[i])) {
                domain.add(i);
            }
        }
//        System.out.println(Arrays.toString(domain.toArray()));
        return domain.iterator();
    }

    @Override
    public SALBP2State transition(SALBP2State state, Decision decision) {
        int val = decision.val();
        int var = decision.var();
        BitSet[] stations = new BitSet[nbStations];
        double[] cyclePerStation = new double[nbStations];
        for (int i = 0; i < nbStations; i++) {
            stations[i] = (BitSet) state.stations()[i].clone();
            cyclePerStation[i] = state.cyclePerStation()[i];
        }
        stations[val].set(var, true);
        cyclePerStation[val] += durations[var];
        double cycle = Math.max(cyclePerStation[val], state.cycle());
        return new SALBP2State(stations, cyclePerStation, cycle);
    }

    @Override
    public double transitionCost(SALBP2State state, Decision decision) {
        int val = decision.val();
        int var = decision.var();
        double varCycle = state.cyclePerStation()[val] + durations[var];
        return (varCycle > state.cycle()) ? (varCycle - state.cycle()) : 0;
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
        return name + " , " + nbTasks + " , " + nbStations + " , " + Arrays.toString(durations) + " , " + Arrays.toString(predecessors);
    }
}
