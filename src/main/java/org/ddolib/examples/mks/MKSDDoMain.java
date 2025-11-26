package org.ddolib.examples.mks;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.mks.*;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public class MKSDDoMain {
    public static void main(String[] args) throws IOException {
        MKSProblem problem = new MKSProblem(Path.of("data", "MKS", "or-library", "mknapcb1_1.txt").toString());

        DdoModel<MKSState> model = new DdoModel<>() {
            @Override
            public Problem<MKSState> problem() {
                return problem;
            }

            @Override
            public MKSRelax relaxation() {
                return new MKSRelax();
            }

            @Override
            public MKSRanking ranking() {
                return new MKSRanking();
            }

            @Override
            public WidthHeuristic<MKSState> widthHeuristic() {
                return new FixedWidth<>(100);
            }

            @Override
            public boolean exportDot() {
                return true;
            }

            @Override
            public ReductionStrategy<MKSState> relaxStrategy() {
                return new GHP<>(new MKSDistance());
                // return new Hybrid<>(new MKSRanking(), new MKSDistance(problem));
                // return new CostBased<>(new MKSRanking());
            }

            @Override
            public ReductionStrategy<MKSState> restrictStrategy() {
                return new CostBased<>(new MKSRanking());
                // return new Kmeans<>(new MKSCoordinates(problem));
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println();
        System.out.println(stats);
    }

}
