package org.ddolib.examples.setcover.elementlayer;

import org.apache.commons.cli.Options;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.DefaultFastLowerBound;
import org.apache.commons.cli.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.ddolib.examples.setcover.elementlayer.SetCover.readInstance;
import static org.ddolib.examples.LaunchInterface.*;

public class SetCoverLoader {
    static final String DEFAULT_DIST = "jac";

    public static void main(String[] args) {
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

        SetCoverProblem problem = null;
        try {
            problem = readInstance(input.instancePath, weighted);
        } catch (IOException e) {
            System.err.println("Problem reading " + input.instancePath);
            System.exit(-1);
        }

        final SolverConfig <SetCoverState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SetCoverRelax();
        config.ranking = new SetCoverRanking();
        config.width = new FixedWidth<>((int) Math.ceil(input.widthFactor*problem.nbVars()));
        config.varh = new SetCoverHeuristics.MinWeight(problem);
        config.flb = new DefaultFastLowerBound<>();
        config.dominance = new DefaultDominanceChecker<>();
        config.distance = new SetCoverDistance(problem, distanceType);
        config.coordinates = new SetCoverCoordinates(problem);

        config.frontier = getFrontier(input.cutSetStr, config);
        config.relaxStrategy = getReductionStrategy(input.relaxStratStr, config);
        config.restrictStrategy = getReductionStrategy(input.restrictStratStr, config);

        Solver solver = getSolver(input.solverStr, config);

        SearchStatistics stats = solver.minimize();

        if (cmd.hasOption("csv")) {

            StringBuilder statsCsv = defaultStatsCsv(config, solver, stats, input, "sc");
            statsCsv.append(";").append(distanceTypeStr).append("\n");
            try {
                FileWriter statsFile = new FileWriter(cmd.getOptionValue("csv"), true);
                statsFile.write(statsCsv.toString());
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
