package org.ddolib.examples.ddo.knapsack;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.examples.ddo.ProblemLoader;

import java.io.IOException;

import static org.ddolib.examples.ddo.knapsack.KSMain.readInstance;

public class KSLoader {

    public static ProblemLoader<Integer, Integer> loadProblem(String fileName, double widthFactor) {
        KSProblem problem = null;
        try {
            problem = readInstance(fileName);
        } catch (IOException e) {
            System.err.println("Problem reading " + fileName);
            System.exit(-1);
        }

        final KSRelax relax = new KSRelax();
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>((int) widthFactor*problem.nbVars());
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final KSFastUpperBound fub = new KSFastUpperBound(problem);
        final SimpleDominanceChecker<Integer, Integer> dominance = new SimpleDominanceChecker<>(new KSDominance(),
                problem.nbVars());
        final StateDistance<Integer> distance = new KSDistance();
        final StateCoordinates<Integer> coordinates = new KSCoordinates();

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
