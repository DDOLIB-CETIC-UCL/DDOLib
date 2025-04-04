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

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;

public class TalentScheduling {


    /**
     * Read data file following the format of
     * <a href="https://people.eng.unimelb.edu.au/pstuckey/talent/">https://people.eng.unimelb.edu.au/pstuckey/talent/</a>
     *
     * @param fileName The name of the file.
     * @return An instance the talent scheduling problem.
     * @throws IOException If something goes wrong while reading input file.
     */
    public static TalentSchedulingProblem readFile(String fileName) throws IOException {
        int nbScenes = 0;
        int nbActors = 0;
        int[] cost = new int[0];
        int[] duration = new int[0];
        BitSet[] actors = new BitSet[0];
        Optional<Integer> opti = Optional.empty();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;

            int lineCount = 0;
            int skip = 0;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    skip++;
                } else if (lineCount == 0) {
                    String[] tokens = line.split("\\s+");
                    if (tokens.length == 3) {
                        opti = Optional.of(Integer.parseInt(tokens[2]));
                    }
                } else if (lineCount == 1) {
                    nbScenes = Integer.parseInt(line);
                    duration = new int[nbScenes];
                } else if (lineCount == 2) {
                    nbActors = Integer.parseInt(line);
                    cost = new int[nbActors];
                    actors = new BitSet[nbScenes];
                    for (int i = 0; i < nbScenes; i++) {
                        actors[i] = new BitSet(nbActors);
                    }
                } else if (lineCount - skip - 3 < nbActors) {
                    int actor = lineCount - skip - 3;
                    String[] tokens = line.split("\\s+");
                    cost[actor] = Integer.parseInt(tokens[nbScenes]);
                    for (int i = 0; i < nbScenes; i++) {
                        int x = Integer.parseInt(tokens[i]);
                        if (Integer.parseInt(tokens[i]) == 1) {
                            actors[i].set(actor);
                        }
                    }
                } else {
                    String[] tokens = line.split("\\s+");
                    for (int i = 0; i < nbScenes; i++) {
                        duration[i] = Integer.parseInt(tokens[i]);
                    }
                }
                lineCount++;
            }

            return new TalentSchedulingProblem(nbScenes, nbActors, cost, duration, actors, opti);
        }
    }

    public static void main(String[] args) throws IOException {
        String file = "data/TalentScheduling/concert";

        final TalentSchedulingProblem problem = readFile(file);
        final TalentSchedRelax relax = new TalentSchedRelax(problem);
        final TalentSchedRanking ranking = new TalentSchedRanking();

        final WidthHeuristic<TalentSchedState> width = new FixedWidth<>(2);
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
