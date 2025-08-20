package org.ddolib.examples.ddo;

import org.apache.commons.cli.*;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.ClusterStrat;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.examples.ddo.knapsack.KSLoader;

import java.util.HashMap;
import java.util.stream.Collectors;

import static org.ddolib.factory.Solvers.*;

public class LaunchInterface {

    static final int DEFAULT_SEED = 6354864;
    static final int DEFAULT_TIME_LIMIT = 1800;
    static final double DEFAULT_WIDTH_FACTOR = 1.0;

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

        options.addOption(Option.builder("s").longOpt("solver").argName("SOLVER")
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

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println(exp.getMessage());
            new HelpFormatter().printHelp("use ddolib", options);
            System.exit(1);
        }

        SolverType solverType = null;
        ProblemType problemType = null;
        CutSetType cutSetType = null;
        ClusterStrat relaxStrat = null;
        ClusterStrat restrictionStrat = null;
        String instancePath = null;
        int timeLimit = DEFAULT_TIME_LIMIT;
        double widthFactor = DEFAULT_WIDTH_FACTOR;
        int seed = DEFAULT_SEED;
        try {
            instancePath = cmd.getOptionValue("input");

            String problemStr = cmd.getOptionValue("problem");
            if(!problemMap.containsKey(problemStr))
                throw new IllegalArgumentException("Unknown problem: " + problemStr + "\nValid problems are: " + quotedValidProblem);
            problemType = problemMap.get(problemStr);

            if (cmd.hasOption("solver")) {
                String solverStr = cmd.getOptionValue("solver");
                if (!solverMap.containsKey(solverStr))
                    throw new IllegalArgumentException("Unknown solver: " + solverStr + "\nValid solvers are: " + quotedValidSolver);
                solverType = solverMap.get(solverStr);
            } else solverType = SolverType.SEQ;

            if (cmd.hasOption("cutset")) {
                String cutSetStr = cmd.getOptionValue("cutset");
                if (!cutSetMap.containsKey(cutSetStr))
                    throw new IllegalArgumentException("Unknown cutset: " + cutSetStr + "\nValid cutsets are:" + quotedValidCutSet);
                cutSetType = cutSetMap.get(cutSetStr);
            } else cutSetType = CutSetType.LastExactLayer;

            if (cmd.hasOption("relax")) {
                String relaxStratStr = cmd.getOptionValue("relax");
                if (!clusteringRelaxMap.containsKey(relaxStratStr))
                    throw new IllegalArgumentException("Unknown relax strat: " + relaxStratStr + "\nValid relax strats are:" + quotedValidClusterRelax);
                relaxStrat = clusteringRelaxMap.get(relaxStratStr);
            } else  relaxStrat = ClusterStrat.Cost;

            if (cmd.hasOption("restrict")) {
                String restrictStratStr = cmd.getOptionValue("restrict");
                if (!clusteringRestrictMap.containsKey(restrictStratStr))
                    throw new IllegalArgumentException("Unknown restrict strat: " + restrictStratStr + "\nValid restrict strats are:" + quotedValidClusterRestrict);
                restrictionStrat = clusteringRestrictMap.get(restrictStratStr);
            } else restrictionStrat = ClusterStrat.Cost;

            if (cmd.hasOption("time-limit")) {
                timeLimit = Integer.parseInt(cmd.getOptionValue("time-limit"));
            }

            if (cmd.hasOption("width-factor")) {
                widthFactor = Double.parseDouble(cmd.getOptionValue("width-factor"));
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CommandLineApp", options);
            System.exit(-1);
        }

        ProblemLoader loader = null ;
        switch (problemType) {
            case KS -> loader = KSLoader.loadProblem(instancePath, widthFactor);
        }

        assert loader != null;
        Solver solver = null;

        switch (solverType) {
            case EXACT -> solver = exactSolver(loader.problem(),
                    loader.relax(),
                    loader.varh(),
                    loader.ranking(),
                    loader.fub(),
                    loader.dominance());
            case SEQ -> solver = sequentialSolver(loader.problem(),
                    loader.relax(),
                    loader.varh(),
                    loader.ranking(),
                    loader.width(),
                    new SimpleFrontier<>(loader.ranking(), cutSetType),
                    loader.fub(),
                    loader.dominance(),
                    timeLimit,
                    0.0,
                    relaxStrat,
                    restrictionStrat,
                    loader.distance(),
                    loader.coordinates(),
                    seed);
            case RELAX -> solver = relaxationSolver(loader.problem(),
                    loader.relax(),
                    loader.varh(),
                    loader.ranking(),
                    loader.width(),
                    loader.fub(),
                    loader.dominance(),
                    relaxStrat,
                    loader.distance(),
                    loader.coordinates(),
                    seed);
            case RESTRI -> solver = relaxationSolver(loader.problem(),
                    loader.relax(),
                    loader.varh(),
                    loader.ranking(),
                    loader.width(),
                    loader.fub(),
                    loader.dominance(),
                    restrictionStrat,
                    loader.distance(),
                    loader.coordinates(),
                    seed);
        }

        SearchStatistics stats = solver.maximize();
    }

    private static enum ProblemType {
        MKS, // multidimensional knapsack
        KS, // knapsack
        MISP, // minimum independent set problem,
        SC // set cover problem
    }

    private final static HashMap<String, ProblemType> problemMap = new HashMap() {
        {
            put("mks", ProblemType.MKS);
            put("ks", ProblemType.KS);
            put("misp", ProblemType.MISP);
            put("sc", ProblemType.SC);
        }
    };

    public static enum SolverType {
        SEQ, // sequential solver
        RELAX, // relaxation solver
        RESTRI, // restriction solver
        EXACT // exact solver
    }

    private final static HashMap<String, SolverType> solverMap = new HashMap() {
        {
            put("seq", SolverType.SEQ);
            put("relax", SolverType.RELAX);
            put("restri", SolverType.RESTRI);
            put("exact", SolverType.EXACT);
        }
    };

    private final static HashMap<String, ClusterStrat> clusteringRelaxMap = new HashMap() {
        {
            put("cost", ClusterStrat.Cost);
            put("Kmeans", ClusterStrat.Kmeans);
            put("GHP", ClusterStrat.GHPMD);
            put("GHPMDP", ClusterStrat.GHPMDPMD);
        }
    };

    private final static HashMap<String, ClusterStrat> clusteringRestrictMap = new HashMap() {
        {
            put("cost", ClusterStrat.Cost);
            put("Kmeans", ClusterStrat.Kmeans);
            put("GHP", ClusterStrat.GHPMD);
            put("GHPMDP", ClusterStrat.GHPMDPMD);
        }
    };

    private final static HashMap<String, CutSetType> cutSetMap = new HashMap() {
        {
            put("frontier", CutSetType.Frontier);
            put("layer", CutSetType.LastExactLayer);
        }
    };

}
