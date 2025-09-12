package org.ddolib.examples.msct;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 * The problem is to sequence n jobs such that:
 * - each job is scheduled after its release time
 * - the sum of completion time is minimized
 * This DP model is from "Transition dominance in domain-independent dynamic programming"
 * In this model a state is represented by:
 * - the set of remaining jobs
 * - the current time (the end time of last sequenced job)
 */
public class MSCTMain {

    public static void main(final String[] args) throws Exception {
//        final String instance = "data/MSCT/msct1.txt";
//        final MSCTProblem problem = readInstance(instance);
        int n = 11;
        SolverConfig<MSCTState, Integer> config = new SolverConfig<>();
        MSCTProblem problem = instanceGenerator(n);
        config.problem = problem;
        System.out.println(Arrays.toString(problem.release));
        System.out.println(Arrays.toString(problem.processing));
        config.relax = new MSCTRelax(problem);
        config.ranking = new MSCTRanking();
        config.width = new FixedWidth<>(100);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        config.dominance = new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());
        final Solver solver = new SequentialSolver<>(config);


        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;
        System.out.println(stats);

        int[] solution = solver.constructBestSolution(problem.nbVars());

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }


    public static MSCTProblem readInstance(final String fname) throws Exception {
        Scanner s = new Scanner(new File(fname)).useDelimiter("\\s+");
        while (!s.hasNextInt())
            s.nextLine();
        int nVar = s.nextInt();
        int[] releas = new int[nVar];
        int[] proces = new int[nVar];
        for (int i = 0; i < nVar; i++) {
            releas[i] = s.nextInt();
            proces[i] = s.nextInt();
        }
        s.close();
        return new MSCTProblem(releas, proces);
    }

    public static MSCTProblem instanceGenerator(int n) {
        int[] release = new int[n];
        int[] processing = new int[n];
        Random rand = new Random(100);
        for (int i = 0; i < n; i++) {
            release[i] = rand.nextInt(10);
            processing[i] = rand.nextInt(10);
        }
        return new MSCTProblem(release, processing);
    }
}


