package org.ddolib.examples.misp;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DebugLevel;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;

public final class MispAstarMain {
    /**
     * ***** The Maximum Independent Set Problem (MISP) *****
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MISP", "tadpole_4_2.dot").toString() : args[0];
        final MispProblem problem = new MispProblem(instance);
        Model<BitSet> model = new Model<>() {
            @Override
            public Problem<BitSet> problem() {
                return problem;
            }

            @Override
            public DominanceChecker<BitSet> dominance() {
                return new SimpleDominanceChecker<>(new MispDominance(), problem.nbVars());
            }

            @Override
            public MispFastLowerBound lowerBound() {
                return new MispFastLowerBound(problem);
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };

        SearchStatistics stats = Solvers.minimizeAstar(model);

        System.out.println(stats);
    }

}
