package org.ddolib.examples.ddo.smic;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolverWithCache;
import org.ddolib.modeling.DefaultFastUpperBound;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import static org.ddolib.factory.Solvers.sequentialSolverWithCache;


/**
 * Given a set J of n jobs, partitioned into a set J1
 * of n1 loading jobs and set J2 of n2 unloading jobs. Each job j ∈ J has a
 * processing time p ∈ R+, a release date r ∈ R+ and a positive (resp. negative) inventory
 * modification for loading (resp. unloading) task. The objective is to sequence
 * the jobs in J such that the makespan is minimized while the inventory is between a given range.
 * This problem is considered in the paper: Morteza Davari, Mohammad Ranjbar, Patrick De Causmaecker, Roel Leus:
 * Minimizing makespan on a single machine with release dates and inventory constraints. Eur. J. Oper. Res. 286(1): 115-128 (2020)
 */
public class SMICCacheMain {
    public static void main(String[] args) throws FileNotFoundException {
        SMICProblem problem = readProblem("data/SMIC/data10_5.txt");
        final SMICRelax relax = new SMICRelax(problem);
        final SMICRanking ranking = new SMICRanking();
        final FixedWidth<SMICState> width = new FixedWidth<>(10);
        final VariableHeuristic<SMICState> varh = new DefaultVariableHeuristic<SMICState>();
        final DefaultDominanceChecker dominance = new DefaultDominanceChecker();
        final SimpleCache<SMICState> cache = new SimpleCache<SMICState>();
        final Frontier<SMICState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolverWithCache(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                new DefaultFastUpperBound<>(),
                dominance,
                cache);


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
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }

    public static SMICProblem readProblem(String filename) throws FileNotFoundException {
        String name = filename;
        Scanner s = new Scanner(new File(filename)).useDelimiter("\\s+");
        while (!s.hasNextLine()) {
            s.nextLine();
        }
        int nbJob = s.nextInt();
        int initInventory = s.nextInt();
        int capaInventory = s.nextInt();
        int[] type = new int[nbJob];
        int[] processing = new int[nbJob];
        int[] weight = new int[nbJob];
        int[] release = new int[nbJob];
        int[] inventory = new int[nbJob];
        for (int i = 0; i < nbJob; i++) {
            type[i] = s.nextInt();
            processing[i] = s.nextInt();
            weight[i] = s.nextInt();
            release[i] = s.nextInt();
            inventory[i] = s.nextInt();
        }
        s.close();
        return new SMICProblem(name, nbJob, initInventory, capaInventory, type, processing, weight, release, inventory);
    }
}
