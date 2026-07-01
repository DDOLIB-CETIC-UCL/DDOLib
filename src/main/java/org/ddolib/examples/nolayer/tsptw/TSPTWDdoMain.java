package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.common.solver.layered.Solution;
import org.ddolib.modeling.layered.StateRanking;
import org.ddolib.solving.ddo.core.heuristics.cluster.nolayer.CostBased;
import org.ddolib.solving.ddo.core.heuristics.cluster.nolayer.ReductionStrategy;
import org.ddolib.solving.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.solving.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.nolayer.DdoModel;
import org.ddolib.modeling.nolayer.Relaxation;
import org.ddolib.solving.ddo.core.solver.nolayer.DdoSolver;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

public class TSPTWDdoMain {

    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Path.of("data", "TSPTW", "AFG", "rbg010a.tw").toString() : args[0];
        final TSPTWProblem problem = TSPTWProblem.fromFile(instance);

        DdoModel<TSPTWState> model = new TSPTWDdoModel(problem) {
            @Override
            public Relaxation<TSPTWState> relaxation() {
                return new Relaxation<TSPTWState>() {
                    @Override
                    public TSPTWState merge(Collection<TSPTWState> states) {
                        Iterator<TSPTWState> it = states.iterator();
                        TSPTWState first = it.next();
                        int currentCity = first.currentCity();
                        int minTime = first.time();
                        BitSet mustVisit = (BitSet) first.mustVisit().clone();

                        while (it.hasNext()) {
                            TSPTWState s = it.next();
                            minTime = Math.min(minTime, s.time());
                            mustVisit.and(s.mustVisit()); // Intersection for relaxed states
                        }
                        return new TSPTWState(currentCity, minTime, mustVisit);
                    }
                };
            }

            @Override
            public StateRanking<TSPTWState> ranking() {
                return (s1, s2) -> Integer.compare(s1.mustVisit().cardinality(), s2.mustVisit().cardinality());
            }

            @Override
            public WidthHeuristic<TSPTWState> widthHeuristic() {
                return new FixedWidth<>(10);
            }

            @Override
            public ReductionStrategy<TSPTWState> relaxStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public ReductionStrategy<TSPTWState> restrictStrategy() {
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

        DdoSolver<TSPTWState> solver = new DdoSolver<>(model);
        Solution bestSolution = solver.minimize(
                limit -> limit.nbIterations() > 1000,
                (sol, stats) -> org.ddolib.util.io.SolutionPrinter.printSolution(stats, sol));

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}

// Ensure TSPTWModel implements DdoModel
abstract class TSPTWDdoModel extends TSPTWModel implements DdoModel<TSPTWState> {
    public TSPTWDdoModel(TSPTWProblem problem) {
        super(problem);
    }
}
