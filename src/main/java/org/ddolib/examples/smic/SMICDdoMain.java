package org.ddolib.examples.smic;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

/**
 * ################## The Single Machine with Inventory Constraint (SMIC) ####################
 */
public class SMICDdoMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data","SMIC","data10_2.txt").toString() : args[0];
        final SMICProblem problem = new SMICProblem(instance);
        DdoModel<SMICState> model = new DdoModel<>() {
            @Override
            public Problem<SMICState> problem() {
                return problem;
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

        final SearchStatistics stats = Solvers.minimizeDdo(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);
    }
}
