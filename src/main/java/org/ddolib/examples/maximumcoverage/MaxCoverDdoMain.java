package org.ddolib.examples.maximumcoverage;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.smic.SMICRanking;
import org.ddolib.examples.smic.SMICRelax;
import org.ddolib.examples.smic.SMICState;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MaxCoverDdoMain {
    public static void main(String[] args) throws IOException {
        MaxCoverProblem problem = new MaxCoverProblem(30, 30, 7,0.1,42);
        // MaxCoverProblem problem = new MaxCoverProblem(10, 10, 5,0.1,42);

        // MaxCoverProblem problem = new MaxCoverProblem("src/test/resources/MaxCover/mc_n10_m5_k3_r10_0.txt");
        System.out.println(problem);
        DdoModel<MaxCoverState> model = new DdoModel<>() {
            @Override
            public Problem<MaxCoverState> problem() {
                return problem;
            }

            @Override
            public MaxCoverRelax relaxation() {
                return new MaxCoverRelax(problem);
            }

            @Override
            public MaxCoverRanking ranking() {
                return new MaxCoverRanking();
            }

            @Override
            public WidthHeuristic<MaxCoverState> widthHeuristic() {
                return new FixedWidth<>(10);
            }

            @Override
            public MaxCoverFastLowerBound lowerBound() {
                return new MaxCoverFastLowerBound(problem);
            }

            @Override
            public boolean exportDot() {
                return false;
            }
        };

        Solution solution = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println();
        System.out.println(solution);
    }

}
