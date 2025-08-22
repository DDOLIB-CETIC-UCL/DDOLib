package org.ddolib.examples.astart.tsptw;

import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.examples.ddo.tsptw.TSPTWFastUpperBound;
import org.ddolib.examples.ddo.tsptw.TSPTWInstance;
import org.ddolib.examples.ddo.tsptw.TSPTWProblem;
import org.ddolib.examples.ddo.tsptw.TSPTWState;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AstarTSPTW {

    public static void main(String[] args) throws IOException {
        String file = Paths.get("data", "TSPTW", "nbNodes_4_1.txt").toString();
        final TSPTWProblem problem = new TSPTWProblem(new TSPTWInstance(file));
        final TSPTWFastUpperBound fub = new TSPTWFastUpperBound(problem);
        final VariableHeuristic<TSPTWState> varh = new DefaultVariableHeuristic<>();
        final DefaultDominanceChecker<TSPTWState> dominance = new DefaultDominanceChecker<>();

        Solver solver = new AStarSolver<>(problem, varh, fub, dominance);


        long start = System.currentTimeMillis();
        SearchStatistics stat = solver.maximize(0, 1, false);
        double duration = (System.currentTimeMillis() - start) / 1000.0;


        Optional<Set<Decision>> bestSol = solver.bestSolution();

        String solutionStr;
        if (bestSol.isPresent()) {
            int[] solution = bestSol.map(decisions -> {
                int[] values = new int[problem.nbVars()];
                for (Decision d : decisions) {
                    values[d.var()] = d.val();
                }
                return values;
            }).get();
            solutionStr = "0 -> " + Arrays.stream(solution)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(" -> "));
        } else {
            solutionStr = "No feasible solution";
        }

        String bestStr = solver.bestValue().isPresent() ? "" + solver.bestValue().get() : "No value";


        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %s%n", bestStr);
        System.out.printf("Solution : %s%n", solutionStr);


    }
}
