package org.ddolib.examples.smic;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
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
public class SMICDdoMain {
    public static void main(String[] args) throws FileNotFoundException {
//        final String file = "data/SMIC/data10_2.txt";
//        SMICProblem problem = readProblem("data/SMIC/data10_2.txt");
        int[] NS = {10, 20, 30, 40, 50};
        int[] ALPHAS = {10, 100};
        double[] TAUS = {0.5, 1.0, 1.5, 2.0};
        int[] ETAS = {1, 3, 5};
        // number of instances per (n,alpha,tau,eta) combination
        int INSTANCES_PER_CONFIG = 3;
        SMICProblem[] problems = new SMICProblem[NS.length * ALPHAS.length * ETAS.length * TAUS.length * INSTANCES_PER_CONFIG];
        long globalSeed = System.currentTimeMillis();
        Random globalRand = new Random(globalSeed);
        int k = 0;
        for (int n : NS) {
            for (int alpha : ALPHAS) {
                for (double tau : TAUS) {
                    for (int eta : ETAS) {
                        for (int inst = 0; inst < INSTANCES_PER_CONFIG; inst++) {
                            long seed = globalRand.nextLong();
                            problems[k] = new SMICGenrator(n, alpha, tau, eta, seed).generate();
                            k++;
                        }
                    }
                }
            }
        }

        DdoModel<SMICState> model = new DdoModel<>() {
            private SMICProblem problem;

            @Override
            public Problem<SMICState> problem() {
                problem = problems[0];
                return problem;
                /*try {
                    problem = readProblem(file);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException();
                }*/
            }

            @Override
            public SMICRelax relaxation() {
                return new SMICRelax(problem);
            }

            @Override
            public SMICRanking ranking() {
                return new SMICRanking();
            }

            @Override
            public SMICFastLowerBound lowerBound() {
                return new SMICFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<SMICState> dominance() {
                return new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
            }

            @Override
            public Frontier<SMICState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };

        Solver<SMICState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeDdo(model);

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
