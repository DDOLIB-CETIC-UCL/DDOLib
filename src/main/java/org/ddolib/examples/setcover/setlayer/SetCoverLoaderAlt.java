package org.ddolib.examples.setcover.setlayer;

import org.apache.commons.cli.*;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.examples.LaunchInterface;
import org.ddolib.examples.knapsack.*;
import org.ddolib.modeling.DefaultFastLowerBound;

import java.io.FileWriter;
import java.io.IOException;

import static org.ddolib.examples.LaunchInterface.*;
import static org.ddolib.examples.LaunchInterface.defaultStatsCsv;
import static org.ddolib.examples.knapsack.KSMain.readInstance;

public class SetCoverLoaderAlt {

    public static void main(String[] args) {
        Options options = defaultOptions();

        options.addOption(Option.builder().longOpt("weighted").hasArg()
                .desc("weighted version of the problem").build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println(exp.getMessage());
            new HelpFormatter().printHelp("use ddolib", options);
            System.exit(1);
        }


        LaunchInterface.CmdInput input = parseDefaultCommandLine(cmd, options);
        boolean weighted = cmd.hasOption("weighted");

        SetCoverProblem problem = null;
        try {
            problem = SetCover.readInstance(input.instancePath, weighted);
        } catch (IOException e) {
            System.err.println("Problem reading " + input.instancePath);
            System.exit(-1);
        }

        final SolverConfig <SetCoverState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SetCoverRelax();
        config.ranking = new SetCoverRanking();
        config.width = new FixedWidth<>((int) Math.ceil(input.widthFactor*problem.nbVars()));
        config.varh = new SetCoverHeuristics.FocusClosingElements(problem);
        config.flb = new DefaultFastLowerBound<>();
        config.dominance = new DefaultDominanceChecker<>();
        config.distance = new SetCoverDistanceWeighted(problem);
        config.coordinates = new SetCoverCoordinates(problem);

        Solver solver = getSolver(input.solverStr, config);

        SearchStatistics stats = solver.minimize();

        if (cmd.hasOption("csv")) {

            StringBuilder statsCsv = defaultStatsCsv(config, solver, stats, input, "scs").append("\n");
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
}
