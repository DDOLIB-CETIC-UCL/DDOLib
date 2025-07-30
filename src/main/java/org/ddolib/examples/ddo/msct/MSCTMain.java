package org.ddolib.examples.ddo.msct;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import static org.ddolib.factory.Solvers.sequentialSolver;

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
        MSCTProblem problem = instanceGenerator(n);
        System.out.println(Arrays.toString(problem.release));
        System.out.println(Arrays.toString(problem.processing));
        final MSCTRelax relax = new MSCTRelax(problem);
        final MSCTRanking ranking = new MSCTRanking();
        final FixedWidth<MSCTState> width = new FixedWidth<>(100);
        final VariableHeuristic<MSCTState> varh = new DefaultVariableHeuristic<MSCTState>();
        final SimpleDominanceChecker<MSCTState, Integer> dominance = new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());
        final Frontier<MSCTState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                dominance
        );


        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;
        System.out.println(stats);

        int[] solution = solver.bestSolution().map(decisions -> {
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            return values;
        }).get();

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


