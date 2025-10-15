package org.ddolib.examples.msct;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 * ################ Minimum Sum Completion Time (MSCT) #####################
 */
public class MSCTDdoMain {

    public static void main(final String[] args) throws IOException {
        final String file = "data/MSCT/msct1.txt";
        DdoModel<MSCTState> model = new DdoModel<>() {
            private final MSCTProblem problem = new MSCTProblem(file);
            @Override
            public Problem<MSCTState> problem() {
                return problem;
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

        Solver<MSCTState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeDdo(model);

//        SearchStatistics stats = solver.minimizeDdo(model, s -> false, (sol, s) -> {
//            System.out.println("--------------------");
//            System.out.println("new incumbent found "+ s.incumbent() + " at iteration " + s.nbIterations());
//            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
//        });

        System.out.println(stats);
    }


//    public static MSCTProblem readInstance(final String fname) throws Exception {
//        Scanner s = new Scanner(new File(fname)).useDelimiter("\\s+");
//        while (!s.hasNextInt())
//            s.nextLine();
//        int nVar = s.nextInt();
//        int[] releas = new int[nVar];
//        int[] proces = new int[nVar];
//        for (int i = 0; i < nVar; i++) {
//            releas[i] = s.nextInt();
//            proces[i] = s.nextInt();
//        }
//        s.close();
//        return new MSCTProblem(releas, proces);
//    }

//    public static MSCTProblem instanceGenerator(int n) {
//        int[] release = new int[n];
//        int[] processing = new int[n];
//        Random rand = new Random(100);
//        for (int i = 0; i < n; i++) {
//            release[i] = rand.nextInt(10);
//            processing[i] = rand.nextInt(10);
//        }
//        return new MSCTProblem(release, processing);
//    }
}


