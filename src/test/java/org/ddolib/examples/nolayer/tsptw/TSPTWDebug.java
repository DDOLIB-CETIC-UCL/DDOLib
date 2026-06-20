package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.common.solver.Solution;
import org.ddolib.solving.astar.core.solver.nolayer.AStarSolver;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TSPTWDebug {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src", "test", "resources", "TSPTW", "nbNodes_4_1.txt");
        System.out.println("Loading " + p);
        TSPTWProblem prob = TSPTWProblem.fromFile(p.toString());
        TSPTWModel model = new TSPTWModel(prob);
        
        AStarSolver<TSPTWState> solver = new AStarSolver<>(model);
        Solution sol = solver.minimize(s -> false, (solution, stats) -> {});
        
        System.out.println("Best value: " + sol.value());
        System.out.println("Solution: " + java.util.Arrays.toString(sol.solution()));
    }
}
