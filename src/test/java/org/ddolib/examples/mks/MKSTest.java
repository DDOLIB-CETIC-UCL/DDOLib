package org.ddolib.examples.mks;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.heuristics.cluster.Kmeans;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.solver.ExactSolver;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.examples.maximumcoverage.MaxCoverProblem;
import org.ddolib.examples.maximumcoverage.MaxCoverTest;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MKSTest {
    private static class MKSBench extends ProblemTestBench<MKSState, MKSProblem> {

        @Override
        protected List<MKSProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "mks").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            return new MKSProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected DdoModel<MKSState> model(MKSProblem problem) {
            DdoModel<MKSState> model = new DdoModel<MKSState>() {
                @Override
                public Problem<MKSState> problem() {
                    return problem;
                }

                @Override
                public MKSRelax relaxation() {
                    return new MKSRelax();
                }

                @Override
                public MKSRanking ranking() {
                    return new MKSRanking();
                }

                @Override
                public WidthHeuristic<MKSState> widthHeuristic() {
                    return new FixedWidth<>(100);
                }

                @Override
                public ReductionStrategy<MKSState> relaxStrategy() {
                    return new GHP<>(new MKSDistance(problem));
                    // return new Hybrid<>(new MKSRanking(), new MKSDistance(problem));
                    // return new CostBased<>(new MKSRanking());
                }

                @Override
                public ReductionStrategy<MKSState> restrictStrategy() {
                    return new CostBased<>(new MKSRanking());
                    // return new Kmeans<>(new MKSCoordinates(problem));
                }
            };
            return model;
        }
    }

    @DisplayName("MKS")
    @TestFactory
    public Stream<DynamicTest> testMaxCover() {
        var bench = new MKSBench();
        bench.testRelaxation = true;
        bench.testFLB = false;
        bench.testDominance = false;
        bench.testCache = false;
        return bench.generateTests();
    }


    /*****************************************************************/

    private static Stream<MKSProblem> smallGeneratedInstances() {
        Random rnd = new Random(546468464);
        int nInstances = 20; // number of instances to generate
        int nDim = 2;
        int nObjet = 10;
        double maxCapa = 200;
        double maxProfit = 100;
        List<MKSProblem> problems = new ArrayList<>();

        for (int i = 0; i < nInstances; i++) {
            int[][] weight = new int[nObjet][nDim];
            int[] profit = new int[nObjet];
            for (int k = 0; k < nObjet; k++) {
                for (int j = 0; j < nDim; j++) {
                    weight[k][j] = (int) rnd.nextDouble(maxCapa / 4.0, 3.0 * maxCapa / 4.0);
                    profit[k] += (int) rnd.nextDouble(0, 3.0 * maxProfit / 4.0 );
                }
            }
            double[] capa = new double[nDim];
            for (int j = 0; j < nDim; j++) {
                capa[j] = rnd.nextDouble(3.0 * maxCapa / 4.0, maxCapa);
            }
            problems.add(new MKSProblem(capa, profit, weight, -1));
        }
        return problems.stream();
    }

    private double bruteForce(MKSProblem problem) {
        List<Set<Integer>> solutions = new ArrayList<>();
        List<double[]> capacity = new ArrayList<>();
        int ndim = problem.capa.length;
        for(int i = 0; i < problem.nbVars(); i++) {
            if (solutions.isEmpty()) {
                solutions.add(new HashSet<>());
                capacity.add(Arrays.copyOf(problem.capa, problem.capa.length));
            }

            for (int j = 0; j < solutions.size(); j++) {
                double[] newCapacity = Arrays.copyOf(capacity.get(j), ndim);
                boolean canAdd = true;
                for (int k = 0; k < newCapacity.length; k++) {
                    newCapacity[k] -= problem.weights[i][k];
                    if (newCapacity[k] < 0) {
                        canAdd = false;
                        break;
                    }
                }
                if (canAdd) {
                    capacity.add(newCapacity);
                    Set<Integer> solution = new HashSet<>(solutions.get(j));
                    solution.add(i);
                    solutions.add(solution);
                }
            }
        }

        int maxCost = -1;
        for (Set<Integer> solution : solutions) {
            int cost = 0;
            for (int elem: solution){
                cost += problem.profit[elem];
            }
            maxCost = Math.max(maxCost, cost);
        }

        return maxCost;
    }

}
