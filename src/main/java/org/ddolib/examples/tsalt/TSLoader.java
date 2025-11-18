package org.ddolib.examples.tsalt;

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
import org.ddolib.examples.misp.*;

import static org.ddolib.examples.LaunchInterface.*;
import static org.ddolib.examples.LaunchInterface.defaultStatsCsv;
import static org.ddolib.examples.tsalt.TSMain.readFile;

import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;

public class TSLoader {

    public static void main(String[] args) {
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


        LaunchInterface.CmdInput input = parseDefaultCommandLine(cmd, options);

        TSProblem problem = null;
        try {
            problem = readFile(input.instancePath);
        } catch (IOException e) {
            System.err.println("Problem reading " + input.instancePath);
            System.exit(-1);
        }

        final SolverConfig <TSState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.timeLimit = input.timeLimit;
        config.relax = new TSRelax(problem);
        config.ranking = new TSRanking();
        config.width = new FixedWidth<>((int) Math.ceil(input.widthFactor*problem.nbVars()));
        config.varh = new DefaultVariableHeuristic<TSState>();
        config.flb = new TSFastLowerBound(problem);
        config.distance = new TSDistance(problem);
        config.hybridFactor = input.hybridFactor;
        config.verbosityLevel = 2;
        // config.coordinates = new TSCoordinates();

        config.frontier = getFrontier(input.cutSetStr, config);
        config.relaxStrategy = getReductionStrategy(input.relaxStratStr, config);
        config.restrictStrategy = getReductionStrategy(input.restrictStratStr, config);

        Solver solver = getSolver(input.solverStr, config);

        SearchStatistics stats = solver.minimize();

        if (cmd.hasOption("csv")) {

            StringBuilder statsCsv = defaultStatsCsv(config, solver, stats, input, "ts").append("\n");
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
