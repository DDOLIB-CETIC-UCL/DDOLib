package org.ddolib.ddo.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.WidthHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.Solver;
import org.ddolib.ddo.lib.heuristics.variables.DefaultVariableHeuristic;
import org.ddolib.ddo.lib.heuristics.width.FixedWidth;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;

import static org.ddolib.ddo.api.Solvers.sequentialSolver;

public class TSMain {


    /**
     * Read data file following the format of
     * <a href="https://people.eng.unimelb.edu.au/pstuckey/talent/">https://people.eng.unimelb.edu.au/pstuckey/talent/</a>
     *
     * @param fileName The name of the file.
     * @return An instance the talent scheduling problem.
     * @throws IOException If something goes wrong while reading input file.
     */
    public static TSProblem readFile(String fileName) throws IOException {
        int nbScenes = 0;
        int nbActors = 0;
        int[] cost = new int[0];
        int[] duration = new int[0];
        BitSet[] actors = new BitSet[0];
        Optional<Double> opti = Optional.empty();

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
                        opti = Optional.of(Double.parseDouble(tokens[2]));
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

            return new TSProblem(nbScenes, nbActors, cost, duration, actors, opti);
        }
    }

    public static void main(String[] args) throws IOException {
        String file = args.length == 0 ? Paths.get("data", "TalentScheduling", "film-12").toString() : args[0];
        int maxWidth = args.length >= 2 ? Integer.parseInt(args[1]) : 50;

        final TSProblem problem = readFile(file);
        final TSRelax relax = new TSRelax(problem);
        final TSRanking ranking = new TSRanking();
        final TSFastUpperBound fub = new TSFastUpperBound(problem);

        final WidthHeuristic<TSState> width = new FixedWidth<>(maxWidth);
        final VariableHeuristic<TSState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                fub
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
