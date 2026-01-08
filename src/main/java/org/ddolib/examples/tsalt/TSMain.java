package org.ddolib.examples.tsalt;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;

public class TSMain {

    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TalentScheduling", "film-12").toString() : args[0];
        final TSProblem problem = new TSProblem(instance);
        DdoModel<TSState> model = new DdoModel<>() {
            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public Relaxation<TSState> relaxation() {
                return new TSRelax(problem);
            }

            @Override
            public TSRanking ranking() {
                return new TSRanking();
            }
        };

        Solution bestSolution = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
