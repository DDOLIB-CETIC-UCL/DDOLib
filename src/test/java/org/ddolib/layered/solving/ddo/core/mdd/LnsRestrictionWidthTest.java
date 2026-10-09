package org.ddolib.layered.solving.ddo.core.mdd;

import org.ddolib.common.compilation.CompilationType;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.examples.layered.knapsack.KSDominance;
import org.ddolib.examples.layered.knapsack.KSFastLowerBound;
import org.ddolib.examples.layered.knapsack.KSProblem;
import org.ddolib.examples.layered.knapsack.KSRanking;
import org.ddolib.layered.modeling.*;
import org.ddolib.layered.solving.ddo.core.SubProblem;
import org.ddolib.layered.solving.ddo.core.compilation.CompilationConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that, in LNS mode, the restriction is applied before the children are generated so
 * that the width of the restricted decision diagram is actually bounded.
 */
public class LnsRestrictionWidthTest {

    private static final int MAX_WIDTH = 10;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void restrictedDiagramWidthIsBoundedInLnsMode(boolean withIncumbent) throws IOException {
        final String instance = Path.of("data", "Knapsack", "instance_n1000_c1000_10_5_10_5_0").toString();
        final KSProblem problem = new KSProblem(instance);
        final LnsModel<Integer> model = new LnsModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(MAX_WIDTH);
            }
        };

        Set<Integer> vars = IntStream.range(0, problem.nbVars()).boxed().collect(
                HashSet::new, HashSet::add, HashSet::addAll);
        SubProblem<Integer> root = new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                model.lowerBound().fastLowerBound(problem.initialState(), vars),
                Collections.emptySet());

        CompilationConfig<Integer> config = new CompilationConfig<>(model);
        config.compilationType = CompilationType.Restricted;
        config.problem = problem;
        config.variableHeuristic = model.variableHeuristic();
        config.stateRanking = model.ranking();
        config.residual = root;
        config.maxWidth = MAX_WIDTH;
        config.flb = model.lowerBound();
        config.dominance = model.dominance();
        config.bestUB = Double.POSITIVE_INFINITY;
        config.exportAsDot = false;
        config.debugLevel = model.debugMode();
        config.reductionStrategy = model.restrictStrategy();
        config.probability = model.probability();
        config.useLNS = true;
        // Taking no item is always feasible for the knapsack
        config.solution = withIncumbent ? new int[problem.nbVars()] : null;

        LinkedDecisionDiagram<Integer> mdd = new LinkedDecisionDiagram<>(config);
        mdd.compile();

        // Each layer keeps at most MAX_WIDTH nodes, each of which has at most 2 children (binary domain)
        int bound = 1 + problem.nbVars() * 2 * MAX_WIDTH;
        assertTrue(mdd.nbNodes() <= bound,
                "Restricted LNS diagram has " + mdd.nbNodes() + " nodes, expected at most " + bound);
        assertFalse(mdd.isExact());
        assertTrue(mdd.bestValue().isPresent());
    }
}
