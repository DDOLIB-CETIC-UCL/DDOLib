package org.ddolib.examples.alp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Represents the <b>Aircraft Landing Problem (ALP)</b>.
 * <p>
 * The ALP consists in scheduling the landing of a fleet of aircraft on one or multiple runways.
 * Each aircraft has a target landing time and a deadline. The objective is to minimize the total
 * deviation from target times (tardiness) while respecting deadlines and runway separation constraints.
 * </p>
 * <p>
 * Landing times are constrained by:
 * </p>
 * <ul>
 *     <li>The previous aircraft landed on the same runway;</li>
 *     <li>The minimum separation time between aircraft classes;</li>
 *     <li>The aircraft’s target and deadline times.</li>
 * </ul>
 * <p>
 * This class implements the {@link Problem} interface for states of type {@link ALPState}.
 * It defines the state space, the feasible domain of decisions, the transition function, and the cost function.
 * </p>
 *
 * <p><b>Fields:</b></p>
 * <ul>
 *     <li>{@code nbClasses}: Number of aircraft classes.</li>
 *     <li>{@code nbAircraft}: Total number of aircraft.</li>
 *     <li>{@code nbRunways}: Number of runways available.</li>
 *     <li>{@code aircraftClass}: Array mapping each aircraft to its class.</li>
 *     <li>{@code aircraftTarget}: Target landing time of each aircraft.</li>
 *     <li>{@code aircraftDeadline}: Deadline for each aircraft.</li>
 *     <li>{@code classTransitionCost}: Minimum separation times between classes.</li>
 *     <li>{@code optimal}: Optional optimal value if known.</li>
 * </ul>
 *
 * @see Problem
 * @see ALPState
 * @see ALPDecision
 */
public class ALPProblem implements Problem<ALPState> {
    /**
     * Number of aircraft classes.
     */
    public final int nbClasses;

    /**
     * Total number of aircraft.
     */
    public final int nbAircraft;

    /**
     * Number of available runways.
     */
    public final int nbRunways;

    /**
     * Mapping of each aircraft to its class.
     */
    public final int[] aircraftClass;

    /**
     * Target landing time for each aircraft.
     */
    public final int[] aircraftTarget;

    /**
     * Deadline for each aircraft.
     */
    public final int[] aircraftDeadline;

    /**
     * Minimum separation times between aircraft classes.
     */
    public final int[][] classTransitionCost;

    /**
     * Known optimal value, if available.
     */
    public final Optional<Double> optimal;

    /**
     * Constructs an ALP problem with the specified parameters.
     *
     * @param nbClasses           Number of aircraft classes
     * @param nbAircraft          Total number of aircraft
     * @param nbRunways           Number of runways
     * @param aircraftClass       Array mapping aircraft to class
     * @param aircraftTarget      Target landing times
     * @param aircraftDeadline    Deadline times
     * @param classTransitionCost Minimum separation times between classes
     * @param optimal             Optional optimal value
     */
    public ALPProblem(final int nbClasses, final int nbAircraft, final int nbRunways, final int[] aircraftClass, final int[] aircraftTarget, final int[] aircraftDeadline, final int[][] classTransitionCost, final Optional<Double> optimal) {
        this.nbClasses = nbClasses;
        this.nbAircraft = nbAircraft;
        this.nbRunways = nbRunways;
        this.aircraftClass = aircraftClass;
        this.aircraftTarget = aircraftTarget;
        this.aircraftDeadline = aircraftDeadline;
        this.classTransitionCost = classTransitionCost;
        this.optimal = optimal;
    }

