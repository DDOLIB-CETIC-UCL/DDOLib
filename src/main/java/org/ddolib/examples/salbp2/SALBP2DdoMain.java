package org.ddolib.examples.salbp2;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public class SALBP2DdoMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "SALBP2", "n20", "n_20-m_2-id_1.alb").toString() : args[0];
        final SALBP2Problem problem = new SALBP2Problem(instance);
        final DdoModel<SALBP2State> model = new DdoModel<>() {
            @Override
            public Problem<SALBP2State> problem() {return problem;}

            @Override
            public Relaxation<SALBP2State> relaxation() {
                return new SALBP2Relax(problem);
            }

            @Override
            public SALBP2Ranking ranking() {
                return new SALBP2Ranking();
            }

            @Override
            public WidthHeuristic<SALBP2State> widthHeuristic() {
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
