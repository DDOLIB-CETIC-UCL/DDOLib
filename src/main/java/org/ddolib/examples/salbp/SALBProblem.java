package org.ddolib.examples.salbp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.util.*;

public class SALBProblem implements Problem<SALBPState> {

    int nbTasks;
    int cycleTime;
    int[] tasksTime;
    BitSet[] allPrecedences;
    // Optimal solution
    Optional<Double> optimal;
    Optional<String> name;

    SALBProblem(int nbTasks, int cycleTime, int[] tasksTime, BitSet[] precedences, Optional<Double> optimal) {
        this.nbTasks = nbTasks;
        this.cycleTime = cycleTime;
        this.tasksTime = tasksTime;
        this.allPrecedences = precedences;
        this.optimal = optimal;
        System.out.println("xxxxxxxxxxxx");
        System.out.println(allPrecedences[19]);
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        BitSet remainingTasks = new BitSet(nbTasks);
        remainingTasks.set(0, nbTasks);
        int currentStationRemainingTime = cycleTime;
        int stations = 1;
        for (int task : solution) {
            BitSet precedence = (BitSet) allPrecedences[task].clone();
            precedence.and(remainingTasks);
            if (!precedence.isEmpty())
                throw new InvalidSolutionException(String.format(
                        "Trying to insert %d but precedences %s are not inserted yet", task, precedence));
            int weight = tasksTime[task];
            if (currentStationRemainingTime < weight) {
                currentStationRemainingTime = cycleTime - weight;
                stations++;
            } else {
                currentStationRemainingTime -= weight;
            }
            remainingTasks.clear(task);
        }
        return stations;
    }

    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public int nbVars() {
        return nbTasks;
    }

    @Override
    public SALBPState initialState() {
        BitSet remainingItems = new BitSet(nbTasks);
        remainingItems.set(0, nbTasks);
        return new SALBPState(cycleTime, remainingItems);
    }

    @Override
    public double initialValue() {
        // Starting with one opened bin.
        return 1;
    }

    @Override
    public Iterator<Integer> domain(SALBPState state, int var) {
        if (var >= nbVars()) return Collections.emptyIterator();

        int nextItem = state.remainingTasks().nextSetBit(0);
        HashSet<Integer> allItems = new HashSet<>();
        HashSet<Integer> fittingItems = new HashSet<>();

        while (nextItem != -1) {
            BitSet precedence = (BitSet) allPrecedences[nextItem].clone();
            precedence.and(state.remainingTasks());
            if (precedence.isEmpty()) {
                if (tasksTime[nextItem] <= state.currentStationRemainingTime())
                    fittingItems.add(nextItem);
                allItems.add(nextItem);
            }
            nextItem = state.remainingTasks().nextSetBit(nextItem + 1);
        }

        if (!fittingItems.isEmpty()) return fittingItems.iterator();
        return allItems.iterator();
    }

    @Override
    public SALBPState transition(SALBPState state, Decision decision) {
        int task = decision.value();
        int taskTime = tasksTime[task];
        boolean stationFull = state.currentStationRemainingTime() - taskTime < 0;

        int currentStationRemainingTime = stationFull ? cycleTime - taskTime : state.currentStationRemainingTime() - taskTime;
        BitSet remainingTasks = (BitSet) state.remainingTasks().clone();
        remainingTasks.set(task, false);

        return new SALBPState(currentStationRemainingTime, remainingTasks);
    }

    @Override
    public double transitionCost(SALBPState state, Decision decision) {
        int item = decision.value();
        if (state.currentStationRemainingTime() < tasksTime[item]) return 1;
        else return 0;
    }

    @Override
    public String toString() {
        return name.orElse("No name");
    }

    public String solutionToString(int[] solution) {
        int currentStationRemainingTime = cycleTime;
        StringBuilder gsb = new StringBuilder();
        StringBuilder tsb = new StringBuilder();
        int currentStation = 1;
        gsb.append("Station #").append(currentStation);
        for (int item : solution) {
            int weight = tasksTime[item];
            if (currentStationRemainingTime < weight) {
                gsb.append(" - Total duration ").append(cycleTime - currentStationRemainingTime).append(" -> \n").append(tsb).append("\nStation #").append(currentStation+1);
                tsb = new StringBuilder();
                currentStationRemainingTime = cycleTime - weight;
                currentStation++;
            } else {
                currentStationRemainingTime -= weight;
            }
            tsb.append("\t|\tTask_").append(item).append(" (duration:").append(weight).append(") ");
        }
        gsb.append(" - Total duration ").append(cycleTime - currentStationRemainingTime).append(" -> ").append(tsb);
        return gsb.toString();
    }
}