    /**
     * Constructs an ALP problem without specifying an optimal value.
     *
     * @param nbClasses           Number of aircraft classes
     * @param nbAircraft          Total number of aircraft
     * @param nbRunways           Number of runways
     * @param aircraftClass       Array mapping aircraft to class
     * @param aircraftTarget      Target landing times
     * @param aircraftDeadline    Deadline times
     * @param classTransitionCost Minimum separation times between classes
     */
    public ALPProblem(final int nbClasses, final int nbAircraft, final int nbRunways, final int[] aircraftClass, final int[] aircraftTarget, final int[] aircraftDeadline, final int[][] classTransitionCost) {
        this(nbClasses, nbAircraft, nbRunways, aircraftClass, aircraftTarget, aircraftDeadline, classTransitionCost, Optional.empty());
    }

    /**
     * Constructs an ALP problem by reading from a file.
     * <p>
     * The file format is expected to provide the number of aircraft, classes, runways,
     * optionally the known optimal value, aircraft target and deadline times, and class separation costs.
     * </p>
     *
     * @param fName Path to the input file
     * @throws IOException if the file cannot be read
     */

    public ALPProblem(final String fName) throws IOException {
        final File f = new File(fName);
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            int lineCounter = 0;
            List<String> linesList = bf.lines().toList();
            linesList = linesList.stream().filter(line -> !line.isBlank()).toList();
            String[] firstLine = linesList.get(0).split(" ");
            int nbAircraft = Integer.parseInt(firstLine[0]);
            int nbClasses = Integer.parseInt(firstLine[1]);
            int nbRunways = Integer.parseInt(firstLine[2]);
            Optional<Double> optimal = Optional.empty();
            if (firstLine.length == 4) {
                optimal = Optional.of(Double.parseDouble(firstLine[3]));
            }

            int[] aircraftClass = new int[nbAircraft];
            int[] aircraftDeadline = new int[nbAircraft];
            int[] aircraftTarget = new int[nbAircraft];
            int[][] classTransitionCost = new int[nbClasses][nbClasses];
            for (int i = 1; i < linesList.size(); i++) {
                String[] splitLine = linesList.get(i).split(" ");
                if (lineCounter < nbAircraft) {
                    aircraftTarget[lineCounter] = Integer.parseInt(splitLine[0]);
                    aircraftDeadline[lineCounter] = Integer.parseInt(splitLine[1]);
                    aircraftClass[lineCounter] = Integer.parseInt(splitLine[2]);
                } else {
                    int cnt = 0;
                    for (String s : splitLine) {
                        classTransitionCost[lineCounter - nbAircraft][cnt] = Integer.parseInt(s);
                        cnt++;
                    }
                }
                lineCounter++;
            }
            this.nbAircraft = nbAircraft;
            this.nbClasses = nbClasses;
            this.nbRunways = nbRunways;
            this.aircraftClass = aircraftClass;
            this.aircraftTarget = aircraftTarget;
            this.aircraftDeadline = aircraftDeadline;
            this.classTransitionCost = classTransitionCost;
            this.optimal = optimal;

            latestToEarliestAircraftByClass = new ArrayList<>();
            minSeparationTo = new int[nbClasses];
            Arrays.fill(minSeparationTo, Integer.MAX_VALUE);

            for (int i = 0; i < nbClasses; i++)
                latestToEarliestAircraftByClass.add(new ArrayList<>(List.of(0)));

            for (int i = nbAircraft - 1; i >= 0; i--) {
                latestToEarliestAircraftByClass.get(aircraftClass[i]).add(i);
            }

            for (int i = 0; i < nbClasses; i++) {
                for (int j = 0; j < nbClasses; j++) {
                    minSeparationTo[j] = Math.min(minSeparationTo[j], classTransitionCost[i][j]);
                }
            }
        }
    }

    // Used to know which aircraft of each class will be next to land.
    public ArrayList<ArrayList<Integer>> latestToEarliestAircraftByClass;
    // Minimal time between a "no_class" aircraft and "class" aircraft.
    int[] minSeparationTo;

    private Optional<String> name = Optional.empty();

    // When no plane has yet landed the previous class is -1.
    public static final int DUMMY = -1;

    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    /**
     * Computes the arrival time of an aircraft on a given runway.
     *
     * @param runwayStates The current state of each runway
     * @param aircraft     The aircraft to land
     * @param runway       The runway index
     * @return The computed landing time
     */
    public int getArrivalTime(RunwayState[] runwayStates, int aircraft, int runway) {
        if (runwayStates[runway].prevClass == DUMMY) {
            if (runwayStates[runway].prevTime == 0)
                return aircraftTarget[aircraft];
            else
                return Math.max(aircraftTarget[aircraft],
                        runwayStates[runway].prevTime + minSeparationTo[aircraftClass[aircraft]]);
        } else {
            return Math.max(aircraftTarget[aircraft],
                    runwayStates[runway].prevTime +
                            classTransitionCost[runwayStates[runway].prevClass][aircraftClass[aircraft]]);
        }
    }

    /**
     * Converts an {@link ALPDecision} to its integer representation.
     *
     * @param decision The decision
     * @return The integer encoding of the decision
     */
    public int toDecision(ALPDecision decision) {
        return decision.aircraftClass + nbClasses * decision.runway;
    }

    /**
     * Restores an {@link ALPDecision} from its integer representation.
     *
     * @param value The integer encoding
     * @return The decoded decision
     */
    public ALPDecision fromDecision(int value) {
        return new ALPDecision(
                value % nbClasses,
                value / nbClasses
        );
    }

    @Override
    public int nbVars() {
        return nbAircraft;
    }

    @Override
    public ALPState initialState() {
        int[] remaining = new int[nbClasses];
        Arrays.fill(remaining, 0);
        for (int i = 0; i < nbAircraft; i++)
            remaining[aircraftClass[i]] += 1;
        RunwayState[] runwayStates = new RunwayState[nbRunways];
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
                for (int runway = 0; runway < nbRunways; runway++) {
                    int arrival = getArrivalTime(state.runwayStates, aircraft, runway);
                    if (arrival <= aircraftDeadline[aircraft]) {
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
            return getArrivalTime(state.runwayStates, aircraft, alpDecision.runway) - aircraftTarget[aircraft];
        }
    }

    @Override
    public String toString() {
        String out = String.format("ALP problem with %d aircrafts, %d classes and %d runways",
                nbAircraft, nbClasses, nbRunways);
        return name.orElse(out);
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not match " +
                    "the number %d variables", Arrays.toString(solution), nbVars()));
        }

        // For each runway, the last time, it was used.
        int[] lastTime = IntStream.range(0, nbRunways).map(r -> Integer.MAX_VALUE).toArray();
        // For each runway, the class of the last aircraft landing to it.
        int[] lastClass = new int[nbRunways];

        ALPSolution alpSol = new ALPSolution(this, solution);
        int value = 0;
        for (ALPSchedule a : alpSol) {
            int target = aircraftTarget[a.aircraft()];
            int deadline = aircraftDeadline[a.aircraft()];
            if (a.landingTime() < target || a.landingTime() > deadline) {
                String msg = String.format("Aircraft %d is landing at %d outside its time window " +
                        "[%d , %d]", a.aircraft(), a.landingTime(), target, deadline);
                throw new InvalidSolutionException(msg);
            }
            int allowedTime = lastTime[a.runway()] + classTransitionCost[lastClass[a.runway()]][a.aircraftClass()];
            if (a.landingTime() < allowedTime) {
                String msg = String.format(
                        "Aircraft %d of class %d is landing on runway %d at %d.\n" +
                                "The previous aircraft using this runway was class %d landing at " +
                                "%d. The next landing should be at least at %d",
                        a.aircraft(),
                        a.aircraftClass(),
                        a.runway(),
                        a.landingTime(),
                        lastClass[a.runway()],
                        lastTime[a.runway()],
                        allowedTime
                );
                throw new InvalidSolutionException(msg);
            }
            value += a.landingTime() - target;
            lastTime[a.runway()] = a.landingTime();
            lastClass[a.runway()] = a.aircraftClass();
        }

        return value;
    }
}


