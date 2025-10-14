package org.ddolib.examples.setcover.setlayer;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.modeling.DefaultFastUpperBound;

import java.io.IOException;

public class SetCoverLoaderAlt {

    public static SolverConfig<SetCoverState, Integer> loadProblem(final String fileName, final double widthFactor, final boolean weighted) {
        SetCoverProblem problem = null;
        try {
            problem = SetCover.readInstance(fileName, weighted);
        } catch (IOException e) {
            System.err.println("Problem reading " + fileName);
            System.exit(-1);
        }

        final SolverConfig <SetCoverState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SetCoverRelax();
        config.ranking = new SetCoverRanking();
        config.width = new FixedWidth<>((int) Math.ceil(widthFactor*problem.nbVars()));
        config.varh = new SetCoverHeuristics.FocusClosingElements(problem);
        config.fub = new DefaultFastUpperBound<>();
        config.dominance = new DefaultDominanceChecker<>();
        config.distance = new SetCoverDistanceWeighted(problem);
        config.coordinates = new SetCoverCoordinates(problem);

        return config;
    }
}
