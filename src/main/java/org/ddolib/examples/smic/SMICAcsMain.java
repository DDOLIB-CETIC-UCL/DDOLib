package org.ddolib.examples.smic;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;

/**
 * ################## The Single Machine with Inventory Constraint (SMIC) ####################
 */

public class SMICAcsMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data","SMIC","data10_2.txt").toString() : args[0];
        final SMICProblem problem = new SMICProblem(instance);
        AcsModel<SMICState> model = new AcsModel<>() {
            @Override
            public Problem<SMICState> problem() {
                return problem;
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

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}
