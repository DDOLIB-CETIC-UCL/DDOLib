package org.ddolib.examples.ddo;

import org.apache.commons.cli.*;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.ExactSolver;
import org.ddolib.ddo.core.solver.RelaxationSolver;
import org.ddolib.ddo.core.solver.RestrictionSolver;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.examples.ddo.knapsack.KSLoader;
import org.ddolib.examples.ddo.misp.MispLoader;
import org.ddolib.examples.ddo.mks.MKSLoader;
import org.ddolib.examples.ddo.setcover.elementlayer.SetCoverLoader;
import org.ddolib.examples.ddo.setcover.setlayer.SetCoverLoaderAlt;
import org.ddolib.modeling.DefaultFastUpperBound;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
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

    public static void main(String[] args) {
        String quotedValidProblem = problemMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidSolver = solverMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidClusterRelax = clusteringRelaxMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidClusterRestrict = clusteringRestrictMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        String quotedValidCutSet = cutSetMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        Options options = new Options();

        options.addOption(Option.builder("p").longOpt("problem").argName("PROBLEM").hasArg().required()
                .desc("problem to solve.\nValid problems are: " + quotedValidProblem).build());

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

        options.addOption(Option.builder().longOpt("seed").argName("SEED").hasArg()
                .desc("Seed").build());

        options.addOption(Option.builder().longOpt("csv").argName("CSVFILE").hasArg()
                .desc("Csv file to store stats").build());

        options.addOption(Option.builder().longOpt("kmeans-iter").argName("NBR_ITERATION").hasArg()
                .desc("Maximal number of iterations for the kmean algorithm (default is 50)").build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println(exp.getMessage());
            new HelpFormatter().printHelp("use ddolib", options);
            System.exit(1);
        }

        String instancePath = null;
        int timeLimit = DEFAULT_TIME_LIMIT;
        double widthFactor = DEFAULT_WIDTH_FACTOR;
        int seed = DEFAULT_SEED;
        int kmeansIter = DEFAULT_KMEANS_ITER;
        String solverStr = DEFAULT_SOLVER;
        String cutSetStr = DEFAULT_CUTSET;
        String relaxStratStr = DEFAULT_CLUSTER;
        String restrictStratStr = DEFAULT_CLUSTER;
        String problemStr = null;

        try {
            instancePath = cmd.getOptionValue("input");

            problemStr = cmd.getOptionValue("problem");
            if(!problemMap.containsKey(problemStr))
                throw new IllegalArgumentException("Unknown problem: " + problemStr + "\nValid problems are: " + quotedValidProblem);

            if (cmd.hasOption("solver")) {
                solverStr = cmd.getOptionValue("solver");
                if (!solverMap.containsKey(solverStr))
                    throw new IllegalArgumentException("Unknown solver: " + solverStr + "\nValid solvers are: " + quotedValidSolver);
            }

            if (cmd.hasOption("cutset")) {
                cutSetStr = cmd.getOptionValue("cutset");
                if (!cutSetMap.containsKey(cutSetStr))
                    throw new IllegalArgumentException("Unknown cutset: " + cutSetStr + "\nValid cutsets are:" + quotedValidCutSet);
            }

            if (cmd.hasOption("relax")) {
               relaxStratStr = cmd.getOptionValue("relax");
                if (!clusteringRelaxMap.containsKey(relaxStratStr))
                    throw new IllegalArgumentException("Unknown relax strat: " + relaxStratStr + "\nValid relax strats are:" + quotedValidClusterRelax);
            }

            if (cmd.hasOption("restrict")) {
                restrictStratStr = cmd.getOptionValue("restrict");
                if (!clusteringRestrictMap.containsKey(restrictStratStr))
                    throw new IllegalArgumentException("Unknown restrict strat: " + restrictStratStr + "\nValid restrict strats are:" + quotedValidClusterRestrict);
            }

            if (cmd.hasOption("time-limit")) {
                timeLimit = Integer.parseInt(cmd.getOptionValue("time-limit"));
            }

            if (cmd.hasOption("width-factor")) {
                widthFactor = Double.parseDouble(cmd.getOptionValue("width-factor"));
            }

            if (cmd.hasOption("kmeans-iter")) {
                kmeansIter = Integer.parseInt(cmd.getOptionValue("kmeans-iter"));
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CommandLineApp", options);
            System.exit(-1);
        }

        SolverType solverType = solverMap.get(solverStr);
        ProblemType problemType = problemMap.get(problemStr);
        ClusterStrat relaxStrat = clusteringRelaxMap.get(relaxStratStr);
        ClusterStrat restrictionStrat = clusteringRestrictMap.get(restrictStratStr);

        SolverConfig config = null ;
        switch (problemType) {
            case KS -> config = KSLoader.loadProblem(instancePath, widthFactor);
            case SC -> config = SetCoverLoader.loadProblem(instancePath, widthFactor);
            case SCS -> config = SetCoverLoaderAlt.loadProblem(instancePath, widthFactor);
            case MKS -> config = MKSLoader.loadProblem(instancePath, widthFactor);
            case MISP -> config = MispLoader.loadProblem(instancePath, widthFactor);
        }
        assert config != null;

        config.frontier = new SimpleFrontier<>(config.ranking, cutSetMap.get(cutSetStr));
        config.timeLimit = timeLimit;
        config.gapLimit = 0.0; // TODO add it to the interface

        switch (relaxStrat) {
            case Cost -> config.relaxStrategy = new CostBased(config.ranking);
            case CostUB -> config.relaxStrategy = new CostUBBased(config.ranking);
            case Kmeans -> {
                Kmeans relaxStrategy = new Kmeans(config.coordinates);
                relaxStrategy.setMaxIterations(kmeansIter);
                config.relaxStrategy = relaxStrategy;
            }
            case GHPMDP -> {
                GHP relaxStrategy = new GHP(config.distance);
                relaxStrategy.setMostDistantPivot(true);
                relaxStrategy.setSeed(seed);
                config.relaxStrategy = relaxStrategy;
            }
            case GHP -> {
                GHP relaxStrategy = new GHP(config.distance);
                relaxStrategy.setMostDistantPivot(false);
                relaxStrategy.setSeed(seed);
                config.relaxStrategy = relaxStrategy;
            }
            case MBP -> {
                MBP relaxStrategy = new MBP(config.distance);
                relaxStrategy.setSeed(seed);
                config.relaxStrategy = relaxStrategy;
            }
        }

        switch (restrictionStrat) {
            case Cost -> config.restrictStrategy = new CostBased(config.ranking);
            case CostUB -> config.restrictStrategy = new CostUBBased(config.ranking);
            case Kmeans -> {
                Kmeans restrictStrategy = new Kmeans(config.coordinates);
                restrictStrategy.setMaxIterations(kmeansIter);
                config.restrictStrategy = restrictStrategy;
            }
            case GHPMDP -> {
                GHP restrictStrategy = new GHP(config.distance);
                restrictStrategy.setMostDistantPivot(true);
                restrictStrategy.setSeed(seed);
                config.restrictStrategy = restrictStrategy;
            }
            case GHP -> {
                GHP restrictStrategy = new GHP(config.distance);
                restrictStrategy.setMostDistantPivot(false);
                restrictStrategy.setSeed(seed);
                config.restrictStrategy = restrictStrategy;
            }
            case MBP -> {
                MBP restrictStrategy = new MBP(config.distance);
                restrictStrategy.setSeed(seed);
                config.restrictStrategy = restrictStrategy;
            }
        }

        Solver solver = null;

        switch (solverType) {
            case EXACT -> solver = new ExactSolver<>(config);
            case SEQ -> solver = new SequentialSolver<>(config);
            case RELAX -> solver = new RelaxationSolver<>(config);
            case RESTRI -> solver = new RestrictionSolver<>(config);
        }

        SearchStatistics stats = solver.maximize();

        System.out.printf("Duration : %d%n", stats.runTimeMS());
        System.out.printf("Objective: %s%n", solver.bestValue().get());
        System.out.printf("Status : %s%n", stats.SearchStatus());
        System.out.printf("Iteration: %d%n", stats.nbIterations());

        if (cmd.hasOption("csv")) {
            StringBuilder statsString = new StringBuilder();
            statsString.append(instancePath).append(";"); // Name
            statsString.append(problemStr).append(";"); // Problem
            statsString.append(solverStr).append(";"); // Solver
            statsString.append(cutSetStr).append(";"); // Cutset
            statsString.append(relaxStratStr).append(";"); // RelaxStrat
            statsString.append(restrictionStrat).append(";"); // RestrictionStrat
            statsString.append(timeLimit).append(";"); // timelimit
            statsString.append(widthFactor).append(";"); // widthFactor
            statsString.append(kmeansIter).append(";");

            boolean useFUB = !(config.fub instanceof DefaultFastUpperBound);
            statsString.append(useFUB).append(";");
            boolean useDominance = !(config.dominance instanceof DefaultDominanceChecker);
            statsString.append(useDominance).append(";");
            statsString.append(config.problem.optimal()).append(";");
            statsString.append(solver.bestValue().get()).append(";"); // objective
            statsString.append(stats.runTimeMS()).append(";"); // runtime
            statsString.append(stats.Gap()).append(";"); // Gap
            statsString.append(stats.nbIterations()).append(";"); // nbIterations
            statsString.append(stats.SearchStatus()).append("\n"); // searchStatus

            try {
                FileWriter statsFile = new FileWriter(cmd.getOptionValue("csv"), true);
                statsFile.write(statsString.toString());
                statsFile.close();

            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
        }
    }

    private enum ProblemType {
        MKS, // multidimensional knapsack
        KS, // knapsack
        MISP, // minimum independent set problem,
        SC, // set cover problem
        SCS // set cover with set layer model
    }

    private final static HashMap<String, ProblemType> problemMap = new HashMap() {
        {
            put("mks", ProblemType.MKS);
            put("ks", ProblemType.KS);
            put("misp", ProblemType.MISP);
            put("sc", ProblemType.SC);
            put("scs", ProblemType.SCS);
        }
    };

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
        CostUB,
        Kmeans,
        GHP,
        GHPMDP,
        MBP
    }

    private final static HashMap<String, ClusterStrat> clusteringRelaxMap = new HashMap() {
        {
            put("Cost", ClusterStrat.Cost);
            put("CostUB", ClusterStrat.CostUB);
            put("Kmeans", ClusterStrat.Kmeans);
            put("GHP", ClusterStrat.GHP);
            put("GHPMDP", ClusterStrat.GHPMDP);
            put("MBP", ClusterStrat.MBP);
        }
    };

    private final static HashMap<String, ClusterStrat> clusteringRestrictMap = new HashMap() {
        {
            put("Cost", ClusterStrat.Cost);
            put("CostUB", ClusterStrat.CostUB);
            put("Kmeans", ClusterStrat.Kmeans);
            put("GHP", ClusterStrat.GHP);
            put("GHPMDP", ClusterStrat.GHPMDP);
            put("MBP", ClusterStrat.MBP);
        }
    };

    private final static HashMap<String, CutSetType> cutSetMap = new HashMap() {
        {
            put("frontier", CutSetType.Frontier);
            put("layer", CutSetType.LastExactLayer);
        }
    };

}
