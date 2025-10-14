package org.ddolib.examples.setcover.elementlayer;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.*;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SetCover {

    public static void main(String[] args) throws IOException {
        final String instance = args[0];
        final int w = Integer.parseInt(args[1]);

        final SetCoverProblem problem = readInstance(instance);
        final SolverConfig<SetCoverState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.ranking = new SetCoverRanking();
        config.relax = new SetCoverRelax();
        config.width = new FixedWidth<>(w);
        config.varh = new SetCoverHeuristics.MinCentrality(problem);
        config.distance = new SetCoverDistance(problem);
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.Frontier);
        config.dominance = new DefaultDominanceChecker<>();
        final Solver solver = new SequentialSolver<>(config);

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
        System.out.printf("Objective: %.3f%n", solver.bestValue().get());
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
        return readInstance(fname, false);
    }

    /**
     * Load the SetCoverProblem from a file
     * @param fname the path to the file describing the instance
     * @param weighted true if the instance has cost for the set, false otherwise
     * @return a SetCoverProblem representing the instance
     * @throws IOException if the file cannot be found or is not readable
     */
    public static SetCoverProblem readInstance(final String fname, final boolean weighted) throws IOException {
        final File f = new File(fname);
        try (final BufferedReader br = new BufferedReader(new FileReader(f))) {
            final PinReadContext context = new PinReadContext();

            br.lines().forEachOrdered((String s) -> {
                if (context.isFirst) {
                    context.isFirst = false;
                    context.isSecond = true;

                    String[] tokens = s.split("\\s");
                    context.nElem = Integer.parseInt(tokens[0]);
                    context.nSet = Integer.parseInt(tokens[1]);

                    context.sets = new ArrayList<>(context.nSet);
                } else if (context.isSecond && weighted) {
                    context.isSecond = false;
                    String[] tokens = s.split("\\s");
                    context.weights = new ArrayList<>(context.nSet);
                    for (int i = 0; i < context.nSet; i++) {
                        context.weights.add(Double.parseDouble(tokens[i]));
                    }
                }
                else {
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
            if (weighted)
                return new SetCoverProblem(context.nElem, context.nSet, convertSetsToConstraints(context.sets, context.nElem), context.weights);
            else {
                context.weights = new ArrayList<>(context.nSet);
                for (int i = 0; i < context.nSet; i++) {
                    context.weights.add(1.0);
                }
                return new SetCoverProblem(context.nElem, context.nSet, convertSetsToConstraints(context.sets, context.nElem), context.weights);
            }
        }
    }

    private static class PinReadContext {
        boolean isFirst = true;
        boolean isSecond = false;
        int nElem = 0;
        int nSet = 0;
        List<Set<Integer>> sets;
        List<Double> weights;
        int count = 0;
    }

}
