package org.ddolib.examples.alp;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Solve;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public final class ALPDdoMain {

    public static void main(final String[] args) throws IOException {
        final String fileStr = Paths.get("data", "alp", "alp_n50_r1_c2_std10_s0").toString();
        DdoModel<ALPState> model = new DdoModel<>() {
            private ALPProblem problem;
            @Override
            public ALPProblem problem() {
                try {
                    ALPInstance instance = new ALPInstance(fileStr);
                    problem = new ALPProblem(instance);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public ALPRelax relaxation() {
                return new ALPRelax(problem);
            }
            @Override
            public ALPRanking ranking() {
                return new ALPRanking();
            }
            @Override
            public ALPFastLowerBound lowerBound() {
                return new ALPFastLowerBound(problem);
            }
            @Override
            public WidthHeuristic widthHeuristic() {
                return new FixedWidth<>(100);
            }
        };

        Solve<ALPState> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeDdo(model);

        solve.onSolution(stats);



    }
}