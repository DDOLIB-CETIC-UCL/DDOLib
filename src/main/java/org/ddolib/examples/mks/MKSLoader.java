package org.ddolib.examples.mks;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.modeling.DefaultFastLowerBound;

import java.io.IOException;

import static org.ddolib.examples.mks.MKSMain.readInstance;

public class MKSLoader {

    public static SolverConfig<MKSState, Integer> loadProblem(String fileName, double widthFactor) {
        MKSProblem problem = null;
        try {
            problem = readInstance(fileName);
        } catch (IOException e) {
            System.err.println("Problem reading " + fileName);
            System.exit(-1);
        }
        final SolverConfig<MKSState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new MKSRelax();
        config.ranking = new MKSRanking();
        config.width = new FixedWidth<>((int) Math.ceil(widthFactor*problem.nbVars()));
        config.varh = new DefaultVariableHeuristic<>();
        config.flb = new DefaultFastLowerBound<>();
        config.dominance = new DefaultDominanceChecker<>();
        config.distance = new MKSDistance();
        config.coordinates = new MKSCoordinates();

        return config;
    }
}
