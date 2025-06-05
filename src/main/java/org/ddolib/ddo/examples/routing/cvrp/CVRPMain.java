package org.ddolib.ddo.examples.routing.cvrp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CVRPMain {

    public static void main(String[] args) throws IOException {
        final String fileName = Paths.get("data", "Routing", "CVRP", "Vrp-Set-E", "E", "E-n13-k4.vrp").toString();
        final CVRPProblem problem = CVRPIO.readInstance(fileName);

        final CVRPRelax relax = new CVRPRelax();
        final CVRPRanking ranking = new CVRPRanking();
        final FixedWidth<CVRPState> width = new FixedWidth<>(5000);
        final VariableHeuristic<CVRPState> varh = new DefaultVariableHeuristic<>();
        final SimpleFrontier<CVRPState> frontier = new SimpleFrontier<>(ranking);

        final Solver solver = new SequentialSolver<>(problem, relax, varh, ranking, width, frontier);

        long start = System.currentTimeMillis();
        solver.maximize();

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
            VRPDecision[] sol = Arrays.stream(solution).mapToObj(problem.decisions::get).toArray(VRPDecision[]::new);
            solutionStr = makeSolutionString(sol, problem.v);
        } else {
            solutionStr = "No solution found";
        }

        String bestStr = solver.bestValue().isPresent() ? "" + solver.bestValue().get() : "No value";

        System.out.printf("Instance : %s%n", fileName);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %s%n", bestStr);
        System.out.printf("Solution : %s%n", solutionStr);
    }

    private static String makeSolutionString(VRPDecision[] solution, int v) {
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>(v);
        Collections.fill(routes, new ArrayList<>());
        for (VRPDecision d : solution) {
            if (d.vehicle() == -1) {
                for (int vehicle = 0; vehicle < v; vehicle++) {
                    routes.get(vehicle).add(0);
                }
            } else {
                routes.get(d.vehicle()).add(d.node());
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int vehicle = 0; vehicle < v; vehicle++) {
            sb.append(String.format("Vehicle %d: ", vehicle));
            sb.append("0 -> ");
            ArrayList<Integer> route = routes.get(vehicle);
            String routeStr = route.stream().map(String::valueOf).collect(Collectors.joining(" -> "));
            sb.append(routeStr).append("\n");
        }

        return sb.toString();

    }
}
