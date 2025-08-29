package org.ddolib.examples.ddo.smic;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;


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
        SMICProblem problem = readProblem("data/SMIC/data10_1.txt");
        SolverConfig<SMICState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SMICRelax(problem);
        config.ranking = new SMICRanking();
        config.width = new FixedWidth<>(200);
        config.varh = new DefaultVariableHeuristic<>();
        config.dominance = new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        final Solver solver = new SequentialSolver<>(config);


        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.constructBestSolution(problem.nbVars());

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %s%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }

    public static SMICProblem readProblem(String filename) throws FileNotFoundException {
        String name = filename;
        Scanner s = new Scanner(new File(filename)).useDelimiter("\\s+");
        while (!s.hasNextLine()) {
            s.nextLine();
        }
        if (filename.contains(".txt")) {
            int nbJob = s.nextInt();
            int initInventory = s.nextInt();
            int capaInventory = s.nextInt();
            int[] type = new int[nbJob];
            int[] processing = new int[nbJob];
            int[] weight = new int[nbJob];
            int[] release = new int[nbJob];
            int[] inventory = new int[nbJob];
            Optional<Double> opti = Optional.empty();
            for (int i = 0; i < nbJob; i++) {
                type[i] = s.nextInt();
                processing[i] = s.nextInt();
                weight[i] = s.nextInt();
                release[i] = s.nextInt();
                inventory[i] = s.nextInt();
            }
            if (s.hasNextInt()) {
                opti = Optional.of(s.nextDouble());
            }

            if (opti.isPresent()) {
                return new SMICProblem(filename, nbJob, initInventory, capaInventory, type, processing, weight, release, inventory,
                        opti.get());
            } else {
                return new SMICProblem(filename, nbJob, initInventory, capaInventory, type, processing, weight, release, inventory);
            }
        } else {
            int nbJob = Integer.parseInt(s.nextLine().split("\t=\t")[1].split(";")[0]);
            int initInventory = Integer.parseInt(s.nextLine().split("\t=\t")[1].split(";")[0]);
            int capaInventory = Integer.parseInt(s.nextLine().split("\t=\t")[1].split(";")[0]);
            int[] type = new int[nbJob];
            int[] processing = new int[nbJob];
            int[] weight = new int[nbJob];
            int[] release = new int[nbJob];
            int[] inventory = new int[nbJob];
            String[] t = extractArrayValue(s.nextLine());
            String[] p = extractArrayValue(s.nextLine());
            String[] w = extractArrayValue(s.nextLine());
            String[] r = extractArrayValue(s.nextLine());
            String[] in = extractArrayValue(s.nextLine());
            for (int i = 0; i < nbJob; i++) {
                type[i] = Integer.parseInt(t[i]);
                processing[i] = Integer.parseInt(p[i]);
                weight[i] = Integer.parseInt(w[i]);
                release[i] = Integer.parseInt(r[i]);
                inventory[i] = Integer.parseInt(in[i]);
            }
            s.close();
            return new SMICProblem(name, nbJob, initInventory, capaInventory, type, processing, weight, release, inventory);
        }
    }

    private static String[] extractArrayValue(String line) {
        String[] v = null;
        if (line.contains("=") && line.contains("[")) {
            int start = line.indexOf('[');
            int end = line.indexOf(']');
            if (start != -1 && end != -1 && end > start) {
                String arrayStr = line.substring(start + 1, end);
                v = arrayStr.split(", ");
            }
        }
        return v;
    }
}
