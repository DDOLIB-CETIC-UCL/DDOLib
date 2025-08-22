package org.ddolib.examples.ddo.knapsack;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;

import java.io.IOException;

import static org.ddolib.examples.ddo.knapsack.KSMain.readInstance;

public class KSLoader {

    public static SolverConfig<Integer, Integer> loadProblem(String fileName, double widthFactor) {
        KSProblem problem = null;
        try {
            problem = readInstance(fileName);
        } catch (IOException e) {
            System.err.println("Problem reading " + fileName);
            System.exit(-1);
        }

        final SolverConfig <Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>((int) Math.ceil(widthFactor*problem.nbVars()));
        config.varh = new DefaultVariableHeuristic<Integer>();
        config.fub = new KSFastUpperBound(problem);
        config.dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
        config.distance = new KSDistance();
        config.coordinates = new KSCoordinates();

        return config;
    }
}
