package org.ddolib.ddo.examples.alp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;
import java.util.stream.IntStream;


public class ALPProblem implements Problem<ALPState> {
    public ALPInstance instance;
    public ArrayList<ArrayList<Integer>> next;
    int[] minSeparationTo;

    public static final int DUMMY = -1;

    public ALPProblem(ALPInstance instance){
        this.instance = instance;
        next = new ArrayList<>();
        // Minimal time between a "no_class" aircraft and "class" aircraft.
        minSeparationTo = new int[instance.nbClasses];

        for(int i = 0; i < instance.nbClasses; i++)
            next.add(new ArrayList<>(List.of(0)));
        Arrays.fill(minSeparationTo,Integer.MAX_VALUE);

        for(int i = instance.nbAircraft-1; i >= 0; i--){
            next.get(instance.classes[i]).add(i);
        }

        for(int i = 0; i < instance.nbClasses; i++){
            for(int j = 0; j < instance.nbClasses; j++){
                minSeparationTo[j] = Math.min(minSeparationTo[j],instance.separation[i][j]);
            }
        }
    }

    /** Returns the arrival time of an aircraft based on runway state.
     *
     * @param runwayStates The array of runway states.
     * @param aircraft The aircraft for which we want to know the arrival time.
     * @param runway The runway on which the aircraft is supposed to land.
     * @return The arrival (landing) time.
     */
    public int getArrivalTime(RunwayState[] runwayStates, int aircraft, int runway){
        if(runwayStates[runway].prevTime == 0 & runwayStates[runway].prevClass == DUMMY){
            return instance.target[aircraft];
        } else if(runwayStates[runway].prevClass == DUMMY){
            return Math.max(instance.target[aircraft],
                    runwayStates[runway].prevTime + minSeparationTo[instance.classes[aircraft]]);
        } else {
            return Math.max(instance.target[aircraft],
                    runwayStates[runway].prevTime +
                            instance.separation[runwayStates[runway].prevClass][instance.classes[aircraft]]);
        }
    }

    /** Formats the ALPDecision as an Integer
     *
     * @param decision The decision.
     * @return The formated decision.
     */
    public int toDecision(ALPDecision decision){
        return decision.aircraftClass + instance.nbClasses*decision.runway;
    }

    /** Restore a decision object from its Integer form.
     *
     * @param value THe formatted Integer form of the decision.
     * @return An ALPDecision object.
     */
    public ALPDecision fromDecision(int value){
        return new ALPDecision(
                value % instance.nbClasses,
                value/instance.nbClasses
        );
    }

    @Override
    public int nbVars() {
        return instance.nbAircraft;
    }

    @Override
    public ALPState initialState() {
        int[] remaining = new int[instance.nbClasses];
        Arrays.fill(remaining,0);
        for(int i = 0; i < instance.nbAircraft; i++)
            remaining[instance.classes[i]] += 1;
        RunwayState[] runwayStates = new RunwayState[instance.nbRunways];
        Arrays.fill(runwayStates, new RunwayState(DUMMY,0));
        return new ALPState(remaining, runwayStates);
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(ALPState state, int var) {
        int totRemaining = 0;
        HashSet<RunwayState> used = new HashSet<>();
        ArrayList<Integer> decisions = new ArrayList<>();
        int[] remainingAircraftOfClass = state.remainingAircraftOfClass;

        for (int c = 0; c < remainingAircraftOfClass.length; c++){       // For each class
            if(remainingAircraftOfClass[c] > 0){                         // If there still are aircraft to land.
                // Get the earliest aircraft in the queue. (sorted backwardly)
                int aircraft = next.get(c).get(state.remainingAircraftOfClass[c]);

                used.clear();
                for(int runway: IntStream.range(0,instance.nbRunways).toArray()){ // For each runway, try to find at least one suitable runway.
                    int arrival = getArrivalTime(state.runwayStates, aircraft, runway);
                    if(arrival <= instance.latest[aircraft]) {
                        decisions.add(toDecision(new ALPDecision(c,runway)));
                        used.add(state.runwayStates[runway]);
                    }
                }

                if(used.isEmpty()){
                    // This aircraft will never be able to land.
                    return Collections.emptyIterator();
                }

            }
            totRemaining += remainingAircraftOfClass[c];
        }

        if(totRemaining == 0){
            return Collections.singletonList(DUMMY).iterator();
        } else {
            return decisions.iterator();
        }
    }

    @Override
    public ALPState transition(ALPState state, Decision decision) {
        if(decision.val() == DUMMY){
            return new ALPState(state);
        } else {
            ALPDecision alpDecision = fromDecision(decision.val());
            int aircraftClass = alpDecision.aircraftClass;
            int runway = alpDecision.runway;
            int aircraft = next.get(aircraftClass).get(state.remainingAircraftOfClass[aircraftClass]);
            ALPState nextState = new ALPState(state);
            nextState.remainingAircraftOfClass[aircraftClass] -= 1;
            nextState.runwayStates[runway].prevClass = aircraftClass;
            nextState.runwayStates[runway].prevTime = getArrivalTime(state.runwayStates, aircraft, runway);

            Arrays.sort(nextState.runwayStates);
            return nextState;
        }
    }

    @Override
    public int transitionCost(ALPState state, Decision decision) {
        if(decision.val() == DUMMY) {
            return 0;
        } else {
            ALPDecision alpDecision = fromDecision(decision.val());
            int aircraftClass = alpDecision.aircraftClass;
            int aircraft = next.get(aircraftClass).get(state.remainingAircraftOfClass[aircraftClass]);
            return -(getArrivalTime(state.runwayStates, aircraft, alpDecision.runway) - instance.target[aircraft]);
        }
    }
}


