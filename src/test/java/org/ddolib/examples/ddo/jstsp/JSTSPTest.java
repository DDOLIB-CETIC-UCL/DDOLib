package org.ddolib.examples.ddo.jstsp;

import org.ddolib.astar.examples.JSTSP.JSTSPFastUpperBound;
import org.ddolib.astar.examples.JSTSP.JSTSPInstance;
import org.ddolib.astar.examples.JSTSP.JSTSPProblem;
import org.ddolib.astar.examples.JSTSP.JSTSPState;
import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.ddolib.factory.Solvers.acsSolver;
import static org.ddolib.factory.Solvers.astarSolver;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSTSPTest {
    @Test
    public void dummyTest() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/dummy", "problem 1");
        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 11);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 11);
    }
    @Test
    public void YanasseTest() {
        int[] sol = new int[]{18, 17, 15, 18, 16, 19, 19, 16, 17, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 17, 16, 16, 16, 15, 19, 19, 16, 15, 18, 17, 18, 18, 19, 22, 18, 18, 20, 20, 18, 30, 21, 23, 23, 21, 26, 22, 21, 28, 25, 29, 24, 24, 26, 24, 25, 23, 23, 31, 23, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 22, 22, 21, 22, 21, 22, 27, 21, 21, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 24, 24, 24, 21, 24, 24, 20, 22, 25, 24, 25, 24, 30, 25, 31, 27, 28, 30, 28, 26, 29, 29, 27, 26, 31, 26, 31, 29, 29, 29, 29, 34, 31, 35, 31, 28, 32, 32, 30, 28, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 28, 26, 25, 29, 29, 25, 30, 38, 28, 27, 26, 25, 25, 25, 25, 25, 25, 25, 25, 25, 31, 26, 28, 32, 26, 30, 27, 25, 34, 29, 35, 36, 35, 36, 35, 36, 36, 35, 36, 33, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 26, 27, 27, 29, 25, 25, 30, 28, 25, 27, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 26, 25, 25, 25, 25, 25, 25, 25, 25, 25, 28, 27, 30, 26, 26, 25, 27, 26, 27, 28, 26, 26, 26, 25, 25, 26, 27, 26, 25, 27, 29, 28, 27, 27, 27, 31, 29, 29, 30, 31, 35, 35, 32, 31, 30, 32, 32, 33, 31, 33, 25, 25, 25, 27, 25, 25, 24, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25};
        int idx = 0;
        for(int i=1;i<30;i++) {
            for (int j=1;j<=10;j++) {
                JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Yanasse/Tabela1/L" + i + ".txt", "problem "+j);
        
                final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
                final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
                final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
                final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
                final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
                );
                final Solver solverACS = acsSolver(
                        problem,
                        varh,
                        fub,
                        dominance,
                        10
                );
                solverAstar.maximize(0, false);
                assertEquals(-1*solverAstar.bestValue().get(), sol[idx]);
                solverACS.maximize(0, false);
                assertEquals(-1*solverACS.bestValue().get(), sol[idx]);
                idx+=1;
            }
        }
        sol = new int[]{17, 18, 18, 18, 15, 19, 17, 17, 16, 17, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 21, 15, 16, 15, 18, 18, 24, 15, 17, 17, 21, 19, 18, 21, 24, 18, 19, 20, 20, 28, 22, 21, 27, 28, 22, 28, 21, 31, 22, 30, 26, 28, 29, 25, 28, 25, 24, 26, 34, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 23, 23, 22, 23, 24, 24, 24, 24, 22, 24, 20, 20, 20, 20, 21, 20, 20, 21, 20, 21, 25, 25, 20, 23, 26, 26, 28, 22, 24, 20, 28, 29, 28, 29, 24, 28, 30, 29, 28, 28, 28, 28, 30, 34, 28, 31, 25, 32, 27, 27, 33, 34, 33, 36, 30, 31, 32, 31, 34, 29, 26, 26, 25, 26, 25, 25, 26, 25, 25, 25, 29, 31, 27, 29, 27, 27, 31, 30, 28, 28, 30, 28, 31, 30, 28, 37, 29, 32, 35, 31, 40, 40, 40, 37, 36, 41, 39, 39, 38, 35, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 29, 31, 25, 25, 25, 25, 26, 28, 28, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 26, 25, 25, 25, 26, 25, 25, 25, 25, 25, 29, 28, 31, 25, 27, 25, 28, 30, 26, 27, 27, 27, 27, 25, 26, 28, 27, 26, 27, 28, 33, 32, 28, 30, 32, 37, 30, 34, 30, 32, 37, 35, 32, 32, 32, 33, 34, 35, 32, 36, 21, 21, 22, 27, 24, 21, 23, 21, 22, 22, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20};
        idx = 0;
        for(int i=1;i<30;i++) {
            for (int j=1;j<=10;j++) {
                JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Yanasse/Tabela2/L" + i + ".txt", "problem "+j);
        
                final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
                final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
                final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
                final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
                final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
                );
                final Solver solverACS = acsSolver(
                        problem,
                        varh,
                        fub,
                        dominance,
                        10
                );
                solverAstar.maximize(0, false);
                assertEquals(-1*solverAstar.bestValue().get(), sol[idx]);
                solverACS.maximize(0, false);
                assertEquals(-1*solverACS.bestValue().get(), sol[idx]);
                idx+=1;
            }
        }
    }
    @Test
    public void datA1_1Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA1", "problem 1");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 14);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 14);
    }
    @Test
    public void datA1_2Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA1", "problem 2");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 11);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 11);
    }
    @Test
    public void datA1_3Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA1", "problem 3");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 12);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 12);
    }
    @Test
    public void datA1_4Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA1", "problem 4");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 13);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 13);
    }
    @Test
    public void datA1_5Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA1", "problem 5");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 13);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 13);
    }
    @Test
    public void datA1_6Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA1", "problem 6");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 14);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 14);
    }
    @Test
    public void datA1_7Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA1", "problem 7");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 12);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 12);
    }
    @Test
    public void datA1_8Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA1", "problem 8");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 11);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 11);
    }
    @Test
    public void datA1_9Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA1", "problem 9");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 13);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 13);
    }
    @Test
    public void datA1_10Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA1", "problem 10");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 12);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 12);

    }
    @Test
    public void datA2_1Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA2", "problem 1");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 11);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 11);
    }
    @Test
    public void datA2_2Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA2", "problem 2");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA2_3Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA2", "problem 3");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA2_4Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA2", "problem 4");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 11);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 11);

    }
    @Test
    public void datA2_5Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA2", "problem 5");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 11);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 11);
    }
    @Test
    public void datA2_6Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA2", "problem 6");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 12);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 12);
    }
    @Test
    public void datA2_7Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA2", "problem 7");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 11);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 11);
    }
    @Test
    public void datA2_8Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA2", "problem 8");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA2_9Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA2", "problem 9");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 11);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 11);
    }
    @Test
    public void datA2_10Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA2", "problem 10");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 11);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 11);

    }

    @Test
    public void datA3_1Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA3", "problem 1");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA3_2Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA3", "problem 2");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA3_3Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA3", "problem 3");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA3_4Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA3", "problem 4");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA3_5Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA3", "problem 5");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA3_6Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA3", "problem 6");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 11);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 11);
    }
    @Test
    public void datA3_7Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA3", "problem 7");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA3_8Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA3", "problem 8");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA3_9Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA3", "problem 9");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA3_10Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA3", "problem 10");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);

    }

    @Test
    public void datA4_1Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA4", "problem 1");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA4_2Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA4", "problem 2");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA4_3Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA4", "problem 3");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA4_4Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA4", "problem 4");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA4_5Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA4", "problem 5");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA4_6Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA4", "problem 6");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA4_7Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA4", "problem 7");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA4_8Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA4", "problem 8");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA4_9Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA4", "problem 9");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);
    }
    @Test
    public void datA4_10Test() {
        JSTSPInstance instance = JSTSPInstance.readFile("data/JSTSP/Catanzaro/datA4", "problem 10");
        long t0 = System.currentTimeMillis();

        final JSTSPProblem problem = new JSTSPProblem(instance,instance.getMin_cost());
        final VariableHeuristic<JSTSPState> varh = new DefaultVariableHeuristic<JSTSPState>();
        final JSTSPFastUpperBound fub = new JSTSPFastUpperBound(problem);
        final DefaultDominanceChecker<JSTSPState> dominance = new DefaultDominanceChecker<>();
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10
        );
        solverAstar.maximize(0, false);
        assertEquals(-1*solverAstar.bestValue().get(), 10);
        solverACS.maximize(0, false);
        assertEquals(-1*solverACS.bestValue().get(), 10);

    }
}
