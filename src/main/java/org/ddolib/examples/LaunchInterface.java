package org.ddolib.examples;

import org.apache.commons.cli.*;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.ExactSolver;
import org.ddolib.ddo.core.solver.RelaxationSolver;
import org.ddolib.ddo.core.solver.RestrictionSolver;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.examples.knapsack.KSLoader;
import org.ddolib.examples.misp.MispLoader;
import org.ddolib.examples.mks.MKSLoader;
import org.ddolib.examples.setcover.elementlayer.SetCoverLoader;
import org.ddolib.examples.setcover.setlayer.SetCoverLoaderAlt;
import org.ddolib.examples.tsalt.TSLoader;
import org.ddolib.modeling.DefaultFastLowerBound;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Collectors;

public class LaunchInterface {

    static final int DEFAULT_SEED = 6354864;
    static final int DEFAULT_TIME_LIMIT = 1800;
    static final double DEFAULT_WIDTH_FACTOR = 1.0;
    static final String DEFAULT_SOLVER = "sequential";
    static final String DEFAULT_CUTSET = "layer";
    static final String DEFAULT_CLUSTER = "Cost";
    static final int DEFAULT_KMEANS_ITER = 50;

    public static Options defaultOptions() {
        String quotedValidSolver = solverMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidClusterRelax = clusteringRelaxMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidClusterRestrict = clusteringRestrictMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidCutSet = cutSetMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        Options options = new Options();

        options.addOption(Option.builder("i").longOpt("input").argName("INSTANCE_FILE").hasArg().required()
                .desc("Input instance file.").build());

        options.addOption(Option.builder("s").longOpt("solver").argName("SOLVER").hasArg()
                .desc("used solver.\nValid solvers are: " + quotedValidSolver).build());

        options.addOption(Option.builder().longOpt("cutset").argName("CUTSETTYPE").hasArg()
                .desc("type of cutset. \nValid cutsets are: " + quotedValidCutSet).build());

        options.addOption(Option.builder().longOpt("relax").argName("CLUSTERTYPE").hasArg()
                .desc("type of clustering for relaxation. \nValid clustering are: " + quotedValidClusterRelax).build());

        options.addOption(Option.builder().longOpt("restrict").argName("CLUSTERTYPE").hasArg()
                .desc("type of clustering for restriction. \nValid clustering are " + quotedValidClusterRestrict).build());

        options.addOption(Option.builder("t").longOpt("time-limit").argName("TIMELIMIT").hasArg()
                .desc("Time limit in seconds.").build());

        options.addOption(Option.builder("w").longOpt("width-factor").argName("WIDTHFACTOR").hasArg()
                .desc("Factor used to scale the maximal width.").build());

        options.addOption(Option.builder("l").longOpt("width-limit").argName("WIDTHLIMIT").hasArg()
                .desc("Maximal width.").build());

        options.addOption(Option.builder().longOpt("seed").argName("SEED").hasArg()
                .desc("Seed").build());

        options.addOption(Option.builder().longOpt("csv").argName("CSVFILE").hasArg()
                .desc("Csv file to store stats").build());

        options.addOption(Option.builder().longOpt("kmeans-iter").argName("NBR_ITERATION").hasArg()
                .desc("Maximal number of iterations for the kmean algorithm (default is 50)").build());

        options.addOption(Option.builder().longOpt("export-graph").build());

        return options;
    }

    public static StringBuilder defaultStatsCsv(
            SolverConfig config,
            Solver solver,
            SearchStatistics stats,
            CmdInput input,
            String problem
            ) {
        StringBuilder statsString = new StringBuilder();
        statsString.append(input.instancePath).append(";"); // Name
        statsString.append(problem).append(";"); // Problem
        statsString.append(input.solverStr).append(";"); // Solver
        statsString.append(input.cutSetStr).append(";"); // Cutset
        statsString.append(config.relaxStrategy).append(";"); // RelaxStrat
        statsString.append(config.restrictStrategy).append(";"); // RestrictionStrat
        statsString.append(config.timeLimit).append(";"); // timelimit
        statsString.append(input.widthFactor).append(";"); // widthFactor
        statsString.append(input.kmeansIter).append(";");

        boolean useFLB = !(config.flb instanceof DefaultFastLowerBound<?>);
        statsString.append(useFLB).append(";");
        boolean useDominance = !(config.dominance instanceof DefaultDominanceChecker);
        statsString.append(useDominance).append(";");
        statsString.append(config.problem.optimalValue().orElse(-1)).append(";");
        statsString.append(solver.bestValue().get()).append(";"); // objective
        statsString.append(stats.runTimeMS()).append(";"); // runtime
        statsString.append(stats.Gap()).append(";"); // Gap
        statsString.append(stats.nbIterations()).append(";"); // nbIterations
        statsString.append(stats.SearchStatus()); // searchStatus

        return statsString;
    }

    public static class CmdInput {
        public String instancePath = null;
        public int timeLimit = DEFAULT_TIME_LIMIT;
        public double widthFactor = DEFAULT_WIDTH_FACTOR;
        public int widthLimit = -1;
        public int seed = DEFAULT_SEED;
        public int kmeansIter = DEFAULT_KMEANS_ITER;
        public String solverStr = DEFAULT_SOLVER;
        public String cutSetStr = DEFAULT_CUTSET;
        public String relaxStratStr = DEFAULT_CLUSTER;
        public String restrictStratStr = DEFAULT_CLUSTER;
        public boolean exportGraph = false;
    }

