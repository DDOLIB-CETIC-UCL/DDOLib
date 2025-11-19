package org.ddolib.examples.knapsack;

import org.apache.commons.cli.*;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.knapsack.KSProblem;
import org.ddolib.examples.knapsack.KSRanking;
import org.ddolib.examples.misp.*;
import org.ddolib.modeling.*;

import java.io.FileWriter;
import java.io.IOException;

import static org.ddolib.examples.LaunchInterface.*;

public class KSLauncher {

    public static void main(String[] args) throws IOException {
        Options options = defaultOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println(exp.getMessage());
            new HelpFormatter().printHelp("use ddolib", options);
            System.exit(1);
        }


        final CmdInput input = parseDefaultCommandLine(cmd, options);

        final KSProblem problem = new KSProblem(input.instancePath);

        DdoModel<Integer> model = new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public Relaxation<Integer> relaxation() {
                return new KSRelax();
            }

            @Override
            public StateRanking<Integer> ranking() {
                return new KSRanking();
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>((int) Math.ceil(input.widthFactor*problem.nbVars()));
            }

            @Override
            public ReductionStrategy<Integer> relaxStrategy() {
                ReductionStrategy<Integer> relaxStrat = null;
                switch (input.relaxStrat) {
                    case Cost -> relaxStrat = new CostBased<>(ranking());
                    case GHP -> relaxStrat = new GHP<>(new KSDistance(), input.seed);
                    case Hybrid -> relaxStrat = new Hybrid<>(ranking(), new KSDistance(), input.hybridFactor, input.seed);
                    case Kmeans -> relaxStrat = new Kmeans<>(stateCoordinates());
                }
                return relaxStrat;
            }

            @Override
            public ReductionStrategy<Integer> restrictStrategy() {
                ReductionStrategy<Integer> restrictStrat = null;
                switch (input.restrictStrat) {
                    case Cost -> restrictStrat = new CostBased<>(ranking());
                    case GHP -> restrictStrat = new GHP<>(new KSDistance(), input.seed);
                    case Hybrid -> restrictStrat = new Hybrid<>(ranking(), new KSDistance(), input.hybridFactor, input.seed);
                    case Kmeans -> restrictStrat = new Kmeans<>(stateCoordinates());
                }
                return restrictStrat;
            }

            @Override
            public Frontier<Integer> frontier() {
                return new SimpleFrontier<>(ranking(), input.cutSetType);
            }

        };

        SearchStatistics stats = null;
        long startTime = System.currentTimeMillis();

        // Predicate<> timeLimit = i -> (System.currentTimeMillis() - startTime >= input.timeLimit*1000.0);
        switch (input.solverType) {
            case RELAX -> stats = Solvers.relaxedDdo(model);
            case RESTRI -> stats = Solvers.restrictedDdo(model);
            case SEQ -> stats = Solvers.minimizeDdo(model,
                    i -> (System.currentTimeMillis() - startTime >= input.timeLimit*1000.0));
            case EXACT -> stats = Solvers.minimizeExact(model);
        }

        assert stats != null;
        if (cmd.hasOption("csv")) {
            String statsCsv = input.toCsv() + ";" + problem.optimalValue().orElse(-1.0) + ";" + stats.toCsv() + "\n";
            try {
                FileWriter statsFile = new FileWriter(cmd.getOptionValue("csv"), true);
                statsFile.write(statsCsv);
                statsFile.close();

            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
        }

    }

}
