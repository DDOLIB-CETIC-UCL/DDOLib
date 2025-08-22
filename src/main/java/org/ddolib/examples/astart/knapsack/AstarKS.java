package org.ddolib.examples.astart.knapsack;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.examples.ddo.knapsack.KSFastUpperBound;
import org.ddolib.examples.ddo.knapsack.KSMain;
import org.ddolib.examples.ddo.knapsack.KSProblem;

import java.io.IOException;
import java.util.Arrays;

import static org.ddolib.factory.Solvers.astarSolver;

public class AstarKS {

    public static void main(String[] args) throws IOException {
        KSProblem problem = KSMain.readInstance("data/Knapsack/simple.txt");
        KSFastUpperBound fub = new KSFastUpperBound(problem);
        VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<>();

        Solver solver = astarSolver(problem, varh, fub, new DefaultDominanceChecker<>());

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution().map(decisions -> {
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            return values;
        }).get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));


    }
}
