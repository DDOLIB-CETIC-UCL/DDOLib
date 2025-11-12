package org.ddolib.examples.salbp1;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public class SALBPDdoMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "SALBP1", "small data set_n=20", "instance_n=20_18.alb").toString() : args[0];
        final SALBPProblem problem = new SALBPProblem(instance);
        final DdoModel<SALBPState> model = new DdoModel<>() {
            @Override
            public Problem<SALBPState> problem() {
                return problem;
            }

            @Override
            public Relaxation<SALBPState> relaxation() {
                return new SALBPRelax(problem);
            }

            @Override
            public SALBPRanking ranking() {
                return new SALBPRanking();
            }

            @Override
            public FastLowerBound<SALBPState> lowerBound() {
                return new SALBPFastLowerBound(problem);
            }

            @Override
            public WidthHeuristic<SALBPState> widthHeuristic() {
                return new FixedWidth<>(2);
            }

            @Override
            public boolean exportDot() {
                return true;
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(stats);
    }
}
