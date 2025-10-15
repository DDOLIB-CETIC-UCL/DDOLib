package org.ddolib.examples.misp;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.BitSet;

public final class MispAcsMain {
    /**
     * ***** The Maximum Independent Set Problem (MISP) *****
     */
    public static void main(String[] args) throws IOException {
        final String file = Paths.get("data", "MISP", "tadpole_4_2.dot").toString();
        final MispProblem problem = new MispProblem(file);
        AcsModel<BitSet> model = new AcsModel<>() {
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
        };

        Solver<BitSet> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAcs(model);

        System.out.println(stats);
    }

}
