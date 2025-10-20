package org.ddolib.examples.talentscheduling;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;
/**
 * ################  The talent scheduling problem (tsp) ####################
 */
public class TSDdoMain {
    public static void main(String[] args) throws IOException {
        String file = args.length == 0 ? Paths.get("data", "TalentScheduling", "film-12").toString() : args[0];
        final TSProblem problem = new TSProblem(file);
        DdoModel<TSState> model = new DdoModel<>() {
            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public Relaxation<TSState> relaxation() {
                return new TSRelax(problem);
            }

            @Override
            public TSRanking ranking() {
                return new TSRanking();
            }

            @Override
            public TSFastLowerBound lowerBound() {
                return new TSFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model);

        System.out.println(stats);
    }
}
