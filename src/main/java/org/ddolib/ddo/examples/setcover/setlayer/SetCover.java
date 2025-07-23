package org.ddolib.ddo.examples.setcover.setlayer;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultStateCoordinates;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.examples.setcover.setlayer.SetCoverHeuristics.FocusClosingElements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.ddolib.ddo.implem.solver.Solvers.relaxationSolver;

public class SetCover {

    public static void main(String[] args) throws IOException {
        final String instance = args[0];
        final int w = Integer.parseInt(args[1]);

        final SetCoverProblem problem = readInstance(instance);
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(w);
        final VariableHeuristic<SetCoverState> varh = new FocusClosingElements(problem);
        final StateDistance<SetCoverState> distance = new SetCoverDistance();
        final StateCoordinates<SetCoverState> coord = new DefaultStateCoordinates<>();
        // final StateDistance<SetCoverState> distance = new SetCoverIntersectionDistance();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final DefaultDominanceChecker<SetCoverState> dominance = new DefaultDominanceChecker<>();
        final Solver solver = relaxationSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                dominance,
                ClusterStrat.GHP,
                distance,
                coord,
                54658646);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;


        int[] solution = solver.bestSolution().map(decisions -> {
            System.out.println("Solution Found");
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            return values;
        }).get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }

    /**
     * The instance file contains the definition of each set, i.e. the element that it covers.
     * For this model we instead need, for each element, a description of the collection of
     * sets that cover it, i.e. the description of each constraint.
     * This method makes the required conversion.
     * @param sets the collection of sets from the instance file
     * @param nElem the number of element in the universe
     * @return the collection of constraints
     */
    private static List<Set<Integer>> convertSetsToConstraints(List<Set<Integer>> sets, int nElem) {
        List<Set<Integer>> constraints = new ArrayList<>(nElem);
        for (int elem = 0; elem < nElem; elem++) {
            constraints.add(new HashSet<>());
            for (int set = 0; set < sets.size(); set++) {
                if (sets.get(set).contains(elem)) {
                    constraints.get(elem).add(set);
                }
            }
        }
        return constraints;
    }

    /**
     * Load the SetCoverProblem from a file
     * @param fname the path to the file describing the instance
     * @return a SetCoverProblem representing the instance
     * @throws IOException if the file cannot be found or is not readable
     */
    public static SetCoverProblem readInstance(final String fname) throws IOException {
        final File f = new File(fname);
        try (final BufferedReader br = new BufferedReader(new FileReader(f))) {
            final PinReadContext context = new PinReadContext();

            br.lines().forEachOrdered((String s) -> {
                if (context.isFirst) {
                    context.isFirst = false;

                    String[] tokens = s.split("\\s");
                    context.nElem = Integer.parseInt(tokens[0]);
                    context.nSet = Integer.parseInt(tokens[1]);

                    context.sets = new ArrayList<>(context.nSet);
                } else {
                    if (context.count< context.nSet) {
                        String[] tokens = s.split("\\s");
                        context.sets.add(new HashSet<>(tokens.length));
                        for (String token : tokens) {
                            context.sets.get(context.count).add(Integer.parseInt(token));
                        }
                        context.count++;
                    }
                }
            });

            return new SetCoverProblem(context.nElem, context.nSet, convertSetsToConstraints(context.sets, context.nElem));
        }
    }

    private static class PinReadContext {
        boolean isFirst = true;
        int nElem = 0;
        int nSet = 0;
        List<Set<Integer>> sets;
        int count = 0;
    }

}
