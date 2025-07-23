package org.ddolib.ddo.examples.mks;



import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ddolib.ddo.implem.solver.Solvers.exactSolver;
import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MKSTest {
    static Stream<MKSProblem> dataProvider1D() {
        Stream<Integer> testStream = IntStream.rangeClosed(0, 10).boxed();
        return testStream.flatMap(i -> {
            try {
                return Stream.of(MKSMain.readInstance("src/test/resources/MKS/instance_test_" + i));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    static Stream<MKSProblem> dataProviderMD() {
        Stream<Integer> testStream = IntStream.rangeClosed(1, 10).boxed();
        return testStream.flatMap(i -> {
            try {
                return Stream.of(MKSMain.readInstance("data/MKS/MKP_" + i+".txt"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @ParameterizedTest
    @MethodSource("dataProvider1D")
    public void testExactMKS(MKSProblem problem) {
        final MKSRelax relax = new MKSRelax();
        final MKSRanking ranking = new MKSRanking();
        final VariableHeuristic<MKSState> varh = new DefaultVariableHeuristic<>();

        final Solver solver = exactSolver(
                problem,
                relax,
                varh,
                ranking);

        solver.maximize();
        assertEquals(problem.optimal, solver.bestValue().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider1D")
    public void testSequentialMKS(MKSProblem problem) {
        final MKSRelax relax = new MKSRelax();
        final MKSRanking ranking = new MKSRanking();
        final FixedWidth<MKSState> width = new FixedWidth<>(50);
        final VariableHeuristic<MKSState> varh = new DefaultVariableHeuristic<>();
        final Frontier<MKSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        solver.maximize();
        assertEquals(problem.optimal, solver.bestValue().get());
    }

    /*@ParameterizedTest
    @MethodSource("dataProviderMD")
    public void testSequentialMKSMD(MKSProblem problem) {
        final MKSRelax relax = new MKSRelax();
        final MKSRanking ranking = new MKSRanking();
        final FixedWidth<MKSState> width = new FixedWidth<>(50);
        final VariableHeuristic<MKSState> varh = new DefaultVariableHeuristic<>();
        final Frontier<MKSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final StateDistance<MKSState> distance = new MKSDistance();
        final StateCoordinates<MKSState> coordinates = new MKSCoordinates();
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                ClusterStrat.GHP,
                distance,
                coordinates,
                64865);

        solver.maximize();
        assertEquals(problem.optimal, solver.bestValue().get());
    }*/

    @Disabled
    @ParameterizedTest
    @MethodSource("dataProvider1D")
    public void testDominanceMKS(MKSProblem problem) {
        final MKSRelax relax = new MKSRelax();
        final MKSRanking ranking = new MKSRanking();
        final FixedWidth<MKSState> width = new FixedWidth<>(50);
        final VariableHeuristic<MKSState> varh = new DefaultVariableHeuristic<>();
        final Frontier<MKSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final SimpleDominanceChecker<MKSState, Integer> dominance = new SimpleDominanceChecker<>(new MKSDominance(),
                problem.nbVars())
;
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                dominance);

        solver.maximize();
        assertEquals(problem.optimal, solver.bestValue().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider1D")
    public void testRelaxationsMKS(MKSProblem problem) {
        final MKSRelax relax = new MKSRelax();
        final MKSRanking ranking = new MKSRanking();
        final FixedWidth<MKSState> width = new FixedWidth<>(50);
        final VariableHeuristic<MKSState> varh = new DefaultVariableHeuristic<>();
        final Frontier<MKSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final SimpleDominanceChecker<MKSState, Integer> dominance = new SimpleDominanceChecker<>(new MKSDominance(),
                problem.nbVars());
        final StateDistance<MKSState> distance = new MKSDistance();
        final StateCoordinates<MKSState> coordinates = new MKSCoordinates();
        final ClusterStrat restrictionStrat = ClusterStrat.Cost;

        for (ClusterStrat relaxStrat : ClusterStrat.values()) {
            final Solver solver = sequentialSolver(
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier,
                    dominance,
                    relaxStrat,
                    restrictionStrat,
                    distance,
                    coordinates,
                    864646);

            solver.maximize();
            assertEquals(problem.optimal, solver.bestValue().get());
        }
    }

    @ParameterizedTest
    @MethodSource("smallGeneratedInstances")
    public void testSequentialMKSRandom(MKSProblem problem) {
        final double optimal = bruteForce(problem);

        final MKSRelax relax = new MKSRelax();
        final MKSRanking ranking = new MKSRanking();
        final FixedWidth<MKSState> width = new FixedWidth<>(50);
        final VariableHeuristic<MKSState> varh = new DefaultVariableHeuristic<>();
        final Frontier<MKSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        solver.maximize();
        System.out.println(String.format("optimal :%f, solution: %f", optimal, solver.bestValue().get()));
        assertEquals(optimal, solver.bestValue().get());
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
