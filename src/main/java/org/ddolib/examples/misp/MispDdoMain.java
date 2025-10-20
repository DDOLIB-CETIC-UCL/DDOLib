package org.ddolib.examples.misp;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.BitSet;

public final class MispDdoMain {
    /**
     * ***** The Maximum Independent Set Problem (MISP) *****
     */
    public static void main(String[] args) throws IOException {
        final String file = Paths.get("data", "MISP", "tadpole_4_2.dot").toString();
        final MispProblem problem = new MispProblem(file);
        DdoModel<BitSet> model = new DdoModel<>() {
            @Override
            public Problem<BitSet> problem() {
                return problem;
            }

            @Override
            public MispRelax relaxation() {
                return new MispRelax(problem);
            }

            @Override
            public MispRanking ranking() {
                return new MispRanking();
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

        Solvers<BitSet> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeDdo(model);

        System.out.println(stats);
    }

}
