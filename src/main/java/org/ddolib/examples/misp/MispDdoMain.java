package org.ddolib.examples.misp;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.BitSet;

public final class MispDdoMain {
    /**
     * ***** The Maximum Independent Set Problem (MISP) *****
     * given a weighted graph 𝐺 = (𝑉,𝐸,𝑤) where 𝑉= {1,...,𝑛}
     * is a set of vertices, 𝐸 \subset 𝑉 ×𝑉 the set of edges connecting those vertices and
     * 𝑤 = {𝑤1,𝑤2,...,𝑤𝑛} is a set of weights s.t. 𝑤𝑖 is the weight of node 𝑖.
     * The problem consists in finding a subset of vertices in a graph such that
     * no edge exists in the graph that connects two of the selected nodes and
     * the sum of the weight of the selected nodes is maximal.
     * This problem is considered in the paper:
     * - David Bergman et al. Decision Diagrams for Optimization. Ed. by Barry O’Sullivan and Michael Wooldridge. Springer, 2016.
     * - David Bergman et al. “Discrete Optimization with Decision Diagrams”. In: INFORMS Journal on Computing 28.1 (2016), pp. 47–66.
     * /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.misp.MispMain"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.misp.MispMain -Dexec.args="<your file>
     * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.
     */
    public static void main(String[] args) throws IOException {
        final String file = Paths.get("data", "MISP", "tadpole_4_2.dot").toString();

        DdoModel<BitSet> model = new DdoModel<>() {
            private MispProblem problem;

            @Override
            public Problem<BitSet> problem() {
                try {
                    problem = new MispProblem(file);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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

        Solver<BitSet> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeDdo(model);

        System.out.println(stats);
    }

}
