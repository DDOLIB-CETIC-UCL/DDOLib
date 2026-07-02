package org.ddolib.nolayer.examples.gruler;

import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.nolayer.modeling.DdoModel;
import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.nolayer.modeling.Relaxation;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.NoLayerTestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class GRTestDataSupplier extends NoLayerTestDataSupplier<GRState, GRProblem> {

    @Override
    protected List<GRProblem> generateProblems() {
        return IntStream.range(2, 7).mapToObj(GRProblem::new).toList();
    }

    @Override
    protected DdoModel<GRState> model(GRProblem problem) {
        return new GRDdoModel(problem) {
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
                return (s1, s2) -> {
                    int c = Integer.compare(s2.getNumberOfMarks(), s1.getNumberOfMarks());
                    if (c != 0) return c;
                    return Integer.compare(s1.getLastMark(), s2.getLastMark());
                };
            }

            @Override
            public WidthHeuristic<GRState> widthHeuristic() {
                return new FixedWidth<>(10);
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
                return VerbosityLevel.SILENT;
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }

            @Override
            public boolean useCache() {
                return false;
            }
        };
    }

    private abstract static class GRDdoModel extends GRModel implements DdoModel<GRState> {
        public GRDdoModel(GRProblem problem) {
            super(problem);
        }
    }
}
