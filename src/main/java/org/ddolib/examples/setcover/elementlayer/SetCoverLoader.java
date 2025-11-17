package org.ddolib.examples.setcover.elementlayer;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.examples.LaunchInterface;
import org.ddolib.modeling.DefaultFastLowerBound;

import java.io.IOException;

import static org.ddolib.examples.setcover.elementlayer.SetCover.readInstance;
import static org.ddolib.examples.LaunchInterface.DistanceType;

public class SetCoverLoader {

    public static SolverConfig<SetCoverState, Integer> loadProblem(final String fileName,
                                                                   final double widthFactor,
                                                                   final boolean weighted,
                                                                   final DistanceType distanceType) {
        SetCoverProblem problem = null;
        try {
            problem = readInstance(fileName, weighted);
        } catch (IOException e) {
            System.err.println("Problem reading " + fileName);
            System.exit(-1);
        }

        final SolverConfig <SetCoverState, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SetCoverRelax();
        config.ranking = new SetCoverRanking();
        config.width = new FixedWidth<>((int) Math.ceil(widthFactor*problem.nbVars()));
        config.varh = new SetCoverHeuristics.MinWeight(problem);
        config.flb = new DefaultFastLowerBound<>();
        config.dominance = new DefaultDominanceChecker<>();
        config.distance = new SetCoverDistance(problem, distanceType);
        config.coordinates = new SetCoverCoordinates(problem);

        return config;
    }
}
