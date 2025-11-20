package org.ddolib.examples.maximumcoverage;

import org.apache.commons.cli.*;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.setcover.elementlayer.SetCoverLauncher;
import org.ddolib.modeling.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.ddolib.examples.LaunchInterface.*;

public class MaxCoverLauncher {
    static final String DEFAULT_DIST = "jac";

    public static void main(String[] args) throws IOException {
        Options options = defaultOptions();

        String quotedValidDistance = distanceMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        options.addOption(Option.builder("d").longOpt("distance").argName("DISTANCETYPE").hasArg()
                .desc("type of distance (for set cover only). \nValid distances are: " + quotedValidDistance).build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println(exp.getMessage());
            new HelpFormatter().printHelp("use ddolib", options);
            System.exit(1);
        }

        String distanceTypeStr = DEFAULT_DIST;

        final CmdInput input = parseDefaultCommandLine(cmd, options);

        if (cmd.hasOption("distance")) {
            distanceTypeStr = cmd.getOptionValue("distance");
            if (!distanceMap.containsKey(distanceTypeStr))
                throw new IllegalArgumentException("Unknown distance: " + distanceTypeStr + "\nValid distance are: " + quotedValidDistance);
        }

        DistanceType distanceType = distanceMap.get(distanceTypeStr);

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
            public StateDistance<MaxCoverState> stateDistance() {
                switch (distanceType) {
                    case JAC -> {
                        return new MaxCoverDistance(problem);
                    }
                    case WJAC -> {
                        return new MaxCoverWeightedJaccardDistance(problem);
                    }
                    default -> throw new IllegalArgumentException("Unknown distance type: " + distanceType);
                }
            }

            @Override
            public ReductionStrategy<MaxCoverState> relaxStrategy() {
                ReductionStrategy<MaxCoverState> relaxStrat = null;
                switch (input.relaxStrat) {
                    case Cost -> relaxStrat = new CostBased<>(ranking());
                    case GHP -> relaxStrat = new GHP<>(stateDistance(), input.seed);
                    case Hybrid -> relaxStrat = new Hybrid<>(ranking(), stateDistance(), input.hybridFactor, input.seed);
                    case Kmeans -> relaxStrat = new Kmeans<>(stateCoordinates());
                }
                return relaxStrat;
            }

            @Override
            public ReductionStrategy<MaxCoverState> restrictStrategy() {
                ReductionStrategy<MaxCoverState> restrictStrat = null;
                switch (input.restrictStrat) {
                    case Cost -> restrictStrat = new CostBased<>(ranking());
                    case GHP -> restrictStrat = new GHP<>(stateDistance(), input.seed);
                    case Hybrid -> restrictStrat = new Hybrid<>(ranking(), stateDistance(), input.hybridFactor, input.seed);
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
            String statsCsv = input.toCsv() + ";" + problem.optimalValue().orElse(-1.0) + ";" + stats.toCsv() +
                    ";" + distanceType + "\n";
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

    public enum DistanceType {
        JAC, // jaccard distance
        WJAC // weighted jaccard distance
    }

    private final static HashMap<String, DistanceType> distanceMap = new HashMap() {
        {
            put("jac", DistanceType.JAC);
            put("wjac", DistanceType.WJAC);
        }
    };


}
