package org.ddolib.examples.mks;

import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.util.testbench.TestDataSupplier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class MKSTestDataSupplier extends TestDataSupplier<MKSState, MKSProblem> {

    private final Path dir;

    public MKSTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<MKSProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile)
                    .map(filePath -> {
                        try {
                            return new MKSProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected DdoModel<MKSState> model(MKSProblem problem) {
        DdoModel<MKSState> model = new DdoModel<MKSState>() {
            @Override
            public Problem<MKSState> problem() {
                return problem;
            }

            @Override
            public MKSRelax relaxation() {
                return new MKSRelax();
            }

            @Override
            public MKSRanking ranking() {
                return new MKSRanking();
            }

            @Override
            public WidthHeuristic<MKSState> widthHeuristic() {
                return new FixedWidth<>(100);
            }

            @Override
            public ReductionStrategy<MKSState> relaxStrategy() {
                return new GHP<>(new MKSDistance(problem));
                // return new Hybrid<>(new MKSRanking(), new MKSDistance(problem));
                // return new CostBased<>(new MKSRanking());
            }

            @Override
            public ReductionStrategy<MKSState> restrictStrategy() {
                return new CostBased<>(new MKSRanking());
                // return new Kmeans<>(new MKSCoordinates(problem));
            }
        };
        return model;
    }
}

