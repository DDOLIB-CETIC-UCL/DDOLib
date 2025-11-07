package org.ddolib.examples.salbp1;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public class SALBPAstarMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "SALBP1", "small data set_n=20", "instance_n=20_4.alb").toString() : args[0];
        final SALBPProblem problem = new SALBPProblem(instance);
        final Model<SALBPState> model = new Model<>() {
            @Override
            public Problem<SALBPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<SALBPState> lowerBound() {
                return new SALBPFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });
    }
}
