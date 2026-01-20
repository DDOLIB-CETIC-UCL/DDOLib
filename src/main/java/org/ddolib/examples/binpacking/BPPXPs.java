package org.ddolib.examples.binpacking;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import static org.ddolib.examples.binpacking.BPP.extractFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class BPPXPs {

    protected static BPPProblem[] loadInstances() throws IOException {
        String instancePath = Path.of("data", "BPP").toString();
        System.out.println(instancePath);
        File instanceDir = new File(instancePath);
        String pattern = "Falkenauer_t60_\\d*.txt"; // regex

        File[] files = instanceDir.listFiles(f -> f.isFile() && f.getName().matches(pattern));
        BPPProblem[] problems = new BPPProblem[files.length];
        for (int i = 0; i < files.length; i++) {
            problems[i] = extractFile(files[i].getAbsolutePath());
        }

        return problems;
    }

    private static DdoModel<BPPState> getModel(BPPProblem problem,
                                              int maxWidth,
                                              ClusterType clusterType,
                                              long seed,
                                              int kmeansIter,
                                              double hybridFactor) {
        return getModel(problem, maxWidth, clusterType, clusterType, seed, kmeansIter, hybridFactor);
    }

    private static DdoModel<BPPState> getModel(BPPProblem problem,
                                              int maxWidth,
                                              ClusterType relaxType,
                                              ClusterType restrictType,
                                              long seed,
                                              int kmeansIter,
                                              double hybridFactor) {
        return new DdoModel<>() {
            @Override
            public Problem<BPPState> problem() {
                return problem;
            }

            @Override
            public BPPRelax relaxation() {
                return new BPPRelax(problem);
            }

            @Override
            public BPPRanking ranking() {
                return new BPPRanking();
            }

            @Override
            public WidthHeuristic<BPPState> widthHeuristic() {
                return new FixedWidth<>(maxWidth);
            }

            @Override
            public boolean exportDot() {
                return false;
            }

            @Override
            public StateDistance<BPPState> stateDistance() {
                return new BPPDistance(problem);
            }

            @Override
            public ReductionStrategy<BPPState> relaxStrategy() {
                ReductionStrategy<BPPState> strat = null;
                switch (relaxType) {
                    case Cost -> strat = new CostBased<>(new BPPRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new BPPCoordinates(problem), kmeansIter, false);
                    case Hybrid -> strat = new Hybrid<>(new BPPRanking(), stateDistance(), hybridFactor, seed);
                    case Random -> strat = new RandomBased<>(seed);
                }
                return strat;
            }

            @Override
            public ReductionStrategy<BPPState> restrictStrategy() {
                ReductionStrategy<BPPState> strat = null;
                switch (restrictType) {
                    case Cost -> strat = new CostBased<>(new BPPRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new BPPCoordinates(problem), kmeansIter, false);
                    case Hybrid -> strat = new Hybrid<>(new BPPRanking(), stateDistance(), hybridFactor, seed);
                    case Random -> strat = new RandomBased<>(seed);
                }
                return strat;
            }

            @Override
            public DominanceChecker<BPPState> dominance() {
                return new DefaultDominanceChecker<>();
            }

            @Override
            public Frontier<BPPState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.LastExactLayer);
            }

            @Override
            public FastLowerBound<BPPState> lowerBound() {
                return new BPPFastLowerBound(problem);
            }

            @Override
            public boolean useCache() {
                return false;
            }
        };
    }

    private static void xpRelaxation() throws IOException {
        BPPProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/relaxationsBPP.csv");
        writer.write("Instance;Optimal;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;Incumbent;RunTime(ms)\n");

        for (BPPProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (BPPXPs.ClusterType clusterType : new BPPXPs.ClusterType[]{ClusterType.GHP}) {
                    int[] kmeansIters = clusterType != BPPXPs.ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                    long[] ghpSeeds = clusterType != BPPXPs.ClusterType.GHP ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != BPPXPs.ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<BPPState> model = getModel(problem,
                                        maxWidth,
                                        clusterType,
                                        seed,
                                        kmeansIter,
                                        hybridFactor);
                                assert problem.name.isPresent();
                                double optimal = problem.optimal.isPresent() ? problem.optimal.get() : -1;
                                System.out.printf("%s %f %d %d %d %f %n", problem.name.get(), optimal, maxWidth, kmeansIter, seed, hybridFactor);
                                Solution solution = Solvers.relaxedDdo(model);

                                writer.append(String.format("%s;%f;%s;%d;%d;%d;%f;%f;%d%n",
                                        problem.name.get(),
                                        optimal,
                                        clusterType,
                                        maxWidth,
                                        seed,
                                        kmeansIter,
                                        hybridFactor,
                                        solution.value(),
                                        solution.statistics().runTimeMs()
                                ));
                                writer.flush();
                            }
                        }
                    }
                }
            }
        }
        writer.close();
    }

    private static void xpRestriction() throws IOException {
        BPPProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/restrictionBPP.csv");
        writer.write("Instance;Optimal;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;Incumbent;RunTime(ms)\n");

        for (BPPProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (BPPXPs.ClusterType clusterType : new BPPXPs.ClusterType[]{ClusterType.GHP}) {
                    int[] kmeansIters = clusterType != BPPXPs.ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                    long[] ghpSeeds = (clusterType != BPPXPs.ClusterType.GHP) && (clusterType != BPPXPs.ClusterType.Random) ? new long[]{465465} : new long[]{546351, 87676, 465465};
                    double[] hybridFactors = clusterType != BPPXPs.ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<BPPState> model = getModel(problem,
                                        maxWidth,
                                        clusterType,
                                        seed,
                                        kmeansIter,
                                        hybridFactor);
                                assert problem.name.isPresent();
                                double optimal = problem.optimal.isPresent() ? problem.optimal.get() : -1;
                                System.out.printf("%s %f %d %d %d %f %n", problem.name.get(), optimal, maxWidth, kmeansIter, seed, hybridFactor);
                                Solution solution = Solvers.restrictedDdo(model);

                                writer.append(String.format("%s;%f;%s;%d;%d;%d;%f;%f;%d%n",
                                        problem.name.get(),
                                        optimal,
                                        clusterType,
                                        maxWidth,
                                        seed,
                                        kmeansIter,
                                        hybridFactor,
                                        solution.value(),
                                        solution.statistics().runTimeMs()
                                ));
                                writer.flush();
                            }
                        }
                    }
                }
            }
        }
        writer.close();
    }

    private static void xpBnB(String instance) throws IOException {
        BPPProblem[] problems = loadInstances();
        BPPProblem problem = problems[0];
        String[] nameParts = instance.split("/");
        FileWriter writer = new FileWriter("results/" + nameParts[nameParts.length - 1].replace(".txt", ".csv"));
        // writer.write("Instance;RelaxType;RestrictType;MaxWidth;Seed;KmeansIter;HybridFactor;" +
        //        "Status;nbIterations;queueMaxSize;RunTimeMs(ms);Incumbent;Gap\n");

        int maxWidth = 60;
        //  int kmeansIter = -1;
        double hybridFactor = -1;
        BPPXPs.ClusterType[] relaxTypes = new BPPXPs.ClusterType[]{BPPXPs.ClusterType.Cost, BPPXPs.ClusterType.GHP, BPPXPs.ClusterType.Kmeans};
        BPPXPs.ClusterType[] restrictTypes = new BPPXPs.ClusterType[]{BPPXPs.ClusterType.Cost, BPPXPs.ClusterType.GHP, BPPXPs.ClusterType.Random, BPPXPs.ClusterType.Kmeans};
        for (BPPXPs.ClusterType relaxType: relaxTypes) {
            for (BPPXPs.ClusterType restrictType : restrictTypes) {
                int[] kmeansIters = (relaxType != BPPXPs.ClusterType.Kmeans && restrictType != BPPXPs.ClusterType.Kmeans ) ? new int[]{-1} : new int[]{5};
                long[] seeds = (relaxType != BPPXPs.ClusterType.GHP && restrictType != BPPXPs.ClusterType.GHP && restrictType != BPPXPs.ClusterType.Random) ? new long[]{465465} : new long[]{465465, 546351, 87676};
                for (long seed : seeds) {
                    for (int kmeansIter : kmeansIters) {
                        DdoModel<BPPState> model = getModel(problem,
                                maxWidth,
                                relaxType,
                                restrictType,
                                seed,
                                kmeansIter,
                                hybridFactor);
                        assert problem.name.isPresent();
                        System.out.printf("%s %s %d %d %d %f %n", problem.name.get(), restrictType, maxWidth, kmeansIter, seed, hybridFactor);
                        long startTime = System.currentTimeMillis();
                        Solution solution = Solvers.minimizeDdo(model, x -> (System.currentTimeMillis() - startTime >= 1000.0 * 300.0));

                        writer.append(String.format("%s;%s;%s;%d;%d;%d;%f;%s%n",
                                problem.name.get(),
                                relaxType,
                                restrictType,
                                maxWidth,
                                seed,
                                kmeansIter,
                                hybridFactor,
                                solution.statistics().toCSV()
                        ));
                        writer.flush();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            xpRelaxation();
            xpRestriction();
            // xpBnB(args[0]);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }


    }

    private enum ClusterType {
        Cost,
        GHP,
        Kmeans,
        Hybrid,
        Random
    }
    
}
