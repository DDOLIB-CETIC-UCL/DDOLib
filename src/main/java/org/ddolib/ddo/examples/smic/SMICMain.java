package org.ddolib.ddo.examples.smic;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;

/**
 * Given a set J of n jobs, partitioned into a set J1
 * of n1 loading jobs and set J2 of n2 unloading jobs. Each job j ∈ J has a
 * processing time p ∈ R+, a release date r ∈ R+ and a positive (resp. negative) inventory
 * modification for loading (resp. unloading) task. The objective is to sequence
 * the jobs in J such that the makespan is minimized while the inventory is between a given range.
 * This problem is considered in the paper: Morteza Davari, Mohammad Ranjbar, Patrick De Causmaecker, Roel Leus:
 * Minimizing makespan on a single machine with release dates and inventory constraints. Eur. J. Oper. Res. 286(1): 115-128 (2020)
 */
public class SMICMain {
    public static void main(String[] args) throws FileNotFoundException {
        SMICProblem problem = readProblem("data/SMIC/data100_2.txt");
        final SMICRelax relax = new SMICRelax(problem);
        final SMICRanking ranking = new SMICRanking();
        final FixedWidth<SMICState> width = new FixedWidth<>(10);
        final VariableHeuristic<SMICState> varh = new DefaultVariableHeuristic<SMICState>();
        final SimpleDominanceChecker<SMICState, Integer> dominance = new SimpleDominanceChecker<>(
                new SMICDominance(), problem.nbVars()
        );
        final Frontier<SMICState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
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
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;


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
