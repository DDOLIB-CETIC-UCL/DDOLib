package org.ddolib.examples.setcover.elementlayer;

import org.apache.commons.cli.Options;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.apache.commons.cli.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.ddolib.examples.LaunchInterface.*;

public class SetCoverLauncher {
    static final String DEFAULT_DIST = "jac";

    public static void main(String[] args) throws IOException {
        Options options = defaultOptions();
        String quotedValidDistance = distanceMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        options.addOption(Option.builder().longOpt("weighted").hasArg()
                .desc("weighted version of the problem").build());

        options.addOption(Option.builder().longOpt("distance").argName("DISTANCETYPE").hasArg()
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

        CmdInput input = parseDefaultCommandLine(cmd, options);
        boolean weighted = cmd.hasOption("weighted");

        if (cmd.hasOption("distance")) {
            distanceTypeStr = cmd.getOptionValue("distance");
            if (!distanceMap.containsKey(distanceTypeStr))
                throw new IllegalArgumentException("Unknown distance: " + distanceTypeStr + "\nValid distance are: " + quotedValidDistance);
        }

        DistanceType distanceType = distanceMap.get(distanceTypeStr);

        final SetCoverProblem problem = new SetCoverProblem(input.instancePath, weighted);

        DdoModel<SetCoverState> model = new DdoModel<>() {
            @Override
            public Problem<SetCoverState> problem() {
                return problem;
            }

            @Override
            public Relaxation<SetCoverState> relaxation() {
                return new SetCoverRelax();
            }

            @Override
            public StateRanking<SetCoverState> ranking() {
                return new SetCoverRanking();
            }

            @Override
            public FastLowerBound<SetCoverState> lowerBound() {
                return new DefaultFastLowerBound<>();
            }

            @Override
            public WidthHeuristic<SetCoverState> widthHeuristic() {
                return new FixedWidth<>((int) Math.ceil(input.widthFactor*problem.nbVars()));
            }

            @Override
            public ReductionStrategy<SetCoverState> relaxStrategy() {
                ReductionStrategy<SetCoverState> relaxStrat = null;
                switch (input.relaxStrat) {
                    case Cost -> relaxStrat = new CostBased<>(ranking());
                    case GHP -> relaxStrat = new GHP<>(new SetCoverDistance(problem, distanceType), input.seed);
                    case Hybrid -> relaxStrat = new Hybrid<>(ranking(), new SetCoverDistance(problem, distanceType), input.hybridFactor, input.seed);
                    case Kmeans -> relaxStrat = new Kmeans<>(stateCoordinates());
                }
                return relaxStrat;
            }

            @Override
            public ReductionStrategy<SetCoverState> restrictStrategy() {
                ReductionStrategy<SetCoverState> restrictStrat = null;
                switch (input.restrictStrat) {
                    case Cost -> restrictStrat = new CostBased<>(ranking());
                    case GHP -> restrictStrat = new GHP<>(new SetCoverDistance(problem, distanceType), input.seed);
                    case Hybrid -> restrictStrat = new Hybrid<>(ranking(), new SetCoverDistance(problem, distanceType), input.hybridFactor, input.seed);
                    case Kmeans -> restrictStrat = new Kmeans<>(stateCoordinates());
                }
                return restrictStrat;
            }

            @Override
            public Frontier<SetCoverState> frontier() {
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
                    ";" + distanceTypeStr + "\n";
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
        SYM, // symmetric difference
        JAC, // jaccard distance
        DICE, // dice distance
        WSYM, // weighted symmetric difference
        WJAC // weighted jaccard distance
    }

    private final static HashMap<String, DistanceType> distanceMap = new HashMap() {
        {
            put("jac", DistanceType.JAC);
            put("dice", DistanceType.DICE);
            put("sym", DistanceType.SYM);
            put("wsym", DistanceType.WSYM);
            put("wjac", DistanceType.WJAC);
        }
    };
}
