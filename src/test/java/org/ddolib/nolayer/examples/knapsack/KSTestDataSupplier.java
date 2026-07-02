package org.ddolib.nolayer.examples.knapsack;

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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class KSTestDataSupplier extends NoLayerTestDataSupplier<KSState, KSProblem> {

    private final Path dir;

    public KSTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<KSProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return KSProblem.fromFile(filePath.toString());
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
    protected DdoModel<KSState> model(KSProblem problem) {
        return new KSDdoModel(problem) {
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
                            currentItem = Math.min(currentItem, s.currentItem()); // Relax by taking min currentItem
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
                return new FixedWidth<>(10);
            }

            @Override
            public NoLayerDominanceChecker<KSState> dominance() {
                return new NoLayerDominanceChecker<KSState>() {
                    private final java.util.Map<Integer, java.util.TreeSet<ValueState>> bestStates = new java.util.HashMap<>();

                    @Override
                    public boolean updateDominance(KSState state, double value) {
                        java.util.TreeSet<ValueState> set = bestStates
                                .computeIfAbsent(state.currentItem(), k -> new java.util.TreeSet<>());
                        ValueState vs = new ValueState(state, value);
                        ValueState floor = set.floor(vs);
                        if (floor != null && floor.state.remainingCapacity() >= state.remainingCapacity() && floor.value <= value) {
                            return true; // Dominated!
                        }

                        java.util.Iterator<ValueState> iterator = set.tailSet(vs).iterator();
                        while (iterator.hasNext()) {
                            ValueState higher = iterator.next();
                            if (state.remainingCapacity() >= higher.state.remainingCapacity() && value <= higher.value) {
                                iterator.remove();
                            } else {
                                break;
                            }
                        }
                        set.add(vs);
                        return false;
                    }

                    @Override
                    public void clear() {
                        bestStates.clear();
                    }

                    class ValueState implements Comparable<ValueState> {
                        final KSState state;
                        final double value;

                        ValueState(KSState state, double value) {
                            this.state = state;
                            this.value = value;
                        }

                        @Override
                        public int compareTo(ValueState o) {
                            if (this.value != o.value) return Double.compare(this.value, o.value);
                            return Integer.compare(this.state.remainingCapacity(), o.state.remainingCapacity());
                        }
                    }
                };
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

    private abstract static class KSDdoModel extends KSModel implements DdoModel<KSState> {
        public KSDdoModel(KSProblem problem) {
            super(problem);
        }
    }
}
