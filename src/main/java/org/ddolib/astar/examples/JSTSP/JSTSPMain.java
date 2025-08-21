package org.ddolib.astar.examples.JSTSP;

import org.ddolib.astar.core.solver.ACSSolver;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.DefaultDominance;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;

import java.util.Arrays;

import static org.ddolib.factory.Solvers.astarSolver;
import static org.ddolib.factory.Solvers.acsSolver;

public class JSTSPMain {
    public static void main(String[] args) {
        final String instanceName = "data/JSTSP/dummy";
        final String problemName = "problem 1";
        JSTSPInstance instance = JSTSPInstance.readFile(instanceName, problemName);
        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance,
                Integer.MAX_VALUE
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10,
                Integer.MAX_VALUE
        );
        System.out.println("Solving with ACS");

        long start = System.currentTimeMillis();
        SearchStatistics stats = solverACS.maximize(0, false);
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        System.out.println("Search statistics using ddo:" + stats);


        int[] solution = solverACS.bestSolution().map(decisions -> {
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            return values;
        }).get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %f%n", solverACS.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));

    }
}
