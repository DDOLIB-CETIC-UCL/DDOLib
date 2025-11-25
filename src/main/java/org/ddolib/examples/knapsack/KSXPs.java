package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.RelaxSearchStatistics;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class KSXPs {

    protected static KSProblem[] loadInstances() throws IOException {
        String instancePath = Path.of("data", "Knapsack", "Nafar_2024").toString();
        System.out.println(instancePath);
        File instanceDir = new File(instancePath);
        String pattern = "KP_\\d*.txt"; // regex

        File[] files = instanceDir.listFiles(f -> f.isFile() && f.getName().matches(pattern));
        KSProblem[] problems = new KSProblem[files.length];
        for (int i = 0; i < files.length; i++) {
            problems[i] = new KSProblem(files[i].getAbsolutePath());
        }

        return problems;
    }

    private static DdoModel<Integer> getModel(KSProblem problem,
                                                    int maxWidth,
                                                    ClusterType clusterType,
                                                    long seed,
                                                    int kmeansIter,
                                                    double hybridFactor) {
        return new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public KSRelax relaxation() {
                return new KSRelax();
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(maxWidth);
            }

            @Override
            public boolean exportDot() {
                return false;
            }

            @Override
            public StateDistance<Integer> stateDistance() {
                return new KSDistance();
            }

            @Override
            public ReductionStrategy<Integer> relaxStrategy() {
                ReductionStrategy<Integer> strat = null;
                switch (clusterType) {
                    case Cost -> strat = new CostBased<>(new KSRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new KSCoordinates(), kmeansIter);
                    case Hybrid -> strat = new Hybrid<>(new KSRanking(), stateDistance(), hybridFactor, seed);
                }
                return strat;
            }

            @Override
            public ReductionStrategy<Integer> restrictStrategy() {
                return relaxStrategy();
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new DefaultDominanceChecker<>();
                //return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public Frontier<Integer> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.LastExactLayer);
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public boolean useCache() {
                return false;
            }
        };
    }

    private static void xpRelaxation() throws IOException {
        KSProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/relaxationsKS.csv");
        writer.write("Instance;Optimal;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRelaxations;avgExactNodes;minExactNodes;maxExactNodes;avgMinCardinality;avgMaxCardinality;"+
                "avgAvgCardinality;avgMinDegradation;avgMaxDegradation;avgAvgDegradation\n");

        for (KSProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Hybrid, ClusterType.Kmeans}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5, 10, 50};
                    long[] ghpSeeds = clusterType != ClusterType.GHP ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<Integer> model = getModel(problem,
                                        maxWidth,
                                        clusterType,
                                        seed,
                                        kmeansIter,
                                        hybridFactor);
                                assert problem.name.isPresent();
                                double optimal = problem.optimal.isPresent() ? problem.optimal.get() : -1;
                                System.out.printf("%s %f %d %d %d %f %n", problem.name.get(), optimal, maxWidth, kmeansIter, seed, hybridFactor);
                                RelaxSearchStatistics stats = Solvers.relaxedDdo(model);

                                writer.append(String.format("%s;%f;%s;%d;%d;%d;%f;%s%n",
                                        problem.name.get(),
                                        optimal,
                                        clusterType,
                                        maxWidth,
                                        seed,
                                        kmeansIter,
                                        hybridFactor,
                                        stats
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

    public static void main(String[] args) {
        try {
            xpRelaxation();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }


    }

    private enum ClusterType {
        Cost,
        GHP,
        Kmeans,
        Hybrid
    }


}
