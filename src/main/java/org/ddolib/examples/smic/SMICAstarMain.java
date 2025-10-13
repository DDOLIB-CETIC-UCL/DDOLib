package org.ddolib.examples.smic;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class SMICAstarMain {
    public static void main(String[] args) throws FileNotFoundException {
        final String file = "data/SMIC/data10_2.txt";
        SMICProblem problem = readProblem("data/SMIC/data10_2.txt");
        Model<SMICState> model = new Model<>() {
            private SMICProblem problem;

            @Override
            public Problem<SMICState> problem() {
                try {
                    problem = readProblem(file);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }

            @Override
            public SMICFastLowerBound lowerBound() {
                return new SMICFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<SMICState> dominance() {
                return new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
            }
        };

        Solver<SMICState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAstar(model);

        System.out.println(stats);

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
                        -opti.get());
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
