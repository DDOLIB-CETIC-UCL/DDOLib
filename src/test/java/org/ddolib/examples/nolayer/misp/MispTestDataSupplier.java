package org.ddolib.examples.nolayer.misp;

import org.ddolib.modeling.layered.StateRanking;
import org.ddolib.modeling.nolayer.DdoModel;
import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Problem;
import org.ddolib.modeling.nolayer.Relaxation;
import org.ddolib.solving.ddo.core.heuristics.cluster.nolayer.CostBased;
import org.ddolib.solving.ddo.core.heuristics.cluster.nolayer.ReductionStrategy;
import org.ddolib.solving.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.solving.ddo.core.heuristics.width.WidthHeuristic;
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

public class MispTestDataSupplier extends NoLayerTestDataSupplier<MispState, MispProblem> {

    private final Path dir;

    public MispTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<MispProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return MispProblem.fromFile(filePath.toString());
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
    protected DdoModel<MispState> model(MispProblem problem) {
        return new MispDdoModel(problem) {
            @Override
            public FastLowerBound<MispState> lowerBound() {
                return (state) -> state.remainingNodes().isEmpty() ? 0 : -1;
            }
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
                return VerbosityLevel.SILENT;
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }

            @Override
            public org.ddolib.common.dominance.NoLayerDominanceChecker<MispState> dominance() {
                return new MispNoLayerDominanceChecker();
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };
    }

    private abstract static class MispDdoModel extends MispModel implements DdoModel<MispState> {
        public MispDdoModel(MispProblem problem) {
            super(problem);
        }
    }
}
