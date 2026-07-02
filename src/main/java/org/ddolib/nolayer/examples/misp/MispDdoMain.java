package org.ddolib.nolayer.examples.misp;

import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.nolayer.common.solver.Solution;
import org.ddolib.nolayer.modeling.DdoModel;
import org.ddolib.nolayer.modeling.Relaxation;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.nolayer.solving.ddo.core.solver.DdoSolver;
import org.ddolib.util.io.SolutionPrinter;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

public class MispDdoMain {

    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Path.of("data", "MISP", "tadpole_4_2.dot").toString() : args[0];
        final MispProblem problem = MispProblem.fromFile(instance);

        DdoModel<MispState> model = new MispDdoModel(problem) {
            @Override
            public Relaxation<MispState> relaxation() {
                return new Relaxation<MispState>() {
                    @Override
                    public MispState merge(Collection<MispState> states) {
                        Iterator<MispState> it = states.iterator();
                        MispState first = it.next();
                        BitSet remaining = (BitSet) first.remainingNodes().clone();

                        while (it.hasNext()) {
                            MispState s = it.next();
                            remaining.or(s.remainingNodes()); // Union of remaining nodes
                        }
                        return new MispState(remaining);
                    }
                };
            }

            @Override
            public StateRanking<MispState> ranking() {
                // Rank by fewest remaining nodes first
                return (s1, s2) -> Integer.compare(s1.remainingNodes().cardinality(), s2.remainingNodes().cardinality());
            }

            @Override
            public WidthHeuristic<MispState> widthHeuristic() {
                return new FixedWidth<>(10);
            }

            @Override
            public ReductionStrategy<MispState> relaxStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public ReductionStrategy<MispState> restrictStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.NORMAL;
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };

        DdoSolver<MispState> solver = new DdoSolver<>(model);
        Solution bestSolution = solver.minimize(
                limit -> limit.nbIterations() > 1000,
                (sol, stats) -> SolutionPrinter.printSolution(stats, sol)
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal MISP value: " + -bestSolution.value());
    }
}

abstract class MispDdoModel extends MispModel implements DdoModel<MispState> {
    public MispDdoModel(MispProblem problem) {
        super(problem);
    }
}
