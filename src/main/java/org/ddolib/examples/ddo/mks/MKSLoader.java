package org.ddolib.examples.ddo.mks;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.examples.ddo.ProblemLoader;
import org.ddolib.examples.ddo.knapsack.*;
import org.ddolib.modeling.DefaultFastUpperBound;
import org.ddolib.modeling.FastUpperBound;

import java.io.IOException;

import static org.ddolib.examples.ddo.mks.MKSMain.readInstance;

public class MKSLoader {

    public static ProblemLoader<MKSState, Integer> loadProblem(String fileName, double widthFactor) {
        MKSProblem problem = null;
        try {
            problem = readInstance(fileName);
        } catch (IOException e) {
            System.err.println("Problem reading " + fileName);
            System.exit(-1);
        }

        final MKSRelax relax = new MKSRelax();
        final MKSRanking ranking = new MKSRanking();
        final FixedWidth<MKSState> width = new FixedWidth<>((int) widthFactor*problem.nbVars());
        final VariableHeuristic<MKSState> varh = new DefaultVariableHeuristic<>();
        final FastUpperBound<MKSState> fub = new DefaultFastUpperBound<>();
        final DefaultDominanceChecker<MKSState> dominance = new DefaultDominanceChecker<>();
        final StateDistance<MKSState> distance = new MKSDistance();
        final StateCoordinates<MKSState> coordinates = new MKSCoordinates();

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
