package org.ddolib.examples.tsalt;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.examples.knapsack.*;

import static org.ddolib.examples.tsalt.TSMain.readFile;

import java.io.IOException;

public class TSLoader {

    public static SolverConfig<TSState, Integer> loadProblem(String fileName, double widthFactor) {
        TSProblem problem = null;
        try {
            problem = readFile(fileName);
        } catch (IOException e) {
            System.err.println("Problem reading " + fileName);
            System.exit(-1);
        }

        final SolverConfig <TSState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new TSRelax(problem);
        config.ranking = new TSRanking();
        config.width = new FixedWidth<>((int) Math.ceil(widthFactor*problem.nbVars()));
        config.varh = new DefaultVariableHeuristic<TSState>();
        config.flb = new TSFastLowerBound(problem);
        config.distance = new TSDistance(problem);
        // config.coordinates = new TSCoordinates();

        return config;
    }

}
