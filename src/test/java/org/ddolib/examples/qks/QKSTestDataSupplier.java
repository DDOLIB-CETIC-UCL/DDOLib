package org.ddolib.examples.qks;

import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.mks.*;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.DefaultFastLowerBound;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.util.testbench.TestDataSupplier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class QKSTestDataSupplier extends TestDataSupplier<QKSState, QKSProblem> {

    private final Path dir;

    public QKSTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<QKSProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile)
                    .map(filePath -> {
                        try {
                            return new QKSProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected DdoModel<QKSState> model(QKSProblem problem) {
        DdoModel<QKSState> model = new DdoModel<>() {
            @Override
            public Problem<QKSState> problem() {
                return problem;
            }

            @Override
            public QKSRelax relaxation() {
                return new QKSRelax();
            }

            @Override
            public QKSRanking ranking() {
                return new QKSRanking();
            }

            @Override
            public WidthHeuristic<QKSState> widthHeuristic() {
                return new FixedWidth<>(100);
            }

            @Override
            public ReductionStrategy<QKSState> relaxStrategy() {
                // return new GHP<>(new QKSDistance(problem));
                // return new Hybrid<>(new QKSRanking(), new QKSDistance(problem));
                return new CostBased<>(new QKSRanking());
            }

            @Override
            public ReductionStrategy<QKSState> restrictStrategy() {
                return new CostBased<>(new QKSRanking());
                // return new Kmeans<>(new QKSCoordinates(problem));
            }

            @Override
            public FastLowerBound<QKSState> lowerBound() {
                return new QKSFastLowerBound(problem);
            }
        };
        return model;
    }
}
