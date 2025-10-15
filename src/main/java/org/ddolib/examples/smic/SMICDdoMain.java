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
 * ################## The Single Machine with Inventory Constraint (SMIC) ####################
 */
public class SMICDdoMain {
    public static void main(String[] args) throws IOException {
        final String file = "data/SMIC/data10_2.txt";
        final SMICProblem problem = new SMICProblem(file);
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

        Solver<SMICState> solver = new Solver<>();

        final SearchStatistics stats = solver.minimizeDdo(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);
    }
}
