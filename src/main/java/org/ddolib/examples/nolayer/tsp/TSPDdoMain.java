package org.ddolib.examples.nolayer.tsp;

import org.ddolib.common.solver.layered.Solution;
import org.ddolib.modeling.layered.StateRanking;
import org.ddolib.solving.ddo.core.heuristics.cluster.nolayer.CostBased;
import org.ddolib.solving.ddo.core.heuristics.cluster.nolayer.ReductionStrategy;
import org.ddolib.solving.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.solving.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.nolayer.DdoModel;
import org.ddolib.modeling.nolayer.Relaxation;
import org.ddolib.solving.ddo.core.solver.nolayer.DdoSolver;
import org.ddolib.util.io.SolutionPrinter;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

public class TSPDdoMain {

    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TSP", "instance_18_0.xml").toString() : args[0];
        final TSPProblem problem = new TSPProblem(instance);

        DdoModel<TSPState> model = new TSPDdoModel(problem) {
            @Override
            public Relaxation<TSPState> relaxation() {
                return new Relaxation<TSPState>() {
                    @Override
                    public TSPState merge(Collection<TSPState> states) {
                        Iterator<TSPState> it = states.iterator();
                        TSPState first = it.next();
                        BitSet toVisit = (BitSet) first.toVisit.clone();
                        BitSet current = (BitSet) first.current.clone();

                        while (it.hasNext()) {
                            TSPState s = it.next();
                            toVisit.and(s.toVisit);
                            current.or(s.current);
                        }
                        return new TSPState(current, toVisit);
                    }
                };
            }

            @Override
            public StateRanking<TSPState> ranking() {
                // Rank by fewest remaining toVisit nodes first
                return (s1, s2) -> Integer.compare(s1.toVisit.cardinality(), s2.toVisit.cardinality());
            }

            @Override
            public WidthHeuristic<TSPState> widthHeuristic() {
                return new FixedWidth<>(500);
            }

            @Override
            public ReductionStrategy<TSPState> relaxStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public ReductionStrategy<TSPState> restrictStrategy() {
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

            @Override
            public org.ddolib.common.dominance.NoLayerDominanceChecker<TSPState> dominance() {
                return new TSPNoLayerDominanceChecker();
            }
        };

        DdoSolver<TSPState> solver = new DdoSolver<>(model);
        Solution bestSolution = solver.minimize(
                limit -> limit.nbIterations() > 1000,
                (sol, stats) -> SolutionPrinter.printSolution(stats, sol)
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal TSP value: " + bestSolution.value());
    }
}

abstract class TSPDdoModel extends TSPModel implements DdoModel<TSPState> {
    public TSPDdoModel(TSPProblem problem) {
        super(problem);
    }
}
