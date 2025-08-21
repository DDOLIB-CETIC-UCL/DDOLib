package org.ddolib.examples.ddo.setcover.elementlayer;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.implem.heuristics.DefaultStateCoordinates;
import org.ddolib.examples.ddo.ProblemLoader;
import org.ddolib.examples.ddo.knapsack.*;
import org.ddolib.modeling.DefaultFastUpperBound;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;

import java.io.IOException;

import static org.ddolib.examples.ddo.setcover.elementlayer.SetCover.readInstance;

public class SetCoverLoader {

    public static ProblemLoader<SetCoverState, Integer> loadProblem(String fileName, double widthFactor) {
        SetCoverProblem problem = null;
        try {
            problem = readInstance(fileName);
        } catch (IOException e) {
            System.err.println("Problem reading " + fileName);
            System.exit(-1);
        }

        final Relaxation<SetCoverState> relax = new SetCoverRelax();
        final StateRanking<SetCoverState> ranking = new SetCoverRanking();
        final FixedWidth<SetCoverState> width = new FixedWidth<>((int) widthFactor*problem.nbVars());
        final VariableHeuristic<SetCoverState> varh = new DefaultVariableHeuristic<>();
        final FastUpperBound<SetCoverState> fub = new DefaultFastUpperBound<>();
        final DominanceChecker<SetCoverState, Integer> dominance = new DefaultDominanceChecker<>();
        final StateDistance<SetCoverState> distance = new SetCoverDistance();
        final StateCoordinates<SetCoverState> coordinates = new DefaultStateCoordinates<>();

        return new ProblemLoader<>(problem,
                relax,
                ranking,
                width,
                varh,
                fub,
                dominance,
                distance,
                coordinates);
    }
}
