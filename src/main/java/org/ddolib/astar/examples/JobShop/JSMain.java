package org.ddolib.astar.examples.JobShop;


import org.ddolib.astar.examples.JobShop.JSDominance;
import org.ddolib.astar.examples.JobShop.JSInstance;
import org.ddolib.astar.examples.JobShop.JSProblem;
import org.ddolib.astar.examples.JobShop.Precedence;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import static org.ddolib.factory.Solvers.acsSolver;
import static org.ddolib.factory.Solvers.astarSolver;

public class JSMain {
    public static double main(String arg, String solver) {
        JSInstance instance = new JSInstance(arg, false);
        JSProblem problem = new JSProblem(instance);
        final VariableHeuristic<JSState> varh = new DefaultVariableHeuristic<JSState>();
        final JSFastUpperBound fub = new JSFastUpperBound(problem);
        final SimpleDominanceChecker<JSState, BitSet> dominance = new SimpleDominanceChecker<>(new JSDominance(problem),
                problem.nbVars());


        ArrayList<Precedence> preds = new ArrayList<>();

        for (int i = 0; i < instance.getnJobs(); i++) {
            for (int j = 0; j < instance.getnMachines() - 1; j++) {
                preds.add(new Precedence(i * instance.getnMachines() + j, i * instance.getnMachines() + j + 1));
            }
        }
        problem.addPrecedencesConstraint(preds);
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        if (solver.equals("Astar")){
            solverAstar.maximize(0, false);
            return solverAstar.bestValue().get();
        }else if (solver.equals("ACS")){
            solverACS.maximize(0, false);
            return solverACS.bestValue().get();
        }
        return -1.0;
    }
    public static void main(String args[]) {
        JSInstance instance = new JSInstance("data/JobShop/bigTest/jobshop_instance_2_3_6.txt", false);
        JSProblem problem = new JSProblem(instance);
        final VariableHeuristic<JSState> varh = new DefaultVariableHeuristic<JSState>();
        final JSFastUpperBound fub = new JSFastUpperBound(problem);
        final SimpleDominanceChecker<JSState, BitSet> dominance = new SimpleDominanceChecker<>(new JSDominance(problem),
                problem.nbVars());
//        final DefaultDominanceChecker<JSState> dominance = new DefaultDominanceChecker<>();


        ArrayList<Precedence> preds = new ArrayList<>();

        for (int i = 0; i < instance.getnJobs(); i++) {
            for (int j = 0; j < instance.getnMachines() - 1; j++) {
                preds.add(new Precedence(i * instance.getnMachines() + j, i * instance.getnMachines() + j + 1));
            }
        }
        problem.addPrecedencesConstraint(preds);

        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
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

//        System.out.println("Solving with A*");
//
//        start = System.currentTimeMillis();
//        stats = solverAstar.maximize(0, false);
//        duration = (System.currentTimeMillis() - start) / 1000.0;
//
//        System.out.println("Search statistics using ddo:" + stats);
//
//
//        solution = solverAstar.bestSolution().map(decisions -> {
//            int[] values = new int[problem.nbVars()];
//            for (Decision d : decisions) {
//                values[d.var()] = d.val();
//            }
//            return values;
//        }).get();
//
//        System.out.printf("Duration : %.3f seconds%n", duration);
//        System.out.printf("Objective: %f%n", solverAstar.bestValue().get());
//        System.out.printf("Solution : %s%n", Arrays.toString(solution));

    }
}
