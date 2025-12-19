package org.ddolib.examples.alp;

import org.ddolib.ddo.core.Decision;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * Class that converts a solution from a solver to a readable and usable solution for the ALP.
 */
class ALPSolution implements Iterable<ALPSchedule> {

    private final ALPSchedule[] solution;

    /**
     * Instantiate a solution of the ALP
     *
     * @param problem  The associated problem.
     * @param solution An array {@code t} such that {@code t[i]} is the assigned value of
     *                 variable {@code i}.
     */
    public ALPSolution(ALPProblem problem, int[] solution) {
        this.solution = new ALPSchedule[problem.nbVars()];

        ALPState curState = problem.initialState();
        for (int i = 0; i < problem.nbVars(); i++) {
            RunwayState[] runwayStates = curState.runwayStates;
            Decision d = new Decision(i, solution[i]);
            ALPDecision alpD = problem.fromDecision(d.val());
            int aircraft = problem.latestToEarliestAircraftByClass
                    .get(alpD.aircraftClass)
                    .get(curState.remainingAircraftOfClass[alpD.aircraftClass]);
            int landingTime = problem.getArrivalTime(runwayStates, aircraft, alpD.runway);
            curState = problem.transition(curState, d);
            this.solution[i] = new ALPSchedule(aircraft, problem.aircraftClass[aircraft], landingTime, alpD.runway);
        }

    }


    @Override
    public String toString() {
        return Arrays.stream(solution).map(s -> String.format(
                "Aircraft: %3d - Class: %3d - Landing time: %5d - Runway: %3d",
                s.aircraft(), s.aircraftClass(), s.landingTime(), s.runway())).collect(Collectors.joining("\n"));
    }

    @Override
    public Iterator<ALPSchedule> iterator() {
        return Arrays.stream(solution).iterator();
    }


}

/**
 * Contains schedule data for an aircraft.
 *
 * @param aircraft      The id of the aircraft.
 * @param aircraftClass The class of the aircraft.
 * @param landingTime   When the aircraft is landing.
 * @param runway        The runway on which the aircraft is landing.
 */
record ALPSchedule(int aircraft, int aircraftClass, int landingTime, int runway) {
}

