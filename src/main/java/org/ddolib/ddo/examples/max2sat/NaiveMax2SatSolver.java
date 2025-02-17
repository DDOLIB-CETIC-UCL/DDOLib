package org.ddolib.ddo.examples.max2sat;

/**
 * A naive Max2Sat solver which enumerate all the solution tp find the best one.
 */
public class NaiveMax2SatSolver {

    private final Max2SatProblem problem;
    private int _best = Integer.MIN_VALUE;
    private int[] _bestSolution;


    public NaiveMax2SatSolver(Max2SatProblem problem) {
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

    private int evaluateSolution(int[] sol) {
        int toReturn = 0;
        for (BinaryClause bc : problem.weights.keySet()) {
            int a = sol[Math.abs(bc.i) - 1];
            int b = sol[Math.abs(bc.j) - 1];
            int eval = bc.eval(a, b);
            toReturn += eval * problem.weights.get(bc);

        }
        return toReturn;
    }


}
