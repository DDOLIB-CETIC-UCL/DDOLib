package org.ddolib.examples.nolayer.knapsack;

import org.ddolib.common.solver.Solution;
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
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;

public class KSDdoMain {

    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "KP", "f10_l-d_kp_20_878").toString() : args[0];
        final KSProblem problem = KSProblem.fromFile(instance);

        DdoModel<KSState> model = new KSDdoModel(problem) {
            @Override
            public Relaxation<KSState> relaxation() {
                return new Relaxation<KSState>() {
                    @Override
                    public KSState merge(Collection<KSState> states) {
                        Iterator<KSState> it = states.iterator();
                        KSState first = it.next();
                        int maxCapacity = first.remainingCapacity();
                        int currentItem = first.currentItem();

                        while (it.hasNext()) {
                            KSState s = it.next();
                            maxCapacity = Math.max(maxCapacity, s.remainingCapacity()); // Relax by taking max capacity
                            currentItem = Math.max(currentItem, s.currentItem()); // Arbitrary/safe
                        }
                        return new KSState(currentItem, maxCapacity);
                    }
                };
            }

            @Override
            public StateRanking<KSState> ranking() {
                // Rank by largest remaining capacity first
                return (s1, s2) -> Integer.compare(s2.remainingCapacity(), s1.remainingCapacity());
            }

            @Override
            public WidthHeuristic<KSState> widthHeuristic() {
                return new FixedWidth<>(2);
            }

            @Override
            public ReductionStrategy<KSState> relaxStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public ReductionStrategy<KSState> restrictStrategy() {
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

        DdoSolver<KSState> solver = new DdoSolver<>(model);
        Solution bestSolution = solver.minimize(
                limit -> limit.nbIterations() > 1000,
                (sol, stats) -> SolutionPrinter.printSolution(stats, sol)
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal KS value: " + -bestSolution.value());
    }
}

abstract class KSDdoModel extends KSModel implements DdoModel<KSState> {
    public KSDdoModel(KSProblem problem) {
        super(problem);
    }
}
