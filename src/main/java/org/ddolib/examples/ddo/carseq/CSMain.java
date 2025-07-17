package org.ddolib.examples.ddo.carseq;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.factory.Solvers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;


/**
 * <a href="https://www.csplib.org/Problems/prob001/">Car Sequencing</a> problem
 */
public class CSMain {
    public static void main(String[] args) throws IOException {
        CSProblem problem = readInstance("data/CarSeq/medium2.txt");
        for (int i = 0; i < problem.nOptions(); i++) { // Prevent sizes larger than 64 (to be able to use a single long instead of a BitSet)
            if (problem.blockSize[i] > 64) throw new IllegalArgumentException("Option block size must be less than 64");
        }

        CSRelax relax = new CSRelax(problem);
        CSFastUpperBound fub = new CSFastUpperBound();
        CSRanking ranking = new CSRanking();
        FixedWidth<CSState> width = new FixedWidth<>(500);
        VariableHeuristic<CSState> varh = new DefaultVariableHeuristic<>();
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

        SearchStatistics stats = solver.maximize(0, false);
        System.out.println(stats);
        Optional<Set<Decision>> solution = solver.bestSolution();
        if (solution.isPresent()) {
            Set<Decision> decisions = solution.get();
            int[] cars = new int[problem.nbVars()];
            for (Decision d : decisions) {
                cars[d.var()] = d.val();
            }
            System.out.println(problem.solutionToString(cars, (int)solver.bestValue().get().doubleValue()));
        }
        else System.out.println("No solution");
    }


    public static CSProblem readInstance(final String filePath) throws IOException {
        File file = new File(filePath);
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Parse number of cars, options and classes
            String line = reader.readLine();
            String[] elements = line.split(" ");
            if (elements.length != 3) throw new IllegalArgumentException("Invalid input");
            int[] blockSize = new int[Integer.parseInt(elements[1])];
            int[] blockMax = new int[blockSize.length];
            int[] classSize = new int[Integer.parseInt(elements[2])];
            boolean[][] carOptions = new boolean[classSize.length][];

            // Parse blockSize and blockMax
            line = reader.readLine();
            elements = line.split(" ");
            if (elements.length != blockMax.length) throw new IllegalArgumentException("Invalid input");
            for (int i = 0; i < blockMax.length; i++) {
                blockMax[i] = Integer.parseInt(elements[i]);
            }
            line = reader.readLine();
            elements = line.split(" ");
            if (elements.length != blockSize.length) throw new IllegalArgumentException("Invalid input");
            for (int i = 0; i < blockSize.length; i++) {
                blockSize[i] = Integer.parseInt(elements[i]);
            }

            // Parse carClass and carOptions
            for (int i = 0; i < classSize.length; i++) {
                line = reader.readLine();
                elements = line.split(" ");
                if (elements.length != 2 + blockSize.length) throw new IllegalArgumentException("Invalid input");
                classSize[i] = Integer.parseInt(elements[1]);
                carOptions[i] = new boolean[blockSize.length];
                for (int j = 0; j < blockSize.length; j++) {
                    carOptions[i][j] = Integer.parseInt(elements[2 + j]) == 1;
                }
            }

            return new CSProblem(classSize, blockSize, blockMax, carOptions);
        }
    }
}
