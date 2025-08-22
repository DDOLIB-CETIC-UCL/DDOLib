package org.ddolib.examples.ddo.misp;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.implem.heuristics.DefaultStateCoordinates;
import org.ddolib.examples.ddo.ProblemLoader;
import org.ddolib.examples.ddo.setcover.elementlayer.*;
import org.ddolib.modeling.*;

import java.io.IOException;
import java.util.BitSet;

import static org.ddolib.examples.ddo.misp.MispMain.readFile;

public class MispLoader {

    public static ProblemLoader<BitSet, Integer> loadProblem(String fileName, double widthFactor) {
        MispProblem problem = null;
        try {
            problem = readFile(fileName);
        } catch (IOException e) {
            System.err.println("Problem reading " + fileName);
            System.exit(-1);
        }

        final Relaxation<BitSet> relax = new MispRelax(problem);
        final StateRanking<BitSet> ranking = new MispRanking();
        final FixedWidth<BitSet> width = new FixedWidth<>((int) Math.ceil(widthFactor*problem.nbVars()));
        final VariableHeuristic<BitSet> varh = new DefaultVariableHeuristic<>();
        final FastUpperBound<BitSet> fub = new MispFastUpperBound(problem);
        final DominanceChecker<BitSet, Integer> dominance = new DefaultDominanceChecker<>();
        final StateDistance<BitSet> distance = new MispDistance();
        final StateCoordinates<BitSet> coordinates = new DefaultStateCoordinates<>();

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
