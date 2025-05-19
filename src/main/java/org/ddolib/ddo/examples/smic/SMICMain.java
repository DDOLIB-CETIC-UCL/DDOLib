package org.ddolib.ddo.examples.smic;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Given a set J= {1,..., n} of jobs, partitioned into set J−
 * of loading jobs and set J+ of unloading jobs. Each job j ∈ J has a
 * processing time p_j ∈ R+, a release date r_j ∈ R+ and an inventory
 * modification δ_j ∈ R, where δ_j ≥ 0 for j ∈ J+ and δ_j < 0 for j ∈ J−.
 * All jobs are to be executed by a single machine, which can only serve
 * one job at a time. The initial inventory is denoted by I_0 and the
 * capacity of the inventory storage is I_C. The objective is to sequence
 * the jobs in J such that the makespan (the completion time of the
 * last job in the sequence) is minimized while the inventory is never
 * below 0 nor above I_C.
 * This problem is considerd in the paper: Morteza Davari, Mohammad Ranjbar, Patrick De Causmaecker, Roel Leus:
 * Minimizing makespan on a single machine with release dates and inventory constraints. Eur. J. Oper. Res. 286(1): 115-128 (2020)
 */
public class SMICMain {
    public static void main(String[] args) throws FileNotFoundException {
        SMICProblem problem = readProblem("data/SMIC/example.txt");
        final SMICRelax relax = new SMICRelax(problem);
        final SMICRanking ranking = new SMICRanking();
        final FixedWidth<SMICState> width = new FixedWidth<>(10);
        final VariableHeuristic<SMICState> varh = new DefaultVariableHeuristic<SMICState>();
        final Frontier<SMICState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);


        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;


        int[] solution = solver.bestSolution().map(decisions -> {
            int[] values = new int[problem.nbVars()];
            for (Decision d: decisions) {
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
        while (!s.hasNextLine()) {s.nextLine();}
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
