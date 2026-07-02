package org.ddolib.nolayer.examples.tsp;

import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.nolayer.modeling.DdoModel;
import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;
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

public class TSPTestDataSupplier extends NoLayerTestDataSupplier<TSPState, TSPProblem> {

    private final Path dir;

    public TSPTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<TSPProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return new TSPProblem(filePath.toString());
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
    protected DdoModel<TSPState> model(TSPProblem problem) {
        return new TSPDdoModel(problem) {
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
                return (s1, s2) -> Integer.compare(s1.toVisit.cardinality(), s2.toVisit.cardinality());
            }

            @Override
            public WidthHeuristic<TSPState> widthHeuristic() {
                return new FixedWidth<>(10);
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

            @Override
            public NoLayerDominanceChecker<TSPState> dominance() {
                return new TSPNoLayerDominanceChecker();
            }
        };
    }

    private abstract static class TSPDdoModel extends TSPModel implements DdoModel<TSPState> {
        public TSPDdoModel(TSPProblem problem) {
            super(problem);
        }
    }
}
