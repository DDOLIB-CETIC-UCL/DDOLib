package org.ddolib.ddo.examples.alp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.heuristics.WidthHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.IOException;
import java.util.Arrays;

public final class ALP {

    public static void main(final String[] args) throws IOException {
        final String fileStr = "data/alp/alp_n50_r1_c2_std10_s0";
        ALPInstance instance = new ALPInstance(fileStr);
        ALPProblem problem = new ALPProblem(instance);
        ALPRelax relax = new ALPRelax(problem);
        ALPRanking ranking = new ALPRanking();

        WidthHeuristic<ALPState> width = new FixedWidth<>(250);
        VariableHeuristic<ALPState> variableHeuristic = new DefaultVariableHeuristic<>();
        Frontier<ALPState> frontier = new SimpleFrontier<>(ranking);

        Solver parallelSolver = new ParallelSolver<>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,variableHeuristic,
                ranking,width,frontier
        );

        Solver sequentialSolver = new SequentialSolver<>(
                problem,
                relax,variableHeuristic,
                ranking,width,frontier
        );

        Solver solver = sequentialSolver;

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        String[] solution = solver.bestSolution().map(decisions -> {
            ALPState curState = problem.initialState();
            String[] values = new String[problem.nbVars()];

            for (int i = 0; i < decisions.size(); i++) {
                final int index = i;
                RunwayState[] runwayStates = curState.runwayStates;
                Decision d = decisions.stream().filter(x -> x.var() == index).findFirst().get();
                ALPDecision alpD = problem.fromDecision(d.val());
                int aircraft = problem.next.get(alpD.aircraftClass).get(curState.remainingAircraftOfClass[alpD.aircraftClass]);
                int arrivalTime = problem.getArrivalTime(runwayStates,aircraft,alpD.runway);
                int cost = problem.transitionCost(curState,d);
                curState = problem.transition(curState,d);
                values[d.var()] = String.format("Aircraft :%d, landing :%d, cost :%d", aircraft, arrivalTime, cost);
            }
            return values;
        }).get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }
}