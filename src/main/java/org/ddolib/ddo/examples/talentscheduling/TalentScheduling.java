package org.ddolib.ddo.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.SearchStatistics;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.heuristics.WidthHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class TalentScheduling {


    /**
     * Read data file following the format of
     * <a href="https://people.eng.unimelb.edu.au/pstuckey/talent/">https://people.eng.unimelb.edu.au/pstuckey/talent/</a>
     *
     * @param fileName The name of the file.
     * @return An instance the talent scheduling problem.
     * @throws IOException If something goes wrong while reading input file.
     */
    private static TalentSchedInstance readFile(String fileName) throws IOException {
        int nbScenes = 0;
        int nbActors = 0;
        int[] cost = new int[0];
        int[] duration = new int[0];
        int[][] actors = new int[0][0];

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine(); // Skip first line

            int lineCount = 0;
            int skip = 0;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    skip++;
                } else if (lineCount == 0) {
                    nbScenes = Integer.parseInt(line);
                    duration = new int[nbScenes];
                } else if (lineCount == 1) {
                    nbActors = Integer.parseInt(line);
                    cost = new int[nbActors];
                    actors = new int[nbActors][nbScenes];
                } else if (lineCount - skip - 2 < nbActors) {
                    int actor = lineCount - skip - 2;
                    String[] tokens = line.split("\\s+");
                    cost[actor] = Integer.parseInt(tokens[nbScenes]);
                    for (int i = 0; i < nbScenes; i++) {
                        actors[actor][i] = Integer.parseInt(tokens[i]);
                    }
                } else {
                    String[] tokens = line.split("\\s+");
                    for (int i = 0; i < nbScenes; i++) {
                        duration[i] = Integer.parseInt(tokens[i]);
                    }
                }
                lineCount++;
            }

            return new TalentSchedInstance(nbScenes, nbActors, cost, duration, actors);
        }
    }

    public static void main(String[] args) throws IOException {
        String file = "data/TalentScheduling/tiny";

        final TalentSchedInstance instance = readFile(file);
        final TalentSchedulingProblem problem = new TalentSchedulingProblem(instance);

        final TalentSchedRelax relax = new TalentSchedRelax(problem.nbVars());
        final TalentSchedRanking ranking = new TalentSchedRanking();

        final WidthHeuristic<TalentSchedState> width = new FixedWidth<>(500);
        final VariableHeuristic<TalentSchedState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TalentSchedState> frontier = new SimpleFrontier<>(ranking);

        SequentialSolver<TalentSchedState> solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );

        long start = System.currentTimeMillis();
        SearchStatistics stat = solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        String solutionStr;
        if (solver.bestSolution().isPresent()) {
            int[] solution = solver.bestSolution()
                    .map(decisions -> {
                        int[] values = new int[problem.nbVars()];
                        for (Decision d : decisions) {
                            values[d.var()] = d.val();
                        }
                        return values;
                    })
                    .get();
            solutionStr = Arrays.toString(solution);

        } else {
            solutionStr = "No feasible solution found";
        }

        String bestStr = solver.bestValue().isPresent() ? "" + solver.bestValue().get() : "No value";


        System.out.printf("Instance : %s%n", file);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %s%n", bestStr);
        System.out.printf("Solution : %s%n", solutionStr);
        System.out.println(stat);


    }
}
