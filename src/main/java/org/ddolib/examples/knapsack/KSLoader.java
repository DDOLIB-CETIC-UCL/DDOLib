package org.ddolib.examples.knapsack;

import org.apache.commons.cli.*;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.examples.LaunchInterface;
import org.ddolib.examples.misp.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;

import static org.ddolib.examples.LaunchInterface.*;
import static org.ddolib.examples.LaunchInterface.defaultStatsCsv;
import static org.ddolib.examples.knapsack.KSMain.readInstance;
import static org.ddolib.examples.misp.MispMain.readFile;

public class KSLoader {

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

        KSProblem problem = null;
        try {
            problem = readInstance(input.instancePath);
        } catch (IOException e) {
            System.err.println("Problem reading " + input.instancePath);
            System.exit(-1);
        }

        final SolverConfig <Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>((int) Math.ceil(input.widthFactor*problem.nbVars()));
        config.varh = new DefaultVariableHeuristic<Integer>();
        config.flb = new KSFastLowerBound(problem);
        config.dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
        config.distance = new KSDistance();
        config.coordinates = new KSCoordinates();
        config.hybridFactor = input.hybridFactor;

        config.frontier = getFrontier(input.cutSetStr, config);
        config.relaxStrategy = getReductionStrategy(input.relaxStratStr, config);
        config.restrictStrategy = getReductionStrategy(input.restrictStratStr, config);

        Solver solver = getSolver(input.solverStr, config);

        SearchStatistics stats = solver.minimize();

        if (cmd.hasOption("csv")) {

            StringBuilder statsCsv = defaultStatsCsv(config, solver, stats, input, "ks").append("\n");
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
