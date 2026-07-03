package org.ddolib.examples.nolayer.misp;

import org.ddolib.nolayer.modeling.Solvers;
import org.ddolib.nolayer.solver.Solution;
import org.ddolib.common.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public final class MispAStarMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MISP", "tadpole_4_2.dot").toString() : args[0];
        final MispProblem problem = MispProblem.fromFile(instance);
        final MispModel model = new MispModel(problem);


        Solution bestSolution = Solvers.minimizeAstar(model,
                (sol, stats) -> {
                    SolutionPrinter.printSolution(stats, sol);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal MISP value: " + -bestSolution.statistics().incumbent());
    }
}
