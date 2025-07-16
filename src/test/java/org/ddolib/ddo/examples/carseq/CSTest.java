package org.ddolib.ddo.examples.carseq;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.Solvers;
import org.junit.jupiter.api.Test;

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

    public static void testSolve(String inputFile) throws IOException {
        // Solve problem
        CSProblem problem = CSMain.readInstance(inputFile);
        CSRelax relax = new CSRelax(problem);
        CSRanking ranking = new CSRanking();
        FixedWidth<CSState> width = new FixedWidth<>(250);
        VariableHeuristic<CSState> varh = new DefaultVariableHeuristic<>();
        Frontier<CSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        Solver solver = Solvers.sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
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