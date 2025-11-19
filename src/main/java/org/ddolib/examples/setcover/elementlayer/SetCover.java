package org.ddolib.examples.setcover.elementlayer;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.*;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.RelaxationSolver;
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
        final Solver solver = new RelaxationSolver<>(config);

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
        System.out.printf("Objective: %.3f%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));*/
    }



}
