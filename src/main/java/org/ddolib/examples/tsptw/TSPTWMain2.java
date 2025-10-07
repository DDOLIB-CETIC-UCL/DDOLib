package org.ddolib.examples.tsptw;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.ExactSolver;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solve;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The TSPTW (TSP with Time Windows) is
 * to find the shortest possible route for a salesman to visit
 * a set of customers (or nodes) exactly once
 * and return to the starting point, while respecting
 * specified time windows for each customer.
 */
public class TSPTWMain2 {

    /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWMain"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWMain -Dexec.args="<your file>
     * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.<br>
     * <p>
     * Given Data files comes from
     * <a href="https://lopez-ibanez.eu/tsptw-instances#makespan">López-Ibáñes and Blum benchmark instances</a>.
     */
    public static void main(String[] args) throws IOException {

        final String file = Paths.get("data", "TSPTW", "AFG", "rbg010a.tw").toString();

        DdoModel<TSPTWState> model = new DdoModel<>(){
            private TSPTWProblem problem;
            @Override
            public Problem<TSPTWState>  problem() {
                try {
                    problem = new TSPTWProblem(new TSPTWInstance(file));
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public TSPTWRelax relaxation() {
                return new TSPTWRelax(problem);
            }
            @Override
            public TSPTWRanking ranking() {
                return new TSPTWRanking();
            }
            @Override
            public TSPTWFastLowerBound lowerBound() {
                return new TSPTWFastLowerBound(problem);
            }
            @Override
            public DominanceChecker<TSPTWState> dominance() {
                return new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
            }

            @Override
            public WidthHeuristic<TSPTWState> widthHeuristic() {
                return new FixedWidth<>(500);
            }
        };

        Solve<TSPTWState> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeDdo(model);

        solve.onSolution(stats);
    }
}
