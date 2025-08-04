package org.ddolib.examples.ddo.mcp;

/**
 * Naive MCP solver which enumerates all the solution to find the best one. Used for tests.
 */
public class NaiveMCPSolver {

    private final MCPProblem problem;
    private int _best = Integer.MIN_VALUE;
    private int[] _bestSolution;

    public NaiveMCPSolver(MCPProblem problem) {
        this.problem = problem;
        _bestSolution = new int[problem.nbVars()];
    }

    public int best() {
        return _best;
    }

    public int[] bestSolution() {
        return _bestSolution;
    }

    public void maximize() {
        int[][] solutions = generatesBinaryValues(problem.nbVars());
        for (int[] sol : solutions) {
            int value = evaluateSolution(sol);
            if (value > _best) {
                _best = value;
                _bestSolution = sol;
            }
        }
    }

    private int[][] generatesBinaryValues(int n) {
        int num = (int) Math.pow(2, n);

        int[][] toReturn = new int[num][];

        for (int x = 0; x < num; x++) {
            String str = Integer.toBinaryString(x);
            String binary = String.format("%" + n + "s", str).replace(' ', '0');

            int[] tmp = new int[n];
            for (int i = 0; i < n; i++) {
                tmp[binary.length() - 1 - i] = Character.getNumericValue(binary.charAt(i));
            }
            toReturn[x] = tmp;
        }

        return toReturn;
    }

    private int evaluateSolution(int[] solution) {
        int toReturn = 0;
        for (int u = 0; u < problem.nbVars(); u++) {
            for (int v = u + 1; v < problem.nbVars(); v++) {
                if (solution[u] != solution[v]) toReturn += problem.graph.weightOf(u, v);
            }
        }
        return toReturn;
    }

    /**
     * Given an adjacency matrix, solves naively the instance of MCP.
     *
     * @param matrix The adjacency matrix defining the MCP
     * @return The optimal solution of the MCP
     */
    public static int getOptimalSolution(int[][] matrix) {
        Graph graph = new Graph(matrix);
        MCPProblem problem = new MCPProblem(graph);
        NaiveMCPSolver solver = new NaiveMCPSolver(problem);
        solver.maximize();
        return solver.best();
    }
}
