package org.ddolib.examples.alp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.*;

/**
 * The definition of the ALP.
 * <p>
 * The Aircraft Landing Problem consist in optimizing the landing plan of a fleet of aircraft.
 * Each aircraft must land before a defined time (deadline) and as close as possible to its target time.
 * </p>
 * <p>
 * The aircraft land on one or several runways. The landing time is defined by the landing time
 * of the previous aircraft and the class of both aircraft. There is a small delay between two landing,
 * which is defined by the class of the two aircraft.
 * </p>
 */
public class ALPProblem implements Problem<ALPState> {
    public ALPInstance instance;
    // Used to know which aircraft of each class will be next to land.
    public ArrayList<ArrayList<Integer>> latestToEarliestAircraftByClass;
    // Minimal time between a "no_class" aircraft and "class" aircraft.
    int[] minSeparationTo;

    private Optional<String> name = Optional.empty();

    // When no plane has yet landed the previous class is -1.
    public static final int DUMMY = -1;

    /**
     * Instantiates an ALPProblem and initiates some other values.
     *
     * @param instance The ALP instance.
     */
    public ALPProblem(ALPInstance instance) {
        this.instance = instance;
        latestToEarliestAircraftByClass = new ArrayList<>();
        minSeparationTo = new int[instance.nbClasses];
        Arrays.fill(minSeparationTo, Integer.MAX_VALUE);

        for (int i = 0; i < instance.nbClasses; i++)
            latestToEarliestAircraftByClass.add(new ArrayList<>(List.of(0)));

        for (int i = instance.nbAircraft - 1; i >= 0; i--) {
            latestToEarliestAircraftByClass.get(instance.aircraftClass[i]).add(i);
        }

        for (int i = 0; i < instance.nbClasses; i++) {
            for (int j = 0; j < instance.nbClasses; j++) {
                minSeparationTo[j] = Math.min(minSeparationTo[j], instance.classTransitionCost[i][j]);
            }
        }
    }

    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public Optional<Double> optimalValue() {
        return instance.optimal;
    }

    /**
     * Returns the arrival time of an aircraft based on runway state.
     *
     * @param runwayStates The array of runway states.
     * @param aircraft     The aircraft for which we want to know the arrival time.
     * @param runway       The runway on which the aircraft is supposed to land.
     * @return The arrival (landing) time.
     */
    public int getArrivalTime(RunwayState[] runwayStates, int aircraft, int runway) {
        if (runwayStates[runway].prevClass == DUMMY) {
            if (runwayStates[runway].prevTime == 0)
                return instance.aircraftTarget[aircraft];
            else
                return Math.max(instance.aircraftTarget[aircraft],
                        runwayStates[runway].prevTime + minSeparationTo[instance.aircraftClass[aircraft]]);
        } else {
            return Math.max(instance.aircraftTarget[aircraft],
                    runwayStates[runway].prevTime +
                            instance.classTransitionCost[runwayStates[runway].prevClass][instance.aircraftClass[aircraft]]);
        }
    }

    /**
     * Formats the ALPDecision as an Integer.
     *
     * @param decision The decision.
     * @return The formated decision.
     */
    public int toDecision(ALPDecision decision) {
        return decision.aircraftClass + instance.nbClasses * decision.runway;
    }

    /**
     * Restores a decision object from its Integer form.
     *
     * @param value THe formatted Integer form of the decision.
     * @return An ALPDecision object.
     */
    public ALPDecision fromDecision(int value) {
        return new ALPDecision(
                value % instance.nbClasses,
                value / instance.nbClasses
        );
    }

    @Override
    public int nbVars() {
        return instance.nbAircraft;
    }

    @Override
    public ALPState initialState() {
        int[] remaining = new int[instance.nbClasses];
        Arrays.fill(remaining, 0);
        for (int i = 0; i < instance.nbAircraft; i++)
            remaining[instance.aircraftClass[i]] += 1;
        RunwayState[] runwayStates = new RunwayState[instance.nbRunways];
        Arrays.fill(runwayStates, new RunwayState(DUMMY, 0));
        return new ALPState(remaining, runwayStates);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(ALPState state, int var) {
        int totRemaining = 0;
        HashSet<RunwayState> used = new HashSet<>();
        ArrayList<Integer> decisions = new ArrayList<>();
        int[] remainingAircraftOfClass = state.remainingAircraftOfClass;

        for (int c = 0; c < remainingAircraftOfClass.length; c++) {       // For each class
            if (remainingAircraftOfClass[c] > 0) {                         // If there still are aircraft to land.
                // Get the earliest aircraft in the queue.
                int aircraft = latestToEarliestAircraftByClass.get(c).get(state.remainingAircraftOfClass[c]);

                used.clear();
                // For each runway, try to find at least one suitable runway.
                for (int runway = 0; runway < instance.nbRunways; runway++) {
                    int arrival = getArrivalTime(state.runwayStates, aircraft, runway);
                    if (arrival <= instance.aircraftDeadline[aircraft]) {
                        decisions.add(toDecision(new ALPDecision(c, runway)));
                        used.add(state.runwayStates[runway]);
                    }
                }

                if (used.isEmpty()) {
                    // This aircraft will never be able to land.
                    return Collections.emptyIterator();
                }
            }
            totRemaining += remainingAircraftOfClass[c];
        }

        if (totRemaining == 0) {
            return Collections.singletonList(DUMMY).iterator();
        } else {
            return decisions.iterator();
        }
    }

    @Override
    public ALPState transition(ALPState state, Decision decision) {
        if (decision.val() == DUMMY) {
            // Latest decision says that there are no plane to land left.
            return new ALPState(state);
        } else {
            // Generating the new state.
            ALPDecision alpDecision = fromDecision(decision.val());
            int aircraftClass = alpDecision.aircraftClass;
            int runway = alpDecision.runway;
            int aircraft = latestToEarliestAircraftByClass.get(aircraftClass).get(state.remainingAircraftOfClass[aircraftClass]);
            ALPState nextState = new ALPState(state);
            nextState.remainingAircraftOfClass[aircraftClass] -= 1;
            nextState.runwayStates[runway].prevClass = aircraftClass;
            nextState.runwayStates[runway].prevTime = getArrivalTime(state.runwayStates, aircraft, runway);

            Arrays.sort(nextState.runwayStates);
            return nextState;
        }
    }

    @Override
    public double transitionCost(ALPState state, Decision decision) {
        // The delta between the arrival time and the earliest arrival time.
        if (decision.val() == DUMMY) {
            return 0;
        } else {
            ALPDecision alpDecision = fromDecision(decision.val());
            int aircraftClass = alpDecision.aircraftClass;
            int aircraft = latestToEarliestAircraftByClass.get(aircraftClass).get(state.remainingAircraftOfClass[aircraftClass]);
            return getArrivalTime(state.runwayStates, aircraft, alpDecision.runway) - instance.aircraftTarget[aircraft];
        }
    }

    @Override
    public String toString() {
        String out = String.format("ALP problem with %d aircrafts, %d classes and %d runways",
                instance.nbAircraft, instance.nbClasses, instance.nbRunways);
        return name.orElse(out);
    }
}


