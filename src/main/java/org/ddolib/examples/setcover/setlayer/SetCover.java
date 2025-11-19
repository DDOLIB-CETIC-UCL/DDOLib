package org.ddolib.examples.setcover.setlayer;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.*;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
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
        /*final String instance = args[0];
        final int w = Integer.parseInt(args[1]);

        final SolverConfig<SetCoverState, Integer> config = new SolverConfig<>();
        final SetCoverProblem problem = readInstance(instance);
        config.problem = problem;
        config.ranking = new SetCoverRanking();
        config.relax = new SetCoverRelax();
        config.width = new FixedWidth<>(w);
        config.varh = new SetCoverHeuristics.FocusClosingElements(problem);
        // final StateDistance<SetCoverState> distance = new SetCoverIntersectionDistance();
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        config.dominance = new DefaultDominanceChecker<>();
        final Solver solver = new SequentialSolver<>(config);

        long start = System.currentTimeMillis();
        solver.minimize();
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
        System.out.printf("Solution : %s%n", Arrays.toString(solution));*/
    }

}
