package org.ddolib.examples.msct;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.File;
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
public class MSCTDdoMain {

    public static void main(final String[] args) throws Exception {
        final String file = "data/MSCT/msct1.txt";
        DdoModel<MSCTState> model = new DdoModel<>() {
            private MSCTProblem problem;

            @Override
            public Problem<MSCTState> problem() {
                try {
                    problem = readInstance(file);
                    return problem;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public MSCTRelax relaxation() {
                return new MSCTRelax(problem);
            }

            @Override
            public MSCTRanking ranking() {
                return new MSCTRanking();
            }

            @Override
            public DominanceChecker<MSCTState> dominance() {
                return new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };

        Solvers<MSCTState> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeDdo(model);

        System.out.println(stats);
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


