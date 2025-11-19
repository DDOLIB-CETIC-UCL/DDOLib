package org.ddolib.examples.mks;

import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.*;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;


public class MKSMain {

    public static void main(String[] args) throws IOException {
        /*final String instance = "data/MKS/MKP_10.txt";
        final SolverConfig<MKSState, Integer> config = new SolverConfig<>();
        config.problem = readInstance(instance);
        config.relax = new MKSRelax();
        config.ranking = new MKSRanking();
        config.width = new FixedWidth<>(1000);
        config.varh = new DefaultVariableHeuristic<MKSState>();
        config.dominance = new SimpleDominanceChecker<>(new MKSDominance(), config.problem.nbVars());
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        config.relaxStrategy = new GHP<>(new MKSDistance());
        config.distance = new MKSDistance();
        config.coordinates = new MKSCoordinates();

        final Solver solver = new SequentialSolver<>(config);


        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.minimize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        System.out.println("Search statistics:" + stats);


        int[] solution = solver.bestSolution().map(decisions -> {
            int[] values = new int[config.problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            return values;
        }).get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));*/
    }



}
