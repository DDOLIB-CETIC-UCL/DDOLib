package org.ddolib.nolayer.examples.tsptw;

import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.nolayer.modeling.DdoModel;
import org.ddolib.nolayer.modeling.Relaxation;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.NoLayerTestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class TSPTWTestDataSupplier extends NoLayerTestDataSupplier<TSPTWState, TSPTWProblem> {

    private final Path dir;

    public TSPTWTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<TSPTWProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return TSPTWProblem.fromFile(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected DdoModel<TSPTWState> model(TSPTWProblem problem) {
        return new TSPTWDdoModel(problem) {
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

    private abstract static class TSPTWDdoModel extends TSPTWModel implements DdoModel<TSPTWState> {
        public TSPTWDdoModel(TSPTWProblem problem) {
            super(problem);
        }
    }
}
