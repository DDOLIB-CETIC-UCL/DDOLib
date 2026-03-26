package org.ddolib.examples.ssalbrb;

import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SSALBRBDdoMainTest {
    public static void main(String[] args) throws IOException {
//        final String instance = args.length == 0
//                ? Path.of("data", "SALBP1", "small data set_n=20", "instance_n=20_441.alb").toString()
//                : args[0];
        final String instance = args.length == 0 ?
                Path.of("data", "test_10tasks_3.alb").toString() : args[0];


        int[] humanDurations = {50, 121, 214, 140, 34, 142, 69, 132, 129, 282, 279};
        int[] robotDurations = {100000, 100000, 428, 280, 68, 284, 100000, 264, 258, 564, 100000};
        int[] collaborationDurations = {100000, 100000, 150, 98, 24, 99, 100000, 100000, 100000, 100000, 100000};
        Map<Integer, List<Integer>> successors = new HashMap<>();
        successors.put(0, Arrays.asList());
        successors.put(1, Arrays.asList(8));
        successors.put(2, Arrays.asList(9));
        successors.put(3, Arrays.asList());
        successors.put(4, Arrays.asList(0));
        successors.put(5, Arrays.asList(10));
        successors.put(6, Arrays.asList());
        successors.put(7, Arrays.asList(6));
        successors.put(8, Arrays.asList());
        successors.put(9, Arrays.asList(7));
        successors.put(10, Arrays.asList());
        final SSALBRBProblem problem = new SSALBRBProblem(humanDurations.length, humanDurations,robotDurations,collaborationDurations,successors,1000);

        final DdoModel<SSALBRBState> model = new DdoModel<>() {
            @Override
            public Problem<SSALBRBState> problem() {
                return problem;
            }

            @Override
            public Relaxation<SSALBRBState> relaxation() {
                return new SSALBRBRelax(problem.humanDurations, problem.robotDurations, problem.collaborationDurations);
            }

            @Override
            public SSALBRBRanking ranking() {
                return new SSALBRBRanking();
            }

            @Override
            public FastLowerBound<SSALBRBState> lowerBound() {
                return new SSALBRBFastLowerBound(problem);
            }

            @Override
            public WidthHeuristic<SSALBRBState> widthHeuristic() {
                return new FixedWidth<>(10);
            }

            @Override
            public boolean exportDot() {
                return false;
            }
        };

        Solution solution = Solvers.minimizeDdo(model, (sol, s) -> {
            System.out.println("\nNew incumbent solution found:");
            SolutionPrinter.printSolution(s, sol);
//            SSALBRBSolutionPrinter.printSolution(problem, sol);
        });
        System.out.println("done");
        System.out.println("\n" + solution.statistics());
    }
}
