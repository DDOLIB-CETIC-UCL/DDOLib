package org.ddolib.examples.misp;

import org.apache.commons.cli.*;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.examples.LaunchInterface;
import org.ddolib.examples.mks.*;
import org.ddolib.modeling.DefaultFastLowerBound;

import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;

import static org.ddolib.examples.LaunchInterface.*;
import static org.ddolib.examples.LaunchInterface.defaultStatsCsv;
import static org.ddolib.examples.misp.MispMain.readFile;

public class MispLoader {

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

        MispProblem problem = null;
        try {
            problem = readFile(input.instancePath);
        } catch (IOException e) {
            System.err.println("Problem reading " + input.instancePath);
            System.exit(-1);
        }

        final SolverConfig<BitSet, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new MispRelax(problem);
        config.ranking = new MispRanking();
        config.width = new FixedWidth<>((int) Math.ceil(input.widthFactor*problem.nbVars()));
        config.varh = new DefaultVariableHeuristic<>();
        config.flb = new MispFastLowerBound(problem);
        config.dominance = new DefaultDominanceChecker<>();
        config.distance = new MispDistance();

        config.frontier = getFrontier(input.cutSetStr, config);
        config.relaxStrategy = getReductionStrategy(input.relaxStratStr, config);
        config.restrictStrategy = getReductionStrategy(input.restrictStratStr, config);

        Solver solver = getSolver(input.solverStr, config);

        SearchStatistics stats = solver.minimize();

        if (cmd.hasOption("csv")) {

            StringBuilder statsCsv = defaultStatsCsv(config, solver, stats, input, "misp").append("\n");
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
