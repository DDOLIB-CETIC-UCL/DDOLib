package org.ddolib.examples.astart.knapsack;

import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.examples.ddo.knapsack.KSFastUpperBound;
import org.ddolib.examples.ddo.knapsack.KSMain;
import org.ddolib.examples.ddo.knapsack.KSProblem;

import java.io.IOException;
import java.util.Arrays;


public class AstarKS {

    public static void main(String[] args) throws IOException {
        KSProblem problem = KSMain.readInstance("data/Knapsack/simple.txt");
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.fub = new KSFastUpperBound(problem);
        config.varh = new DefaultVariableHeuristic<>();

        Solver solver = new AStarSolver<>(config);

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
