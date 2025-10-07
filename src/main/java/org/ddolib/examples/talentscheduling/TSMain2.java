package org.ddolib.examples.talentscheduling;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.examples.pigmentscheduling.PSProblem;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solve;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;

public class TSMain2 {


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
        DdoModel<TSState> model = new DdoModel<>() {
            private TSProblem problem;
            @Override
            public Problem<TSState> problem() {
                try {
                    problem = readFile(file);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Relaxation<TSState> relaxation() {
                return new TSRelax(problem);
            }
            @Override
            public TSRanking ranking() {
                return new TSRanking();
            }
            @Override
            public TSFastLowerBound lowerBound() {
                return new TSFastLowerBound(problem);
            }
        };

        Solve<TSState> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeDdo(model);

        solve.onSolution(stats);
    }
}
