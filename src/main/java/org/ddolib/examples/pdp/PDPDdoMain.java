package org.ddolib.examples.pdp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.IOException;
import java.util.Random;

import static org.ddolib.examples.pdp.PDPGenerator.genInstance;
/**
 * ############# Single Vehicle Pick-up and Delivery Problem (PDP)
 */
public final class PDPDdoMain {


    public static void main(final String[] args) throws IOException {

        final PDPProblem problem = genInstance(18, 2, 3, new Random(1));
        DdoModel<PDPState> model = new DdoModel<>() {
            @Override
            public Problem<PDPState> problem() {
                return problem;
            }

            @Override
            public PDPRelax relaxation() {
                return new PDPRelax(problem);
            }

            @Override
            public PDPRanking ranking() {
                return new PDPRanking();
            }

            @Override
            public PDPFastLowerBound lowerBound() {
                return new PDPFastLowerBound(problem);
            }

            @Override
            public Frontier<PDPState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public WidthHeuristic<PDPState> widthHeuristic() {
                return new FixedWidth<>(1000);
            }
        };

        Solver<PDPState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeDdo(model);

        System.out.println(stats);
    }
}
