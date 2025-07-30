package org.ddolib.examples.ddo.carseq;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.OrderedVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.factory.Solvers;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CSTest {
    @Test
    public void testSolveExample() throws IOException {
        testSolve("data/CarSeq/example.txt");
    }

    @Test
    public void testSolveSmall() throws IOException {
        testSolve("data/CarSeq/small.txt");
    }

    @Test
    public void testSolveMedium() throws IOException {
        testSolve("data/CarSeq/medium.txt");
    }

    @Test
    public void testSolveInstances() throws IOException {
        File dir = new File("data/CarSeq");
        for (File file : dir.listFiles()) {
            String path = file.getPath();
            if (path.startsWith("data/CarSeq/instance")) testSolve(path);
        }
    }

    public static void testSolve(String inputFile) throws IOException {
        // Solve problem
        CSProblem problem = CSInstance.read(inputFile);
        for (int i = 0; i < problem.nOptions(); i++) { // Prevent sizes larger than 64 (to be able to use a single long instead of a BitSet)
            if (problem.blockSize[i] > 64) throw new IllegalArgumentException("Option block size must be less than 64");
        }

        CSRelax relax = new CSRelax(problem);
        CSFastUpperBound fub = new CSFastUpperBound(problem);
        CSRanking ranking = new CSRanking();
        FixedWidth<CSState> width = new FixedWidth<>(500);
        VariableHeuristic<CSState> varh = new OrderedVariableHeuristic<>(problem);
        Frontier<CSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        SimpleDominanceChecker<CSState, Integer> dominance = new SimpleDominanceChecker<>(new CSDominance(problem), problem.nbVars());
        Solver solver = Solvers.sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                fub
        );
        solver.maximize();

        // Check solution
        assertTrue(solver.bestSolution().isPresent() && solver.bestValue().isPresent(), "No solution found");
        assertEquals(0, solver.bestValue().get(), "Solution violates constraints");
        Set<Decision> decisions = solver.bestSolution().get();
        int[] cars = new int[problem.nbVars()];
        for (Decision d : decisions) {
            cars[d.var()] = d.val();
        }
        for (int i = 0; i < problem.nOptions(); i++) { // Check option i
            for (int j = 0; j < problem.nCars; j++) {
                int n = 0;
                for (int k = 0; k < problem.blockSize[i] && j + k < problem.nCars; k++) {
                    if (problem.carOptions[cars[j + k]][i]) n++;
                }
                assertTrue(n <= problem.blockMax[i]);
            }
        }
    }
}