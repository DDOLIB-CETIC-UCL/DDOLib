package org.ddolib.ddo.examples.mks;



import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.RelaxationStrat;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.knapsack.KSProblem;
import org.ddolib.ddo.examples.knapsack.KSRanking;
import org.ddolib.ddo.examples.knapsack.KSRelax;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.Dominance;
import org.ddolib.ddo.implem.dominance.DominanceChecker;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ddolib.ddo.examples.mks.MKSMain.readInstance;
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

    @ParameterizedTest
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
                RelaxationStrat.GHP,
                distance,
                coordinates,
                64865);

        solver.maximize();
        assertEquals(problem.optimal, solver.bestValue().get());
    }

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

        for (RelaxationStrat relaxStrat : RelaxationStrat.values()) {
            final Solver solver = sequentialSolver(
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier,
                    dominance,
                    relaxStrat,
                    distance,
                    coordinates,
                    864646);

            solver.maximize();
            assertEquals(problem.optimal, solver.bestValue().get());
        }
    }
}
