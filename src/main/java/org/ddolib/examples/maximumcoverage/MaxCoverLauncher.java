package org.ddolib.examples.maximumcoverage;

import org.apache.commons.cli.*;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;

import java.io.FileWriter;
import java.io.IOException;

import static org.ddolib.examples.LaunchInterface.*;

public class MaxCoverLauncher {

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

        final MaxCoverProblem problem = new MaxCoverProblem(input.instancePath);

        DdoModel<MaxCoverState> model = new DdoModel<>() {
            @Override
            public Problem<MaxCoverState> problem() {
                return problem;
            }

            @Override
            public Relaxation<MaxCoverState> relaxation() {
                return new MaxCoverRelax(problem);
            }

            @Override
            public MaxCoverRanking ranking() {
                return new MaxCoverRanking();
            }

            @Override
            public FastLowerBound<MaxCoverState> lowerBound() {
                return new DefaultFastLowerBound<>();
            }

            @Override
            public WidthHeuristic<MaxCoverState> widthHeuristic() {
                return new FixedWidth<>((int) Math.ceil(input.widthFactor*problem.nbVars()));
            }

            @Override
            public ReductionStrategy<MaxCoverState> relaxStrategy() {
                ReductionStrategy<MaxCoverState> relaxStrat = null;
                switch (input.relaxStrat) {
                    case Cost -> relaxStrat = new CostBased<>(ranking());
                    case GHP -> relaxStrat = new GHP<>(new MaxCoverDistance(), input.seed);
                    case Hybrid -> relaxStrat = new Hybrid<>(ranking(), new MaxCoverDistance(), input.hybridFactor, input.seed);
                    case Kmeans -> relaxStrat = new Kmeans<>(stateCoordinates());
                }
                return relaxStrat;
            }

            @Override
            public ReductionStrategy<MaxCoverState> restrictStrategy() {
                ReductionStrategy<MaxCoverState> restrictStrat = null;
                switch (input.restrictStrat) {
                    case Cost -> restrictStrat = new CostBased<>(ranking());
                    case GHP -> restrictStrat = new GHP<>(new MaxCoverDistance(), input.seed);
                    case Hybrid -> restrictStrat = new Hybrid<>(ranking(), new MaxCoverDistance(), input.hybridFactor, input.seed);
                    case Kmeans -> restrictStrat = new Kmeans<>(stateCoordinates());
                }
                return restrictStrat;
            }

            @Override
            public Frontier<MaxCoverState> frontier() {
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
