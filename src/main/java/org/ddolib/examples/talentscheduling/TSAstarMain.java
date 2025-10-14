package org.ddolib.examples.talentscheduling;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;

public class TSAstarMain {
    public static void main(String[] args) throws IOException {
        String file = args.length == 0 ? Paths.get("data", "TalentScheduling", "film-12").toString() : args[0];
        Model<TSState> model = new Model<>() {
            private TSProblem problem;

            @Override
            public Problem<TSState> problem() {
                try {
                    problem = new TSProblem(file);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public TSFastLowerBound lowerBound() {
                return new TSFastLowerBound(problem);
            }
        };

        Solvers<TSState> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeAstar(model);

        System.out.println(stats);
    }
}
