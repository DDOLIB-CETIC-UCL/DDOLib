package org.ddolib.examples.talentscheduling;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;
/**
 * ################  The talent scheduling problem (tsp) ####################
 */
public class TSAstarMain {
    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TalentScheduling", "film-12").toString() : args[0];
        final TSProblem problem = new TSProblem(instance);
        Model<TSState> model = new Model<>() {
            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public TSFastLowerBound lowerBound() {
                return new TSFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAstar(model);

        System.out.println(stats);
    }
}
