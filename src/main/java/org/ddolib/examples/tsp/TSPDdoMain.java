package org.ddolib.examples.tsp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;

public class TSPDdoMain {

    public static void main(final String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TSP", "instance_18_0.xml").toString() : args[0];
        final TSPProblem problem = new TSPProblem(instance);
        DdoModel<TSPState> model = new DdoModel<TSPState>() {
            @Override
            public Problem<TSPState> problem() {
                return problem;
            }

            @Override
            public Relaxation<TSPState> relaxation() {
                return new TSPRelax(problem);
            }

            @Override
            public TSPRanking ranking() {
                return new TSPRanking();
            }

            @Override
            public TSPFastLowerBound lowerBound() {
                return new TSPFastLowerBound(problem);
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public WidthHeuristic<TSPState> widthHeuristic() {
                return new FixedWidth<>(500);
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println(stats);

    }


}
