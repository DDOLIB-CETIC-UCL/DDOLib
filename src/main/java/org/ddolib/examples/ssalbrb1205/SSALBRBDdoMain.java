package org.ddolib.examples.ssalbrb1205;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public class SSALBRBDdoMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0
                ? Path.of("data", "SALBP1", "small data set_n=20", "instance_n=20_441.alb").toString()
                : args[0];
//        final String instance = args.length == 0 ?
//                Path.of("data", "test_5tasks_2.alb").toString() : args[0];

        final SSALBRBProblem problem = new SSALBRBProblem(instance);
        
        final DdoModel<SSALBRBState> model = new DdoModel<>() {
            @Override
            public Problem<SSALBRBState> problem() {
                return problem;
            }

            @Override
            public Relaxation<SSALBRBState> relaxation() {
                return new SSALBRBRelax();
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
                return new FixedWidth<>(500000);  // 无限宽度（完全搜索）
            }

            @Override
            public boolean exportDot() {
                return false;
            }
        };

        Solution solution = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
//            SSALBRBSolutionPrinter.printSolution(problem, sol);
        });

        System.out.println("\n" + solution.statistics());
    }
}
