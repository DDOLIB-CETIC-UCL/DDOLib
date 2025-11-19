package org.ddolib.examples.tsalt;

import org.apache.commons.cli.*;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;

import javax.sql.rowset.Predicate;
import java.io.FileWriter;
import java.io.IOException;

import static org.ddolib.examples.LaunchInterface.*;

public class TSLauncher {

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

        final TSProblem problem = new TSProblem(input.instancePath);

        DdoModel<TSState> model = new DdoModel<>() {
            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public Relaxation<TSState> relaxation() {
                return new TSRelax(problem);
            }

            @Override
            public TSRanking ranking() {
                return new TSRanking();
            }

            @Override
            public TSFastLowerBound lowerBound() {
                return new TSFastLowerBound(problem);
            }

            @Override
            public WidthHeuristic<TSState> widthHeuristic() {
                return new FixedWidth<>((int) Math.ceil(input.widthFactor*problem.nbVars()));
            }

            @Override
            public ReductionStrategy<TSState> relaxStrategy() {
                ReductionStrategy<TSState> relaxStrat = null;
                switch (input.relaxStrat) {
                    case Cost -> relaxStrat = new CostBased<>(ranking());
                    case GHP -> relaxStrat = new GHP<>(new TSDistance(problem), input.seed);
                    case Hybrid -> relaxStrat = new Hybrid<>(ranking(), new TSDistance(problem), input.hybridFactor, input.seed);
                    case Kmeans -> relaxStrat = new Kmeans<>(stateCoordinates());
                }
                return relaxStrat;
            }

            @Override
            public ReductionStrategy<TSState> restrictStrategy() {
                ReductionStrategy<TSState> restrictStrat = null;
                switch (input.restrictStrat) {
                    case Cost -> restrictStrat = new CostBased<>(ranking());
                    case GHP -> restrictStrat = new GHP<>(new TSDistance(problem), input.seed);
                    case Hybrid -> restrictStrat = new Hybrid<>(ranking(), new TSDistance(problem), input.hybridFactor, input.seed);
                    case Kmeans -> restrictStrat = new Kmeans<>(stateCoordinates());
                }
                return restrictStrat;
            }

            @Override
            public Frontier<TSState> frontier() {
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
