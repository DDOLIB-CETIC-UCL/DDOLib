package org.ddolib.examples.ddo.misp;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.implem.heuristics.DefaultStateCoordinates;
import org.ddolib.modeling.*;

import java.io.IOException;
import java.util.BitSet;

import static org.ddolib.examples.ddo.misp.MispMain.readFile;

public class MispLoader {

    public static SolverConfig<BitSet, Integer> loadProblem(String fileName, double widthFactor) {
        MispProblem problem = null;
        try {
            problem = readFile(fileName);
        } catch (IOException e) {
            System.err.println("Problem reading " + fileName);
            System.exit(-1);
        }
        final SolverConfig<BitSet, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new MispRelax(problem);
        config.ranking = new MispRanking();
        config.width = new FixedWidth<>((int) Math.ceil(widthFactor*problem.nbVars()));
        config.varh = new DefaultVariableHeuristic<>();
        config.fub = new MispFastUpperBound(problem);
        config.dominance = new DefaultDominanceChecker<>();
        config.distance = new MispDistance();
        config.coordinates = new DefaultStateCoordinates<>();

        return config;
    }
}
