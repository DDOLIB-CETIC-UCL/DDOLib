package org.ddolib.examples;

import org.apache.commons.cli.*;
import org.ddolib.ddo.core.frontier.CutSetType;

import java.util.HashMap;
import java.util.stream.Collectors;

public abstract class LaunchInterface {

    static final int DEFAULT_SEED = 6354864;
    static final int DEFAULT_TIME_LIMIT = 1800;
    static final double DEFAULT_WIDTH_FACTOR = 1.0;
    static final SolverType DEFAULT_SOLVER = SolverType.SEQ;
    static final CutSetType DEFAULT_CUTSET = CutSetType.LastExactLayer;
    static final ClusterStrat DEFAULT_CLUSTER = ClusterStrat.Cost;
    static final int DEFAULT_KMEANS_ITER = 50;
    static final double DEFAULT_HYBRID_FACTOR = 0.5;

    public static Options defaultOptions() {
        String quotedValidSolver = solverMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidCluster = clusteringMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
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
                .desc("type of clustering for relaxation. \nValid clustering are: " + quotedValidCluster).build());

        options.addOption(Option.builder().longOpt("restrict").argName("CLUSTERTYPE").hasArg()
                .desc("type of clustering for restriction. \nValid clustering are " + quotedValidCluster).build());

        options.addOption(Option.builder("t").longOpt("time-limit").argName("TIMELIMIT").hasArg()
                .desc("Time limit in seconds.").build());

        options.addOption(Option.builder("w").longOpt("width-factor").argName("WIDTHFACTOR").hasArg()
                .desc("Factor used to scale the maximal width.").build());

        options.addOption(Option.builder().longOpt("seed").argName("SEED").hasArg()
                .desc("Seed").build());

        options.addOption(Option.builder().longOpt("csv").argName("CSVFILE").hasArg()
                .desc("Csv file to store stats").build());

        options.addOption(Option.builder().longOpt("kmeans-iter").argName("NBR_ITERATION").hasArg()
                .desc("Maximal number of iterations for the kmean algorithm (default is 50)").build());

        options.addOption(Option.builder().longOpt("hybrid-factor").argName("HYBRIDFACTOR").hasArg()
                .desc("Proportion of preserved nodes on each reduced layer").build());

        options.addOption(Option.builder().longOpt("export-graph").build());

        return options;
    }

    public static class CmdInput {
        public String instancePath = null;
        public int timeLimit = DEFAULT_TIME_LIMIT;
        public double widthFactor = DEFAULT_WIDTH_FACTOR;
        public int seed = DEFAULT_SEED;
        public int kmeansIter = DEFAULT_KMEANS_ITER;
        public SolverType solverType = DEFAULT_SOLVER;
        public CutSetType cutSetType = DEFAULT_CUTSET;
        public ClusterStrat relaxStrat = DEFAULT_CLUSTER;
        public ClusterStrat restrictStrat = DEFAULT_CLUSTER;
        public double hybridFactor = DEFAULT_HYBRID_FACTOR;
        public boolean exportGraph = false;

        public String toCsv() {
            return this.instancePath + ";" + // Name
                    this.solverType + ";" + // Solver
                    this.cutSetType + ";" + // Cutset
                    this.relaxStrat + ";" + // RelaxStrat
                    this.restrictStrat + ";" + // RestrictionStrat
                    this.timeLimit + ";" + // timelimit
                    this.widthFactor + ";" + // widthFactor
                    this.kmeansIter + ";" +
                    this.hybridFactor;
        }
    }

    public static CmdInput parseDefaultCommandLine(CommandLine cmd, Options options) {
        String quotedValidSolver = solverMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidClusterRelax = clusteringMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidClusterRestrict = clusteringMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidCutSet = cutSetMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        CmdInput input = new CmdInput();

        try {
            input.instancePath = cmd.getOptionValue("input");

            if (cmd.hasOption("solver")) {
                String solverStr = cmd.getOptionValue("solver");
                if (!solverMap.containsKey(solverStr))
                    throw new IllegalArgumentException("Unknown solver: " + solverStr + "\nValid solvers are: " + quotedValidSolver);
                input.solverType = solverMap.get(solverStr);
            }

            if (cmd.hasOption("cutset")) {
                String cutSetStr = cmd.getOptionValue("cutset");
                if (!cutSetMap.containsKey(cutSetStr))
                    throw new IllegalArgumentException("Unknown cutset: " + cutSetStr + "\nValid cutsets are:" + quotedValidCutSet);
                input.cutSetType = cutSetMap.get(cutSetStr);
            }

            if (cmd.hasOption("relax")) {
                String relaxStratStr = cmd.getOptionValue("relax");
                if (!clusteringMap.containsKey(relaxStratStr))
                    throw new IllegalArgumentException("Unknown relax strat: " + relaxStratStr + "\nValid relax strats are:" + quotedValidClusterRelax);
                input.relaxStrat = clusteringMap.get(relaxStratStr);
            }

            if (cmd.hasOption("restrict")) {
                String restrictStratStr = cmd.getOptionValue("restrict");
                if (!clusteringMap.containsKey(restrictStratStr))
                    throw new IllegalArgumentException("Unknown restrict strat: " + restrictStratStr + "\nValid restrict strats are:" + quotedValidClusterRestrict);
                input.restrictStrat = clusteringMap.get(restrictStratStr);
            }

            if (cmd.hasOption("time-limit")) {
                input.timeLimit = Integer.parseInt(cmd.getOptionValue("time-limit"));
            }

            if (cmd.hasOption("width-factor")) {
                input.widthFactor = Double.parseDouble(cmd.getOptionValue("width-factor"));
            }

            if (cmd.hasOption("kmeans-iter")) {
                input.kmeansIter = Integer.parseInt(cmd.getOptionValue("kmeans-iter"));
            }

            if (cmd.hasOption("seed")) {
                input.seed = Integer.parseInt(cmd.getOptionValue("seed"));
            }

            if (cmd.hasOption("hybrid-factor")) {
                input.hybridFactor = Double.parseDouble(cmd.getOptionValue("hybrid-factor"));
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


    public enum SolverType {
        SEQ, // sequential solver
        RELAX, // relaxation solver
        RESTRI, // restriction solver
        EXACT // exact solver
    }

     public final static HashMap<String, SolverType> solverMap = new HashMap() {
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

    public final static HashMap<String, ClusterStrat> clusteringMap = new HashMap() {
        {
            put("Cost", ClusterStrat.Cost);
            put("Kmeans", ClusterStrat.Kmeans);
            put("GHP", ClusterStrat.GHP);
            put("Hybrid", ClusterStrat.Hybrid);
        }
    };


    public final static HashMap<String, CutSetType> cutSetMap = new HashMap() {
        {
            put("frontier", CutSetType.Frontier);
            put("layer", CutSetType.LastExactLayer);
        }
    };

}
