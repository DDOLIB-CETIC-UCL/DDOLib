package org.ddolib.examples.smic.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.smic.*;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.debug.DebugLevel;

import java.util.Arrays;

public class SmicApp extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final SMICProblem problem = new SMICProblem("data/SMIC/data10_2.txt");
        final DdoModel<SMICState> model = new DdoModel<>() {
            @Override
            public Relaxation<SMICState> relaxation() {
                return new SMICRelax(problem);
            }

            public SMICRanking ranking() {
                return new SMICRanking();
            }

            @Override
            public WidthHeuristic<SMICState> widthHeuristic() {
                return new FixedWidth<>(2);
            }

            @Override
            public Problem<SMICState> problem() {
                return problem;
            }

            @Override
            public SMICFastLowerBound lowerBound() {
                return new SMICFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<SMICState> dominance() {
                return new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };

        SmicChartView smicView = new SmicChartView(problem.initInventory, problem.capaInventory);

        Scene scene = new Scene(smicView, 800, 600);
        stage.setTitle("SMIC Demo");
        stage.setScene(scene);
        stage.show();

        Thread computation = new Thread(() -> {
            int[] bestSolution = new int[problem.nbVars()];
            SearchStatistics stats = Solvers.minimizeDdo(model, (sol, stat) -> {
                Arrays.setAll(bestSolution, i -> sol[i]);
                smicView.refresh(problem.toTasks(sol));
            });

            System.out.printf("Find solution in %d ms%n", stats.runTimeMs());
            System.out.printf("Solution: %s%n", Arrays.toString(bestSolution));
            System.out.printf("Value: %.0f%n", stats.incumbent());
        });

        computation.setDaemon(true);
        computation.start();
    }
}