    public static CmdInput parseDefaultCommandLine(CommandLine cmd, Options options) {
        String quotedValidSolver = solverMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidClusterRelax = clusteringRelaxMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidClusterRestrict = clusteringRestrictMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidCutSet = cutSetMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        CmdInput input = new CmdInput();

        try {
            input.instancePath = cmd.getOptionValue("input");

            if (cmd.hasOption("solver")) {
                input.solverStr = cmd.getOptionValue("solver");
                if (!solverMap.containsKey(input.solverStr))
                    throw new IllegalArgumentException("Unknown solver: " + input.solverStr + "\nValid solvers are: " + quotedValidSolver);
            }

            if (cmd.hasOption("cutset")) {
                input.cutSetStr = cmd.getOptionValue("cutset");
                if (!cutSetMap.containsKey(input.cutSetStr))
                    throw new IllegalArgumentException("Unknown cutset: " + input.cutSetStr + "\nValid cutsets are:" + quotedValidCutSet);
            }

            if (cmd.hasOption("relax")) {
                input.relaxStratStr = cmd.getOptionValue("relax");
                if (!clusteringRelaxMap.containsKey(input.relaxStratStr))
                    throw new IllegalArgumentException("Unknown relax strat: " + input.relaxStratStr + "\nValid relax strats are:" + quotedValidClusterRelax);
            }

            if (cmd.hasOption("restrict")) {
                input.restrictStratStr = cmd.getOptionValue("restrict");
                if (!clusteringRestrictMap.containsKey(input.restrictStratStr))
                    throw new IllegalArgumentException("Unknown restrict strat: " + input.restrictStratStr + "\nValid restrict strats are:" + quotedValidClusterRestrict);
            }

            if (cmd.hasOption("time-limit")) {
                input.timeLimit = Integer.parseInt(cmd.getOptionValue("time-limit"));
            }

            if (cmd.hasOption("width-factor")) {
                input.widthFactor = Double.parseDouble(cmd.getOptionValue("width-factor"));
            }

            if (cmd.hasOption("width-limit")) {
                input.widthLimit = Integer.parseInt(cmd.getOptionValue("width-limit"));
            }

            if (cmd.hasOption("kmeans-iter")) {
                input.kmeansIter = Integer.parseInt(cmd.getOptionValue("kmeans-iter"));
            }

            if (cmd.hasOption("seed")) {
                input.seed = Integer.parseInt(cmd.getOptionValue("seed"));
            }

            input.exportGraph = cmd.hasOption("export-graph");

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CommandLineApp", options);
            System.exit(-1);
        }

        return input;
    }

    public static Frontier getFrontier(String cutSetStr, SolverConfig config) {
        return new SimpleFrontier<>(config.ranking, cutSetMap.get(cutSetStr));
    }

    public static ReductionStrategy getReductionStrategy(String stratStr, SolverConfig config) {
        ClusterStrat strat = clusteringRelaxMap.get(stratStr);
        ReductionStrategy reductionStrategy = null;
        switch (strat) {
            case Cost -> reductionStrategy = new CostBased(config.ranking);
            case Kmeans -> {
                Kmeans reduc = new Kmeans(config.coordinates);
                reduc.setMaxIterations(config.kMeansIter);
                reductionStrategy = reduc;
            }
            case GHP -> {
                GHP reduc = new GHP(config.distance);
                reduc.setSeed(config.seed);
                reductionStrategy = reduc;
            }
            case Hybrid -> {
                Hybrid reduc = new Hybrid<>(config.ranking, config.distance);
                reduc.setSeed(config.seed);
                reductionStrategy = reduc;
            }
        }
        return reductionStrategy;
    }

    public static Solver getSolver(String solverStr, SolverConfig config) {
        SolverType solverType = solverMap.get(solverStr);
        Solver solver = null;
        switch (solverType) {
            case EXACT -> solver = new ExactSolver<>(config);
            case SEQ -> solver = new SequentialSolver<>(config);
            case RELAX -> solver = new RelaxationSolver<>(config);
            case RESTRI -> solver = new RestrictionSolver<>(config);
        }
        return solver;
    }



    public enum SolverType {
        SEQ, // sequential solver
        RELAX, // relaxation solver
        RESTRI, // restriction solver
        EXACT // exact solver
    }

    private final static HashMap<String, SolverType> solverMap = new HashMap() {
        {
            put("sequential", SolverType.SEQ);
            put("relaxed", SolverType.RELAX);
            put("restricted", SolverType.RESTRI);
            put("exact", SolverType.EXACT);
        }
    };

    public enum ClusterStrat {
        Cost,
        Kmeans,
        GHP,
        Hybrid
    }

    private final static HashMap<String, ClusterStrat> clusteringRelaxMap = new HashMap() {
        {
            put("Cost", ClusterStrat.Cost);
            put("Kmeans", ClusterStrat.Kmeans);
            put("GHP", ClusterStrat.GHP);
            put("Hybrid", ClusterStrat.Hybrid);
        }
    };

    private final static HashMap<String, ClusterStrat> clusteringRestrictMap = new HashMap() {
        {
            put("Cost", ClusterStrat.Cost);
            put("Kmeans", ClusterStrat.Kmeans);
            put("GHP", ClusterStrat.GHP);
            put("Hybrid", ClusterStrat.Hybrid);
        }
    };

    private final static HashMap<String, CutSetType> cutSetMap = new HashMap() {
        {
            put("frontier", CutSetType.Frontier);
            put("layer", CutSetType.LastExactLayer);
        }
    };

}
