package org.ddolib.examples.talentscheduling;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;
/**
 * ################  The talent scheduling problem (tsp) ####################
 */
public class TSAcsMain {
    public static void main(String[] args) throws IOException {
        String file = args.length == 0 ? Paths.get("data", "TalentScheduling", "film-12").toString() : args[0];
        final TSProblem problem = new TSProblem(file);
        AcsModel<TSState> model = new AcsModel<>() {
            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public TSFastLowerBound lowerBound() {
                return new TSFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model);

        System.out.println(stats);
    }
}
