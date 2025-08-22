package org.ddolib.examples.astart.mips;

import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.examples.ddo.misp.MispFastUpperBound;
import org.ddolib.examples.ddo.misp.MispMain;
import org.ddolib.examples.ddo.misp.MispProblem;

import javax.lang.model.type.NullType;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;

public class AstarMisp {

    public static void main(String[] args) throws IOException {
        final MispProblem problem = MispMain.readFile("data/MISP/weighted.dot");
        SolverConfig<BitSet, NullType> config = new SolverConfig<>();
        config.problem = problem;
        config.varh = new DefaultVariableHeuristic<>();
        config.fub = new MispFastUpperBound(problem);

        BitSet root = new BitSet();
        root.set(2);
        root.set(3);
        root.set(4);
        int depth = 2;
        System.out.printf("root: %s - depth: %d\n", root, depth);

        final Solver solver = new AStarSolver<>(config);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] values = new int[problem.nbVars()];
                    for (Decision d : decisions) {
                        values[d.var()] = d.val();
                    }
                    return values;
                })
                .get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));

    }
}
