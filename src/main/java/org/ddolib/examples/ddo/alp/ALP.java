package org.ddolib.examples.ddo.alp;

import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.ddo.core.heuristics.variable.*;
import org.ddolib.ddo.core.heuristics.width.*;

import javax.lang.model.type.NullType;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public final class ALP {

    public static void main(final String[] args) throws IOException {
        final String fileStr = Paths.get("data", "alp", "alp_n50_r1_c2_std10_s0").toString();
        ALPInstance instance = new ALPInstance(fileStr);
        ALPProblem problem = new ALPProblem(instance);
        ALPRelax relax = new ALPRelax(problem);
        ALPFastUpperBound fub = new ALPFastUpperBound(problem);
        ALPRanking ranking = new ALPRanking();

        WidthHeuristic<ALPState> width = new FixedWidth<>(100);
        VariableHeuristic<ALPState> variableHeuristic = new DefaultVariableHeuristic<>();
        Frontier<ALPState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        SolverConfig<ALPState, NullType> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = relax;
        config.fub = fub;
        config.ranking = ranking;
        config.width = width;
        config.varh = variableHeuristic;
        config.frontier = frontier;

        Solver solver = new SequentialSolver<>(config);

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
                int aircraft = problem.latestToEarliestAircraftByClass.get(alpD.aircraftClass).get(curState.remainingAircraftOfClass[alpD.aircraftClass]);
                int arrivalTime = problem.getArrivalTime(runwayStates, aircraft, alpD.runway);
                double cost = problem.transitionCost(curState, d);
                curState = problem.transition(curState, d);
                values[d.var()] = String.format("Aircraft :%d, landing :%d, cost :%,.2f", aircraft, arrivalTime, cost);
            }
            return values;
        }).get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %,.2f%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }
}