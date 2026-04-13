package org.ddolib.examples.pdptw;

import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.pdp.PDPProblem;
import org.ddolib.examples.pdp.PDPState;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.util.testbench.TestDataSupplier;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class PDPTWTestDataSupplier extends TestDataSupplier<PDPTWState, PDPTWProblem> {

    @Override
    protected List<PDPTWProblem> generateProblems() {
        Random random = new Random(1);
        int nbTests = 10;

        return IntStream.range(0, nbTests).boxed().map(
                i -> PDPTWGenerator.genInstance(10, 2, 3, random)
        ).toList();
    }

    @Override
    protected DdoModel<PDPTWState> model(PDPTWProblem problem) {
        return new DdoModel<>() {
            @Override
            public Problem<PDPTWState> problem() {
                return problem;
            }

            @Override
            public PDPTWFastLowerBound lowerBound() {
                return new PDPTWFastLowerBound(problem);
            }

            @Override
            public PDPTWRelax relaxation() {
                return new PDPTWRelax(problem);
            }

            @Override
            public PDPTWRanking ranking() {
                return new PDPTWRanking();
            }

            @Override
            public WidthHeuristic<PDPTWState> widthHeuristic() {
                return new FixedWidth<>(1000);
            }

            @Override
            public Frontier<PDPTWState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };
    }
}
