package org.ddolib.examples.misp;

import org.apache.commons.cli.*;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.misp.*;
import org.ddolib.examples.mks.*;
import org.ddolib.modeling.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;

import static org.ddolib.examples.LaunchInterface.*;

public class MispLauncher {

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

        final MispProblem problem = new MispProblem(input.instancePath);

        DdoModel<BitSet> model = new DdoModel<>() {
            @Override
            public Problem<BitSet> problem() {
                return problem;
            }

            @Override
            public Relaxation<BitSet> relaxation() {
                return new MispRelax(problem);
            }

            @Override
            public StateRanking<BitSet> ranking() {
                return new MispRanking();
            }

            @Override
            public FastLowerBound<BitSet> lowerBound() {
                return new MispFastLowerBound(problem);
            }

            @Override
            public WidthHeuristic<BitSet> widthHeuristic() {
                return new FixedWidth<>((int) Math.ceil(input.widthFactor*problem.nbVars()));
            }

            @Override
            public ReductionStrategy<BitSet> relaxStrategy() {
                ReductionStrategy<BitSet> relaxStrat = null;
                switch (input.relaxStrat) {
                    case Cost -> relaxStrat = new CostBased<>(ranking());
                    case GHP -> relaxStrat = new GHP<>(new MispDistance(), input.seed);
                    case Hybrid -> relaxStrat = new Hybrid<>(ranking(), new MispDistance(), input.hybridFactor, input.seed);
                    case Kmeans -> relaxStrat = new Kmeans<>(stateCoordinates());
                }
                return relaxStrat;
            }

            @Override
            public ReductionStrategy<BitSet> restrictStrategy() {
                ReductionStrategy<BitSet> restrictStrat = null;
                switch (input.restrictStrat) {
                    case Cost -> restrictStrat = new CostBased<>(ranking());
                    case GHP -> restrictStrat = new GHP<>(new MispDistance(), input.seed);
                    case Hybrid -> restrictStrat = new Hybrid<>(ranking(), new MispDistance(), input.hybridFactor, input.seed);
                    case Kmeans -> restrictStrat = new Kmeans<>(stateCoordinates());
                }
                return restrictStrat;
            }

            @Override
            public Frontier<BitSet> frontier() {
                return new SimpleFrontier<>(ranking(), input.cutSetType);
            }

        };

        SearchStatistics stats = null;
        long startTime = System.currentTimeMillis();

        // Predicate<> timeLimit = i -> (System.currentTimeMillis() - startTime >= input.timeLimit*1000.0);
        switch (input.solverType) {
            case RELAX -> stats = Solvers.relaxedDdo(model);
            case RESTRI -> stats = Solvers.restrictedDdo(model);
            case SEQ -> Solvers.minimizeDdo(model,
                    i -> (System.currentTimeMillis() - startTime >= input.timeLimit*1000.0));
            case EXACT -> Solvers.minimizeExact(model);
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
