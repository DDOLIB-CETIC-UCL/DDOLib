package org.ddolib.examples.ssalbrb;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SSALBRBAstarMain {
    public static void main(String[] args) throws IOException {

        final String instance = args.length == 0 ?
                Path.of("data", "HRCP", "example_11").toString() : args[0];


        final SSALBRBProblem problem = new SSALBRBProblem(instance);

        final Model<SSALBRBState> model = new Model<SSALBRBState>() {
            @Override
            public Problem<SSALBRBState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<SSALBRBState> lowerBound() {
                return new SSALBRBFastLowerBound(problem);
            }
        };

        Solution solution = Solvers.minimizeAstar(model, (sol, s) -> {
            System.out.println("\nNew incumbent solution found:");
            SolutionPrinter.printSolution(s, sol);
//            SSALBRBSolutionPrinter.printSolution(problem, sol);
        });
        System.out.println("done");
        System.out.println("\n" + solution.statistics());
    }
}
