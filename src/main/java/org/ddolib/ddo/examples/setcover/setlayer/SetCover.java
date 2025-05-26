package org.ddolib.ddo.examples.setcover.setlayer;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.RelaxationSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SetCover {

    public static void main(String[] args) throws IOException {
        final String instance = args[0];
        final String solverString = args[1];
        final String branchingString = args[2];

        final SetCoverProblem problem = readInstance(instance);
        final SetCoverRanking ranking = new SetCoverRanking();
        final SetCoverRelax relax = new SetCoverRelax();
        final FixedWidth<SetCoverState> width = new FixedWidth<>(1000000);
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);

        final Solver solver;
        switch (solverString) {
            case "relax":
                solver = new RelaxationSolver<>(
                        problem,
                        relax,
                        varh,
                        ranking,
                        width,
                        frontier);
                break;
            default: // sequential
                solver = new SequentialSolver<>(
                        problem,
                        relax,
                        varh,
                        ranking,
                        width,
                        frontier);
                break;
        }



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

    public static SetCoverProblem readInstance(final String fname) throws IOException {
        return readInstance(fname, 0);
    }

    public static SetCoverProblem readInstance(final String fname, int nbrElemRemoved) throws IOException {
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

            nbrElemRemoved = Math.min(context.nElem,  nbrElemRemoved);

            return new SetCoverProblem(context.nElem, context.nSet, context.sets, nbrElemRemoved);
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
