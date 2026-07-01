package org.ddolib.examples.nolayer.gruler;

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

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

public class GRDdoMain {

    public static void main(String[] args) {
        int order = args.length == 0 ? 10 : Integer.parseInt(args[0]);
        final GRProblem problem = new GRProblem(order);

        DdoModel<GRState> model = new GRDdoModel(problem) {
            @Override
            public Relaxation<GRState> relaxation() {
                return new Relaxation<GRState>() {
                    @Override
                    public GRState merge(Collection<GRState> states) {
                        Iterator<GRState> it = states.iterator();
                        GRState first = it.next();
                        BitSet marks = (BitSet) first.getMarks().clone();
                        BitSet distances = (BitSet) first.getDistances().clone();
                        int lastMark = first.getLastMark();

                        while (it.hasNext()) {
                            GRState s = it.next();
                            marks.and(s.getMarks());
                            distances.and(s.getDistances());
                            lastMark = Math.min(lastMark, s.getLastMark());
                        }
                        return new GRState(marks, distances, lastMark, first.getLayer());
                    }
                };
            }

            @Override
            public StateRanking<GRState> ranking() {
                // Rank by highest number of marks first, then smallest last mark
                return (s1, s2) -> {
                    int c = Integer.compare(s2.getNumberOfMarks(), s1.getNumberOfMarks());
                    if (c != 0)
                        return c;
                    return Integer.compare(s1.getLastMark(), s2.getLastMark());
                };
            }

            @Override
            public WidthHeuristic<GRState> widthHeuristic() {
                return new FixedWidth<>(5);
            }

            @Override
            public ReductionStrategy<GRState> relaxStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public ReductionStrategy<GRState> restrictStrategy() {
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

        DdoSolver<GRState> solver = new DdoSolver<>(model);
        Solution bestSolution = solver.minimize(
                limit -> limit.nbIterations() > 1000,
                (sol, stats) -> SolutionPrinter.printSolution(stats, sol));

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal Golomb Ruler value for order " + order + ": " + bestSolution.value());
    }
}

abstract class GRDdoModel extends GRModel implements DdoModel<GRState> {
    public GRDdoModel(GRProblem problem) {
        super(problem);
    }
}
