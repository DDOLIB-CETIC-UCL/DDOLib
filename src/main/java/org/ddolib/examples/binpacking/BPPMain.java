package org.ddolib.examples.binpacking;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;

public class BPPMain {


    public static void main(String[] args) throws IOException, InvalidSolutionException {

        BPPProblem problem = new BPPProblem("data/BPP/Falkenauer_t60_01.txt");

        BPPDdoModel model = new BPPDdoModel(problem, 10);
        BPPAcsModel model2 = new BPPAcsModel(problem);

        Solution bestSolution = Solvers.minimizeDdo(model, s -> s.runtime() > 60000,(sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });
        System.out.println(bestSolution);
        System.out.println(bestSolution.statistics());

        if (problem.optimal.isPresent()) {
            System.out.printf("Found : %f \t Optimal : %f\n", bestSolution.value(), problem.optimal.get());
        } else {
            System.out.printf("Found : %f\n", bestSolution.value());
        }
        try {
            problem.evaluate(bestSolution.solution());
        } catch (InvalidSolutionException e) {
            throw new RuntimeException(e);
        }
    }
}
