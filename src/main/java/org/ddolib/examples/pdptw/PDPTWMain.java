package org.ddolib.examples.pdptw;

import org.ddolib.astar.core.solver.ACSSolver;
import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import static java.lang.Math.max;

public final class PDPTWMain {

    /**
     * Generates a PDPTW problem with a single vehicle:
     * a TSP problem such that
     * nodes are grouped by pair: (pickup node; delivery node)
     * in a pair, the pickup node must be reached before the delivery node
     * the problem can also have "unrelated nodes" that are not involved in such a pair
     *
     * @param n         the number of nodes of the PDPTW problem
     * @param unrelated the number of nodes that are not involved in a pickup-delivery pair.
     *                  there might be one more unrelated node than specified here
     * @return a PDPTW problem
     */
    public static PDPTWInstance genRandomInstance(int n, int unrelated, int maxCapa, Random random) {

        int[] x = new int[n];
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            x[i] = random.nextInt(100);
            y[i] = random.nextInt(100);
        }

        int[][] distance = new int[n][];
        for (int i = 0; i < n; i++) {
            distance[i] = new int[n];
            for (int j = 0; j < n; j++) {
                distance[i][j] = dist(x[i] - x[j], y[i] - y[j]);
            }
        }
        TimeWindow[] timeWindows = new TimeWindow[n];
        for (int i = 0; i < n; i++) {
            int earlyLine = random.nextInt(200);
            int deadline = earlyLine + 550;
            timeWindows[i] = new TimeWindow(earlyLine, deadline);
        }
        HashMap<Integer, Integer> pickupToAssociatedDelivery = new HashMap<>();

        int numberOfPairs = Math.floorDiv(n - max(1, unrelated), 2);
        int firstDelivery = numberOfPairs + 1;
        for (int p = 1; p < firstDelivery; p++) {
            int d = firstDelivery + p - 1;
            pickupToAssociatedDelivery.put(p, d);
        }

        return new PDPTWInstance(distance, pickupToAssociatedDelivery, maxCapa, timeWindows, Integer.MAX_VALUE);
    }

    /**
     * Generates a PDPTW problem with a single vehicle:
     * a TSP problem such that
     * nodes are grouped by pair: (pickup node; delivery node)
     * in a pair, the pickup node must be reached before the delivery node
     * the problem can also have "unrelated nodes" that are not involved in such a pair
     *
     * this generator will generate an instance with a known solution although there might be a better one
     *
     * @param n         the number of nodes of the PDPTW problem
     * @param unrelated the number of nodes that are not involved in a pickup-delivery pair.
     *                  there might be one more unrelated node than specified here
     * @return a PDPTW problem
     */
    public static PDPTWInstance genInstance2(int n, int unrelated, Random random) {

        int[] x = new int[n];
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            x[i] = random.nextInt(100);
            y[i] = random.nextInt(100);
        }

        int[][] distance = new int[n][];
        for (int i = 0; i < n; i++) {
            distance[i] = new int[n];
            for (int j = 0; j < n; j++) {
                distance[i][j] = dist(x[i] - x[j], y[i] - y[j]);
            }
        }

        //generate a solution; based on random sort

        List<Integer> solution = new ArrayList<>();
        for (int i = 1; i <= n-1; i++) {
            solution.add(i);
        }
        Collections.shuffle(solution, random);

        HashMap<Integer, Integer> pickupToAssociatedDelivery = new HashMap<>();
        HashMap<Integer, Integer> deliveryToAssociatedPickup = new HashMap<>();
        HashMap<Integer, Integer> nodeToAssociatedNode = new HashMap<>();

        HashSet<Integer> unrelatedNodes = new HashSet<Integer>(IntStream.range(0, n).boxed().toList());

        int numberOfPairs = Math.floorDiv(n - max(1, unrelated), 2);
        int firstDelivery = numberOfPairs + 1;
        for (int p = 1; p < firstDelivery; p++) {
            int d = firstDelivery + p - 1;
            nodeToAssociatedNode.put(p,d);
            nodeToAssociatedNode.put(d,p);
            unrelatedNodes.remove(p);
            unrelatedNodes.remove(d);
        }

        TimeWindow[] timeWindows = new TimeWindow[n];
        int currentTime = 0;  //startTime is  0; also earlyLine for node0
        int currentNode = 0;
        int totalDistance = 0;
        int currentContent = 0;
        int maxCapa = 0;
        for(int nextNode : solution){
            int arrivalTime = currentTime + distance[currentNode][nextNode];
            totalDistance += distance[currentNode][nextNode];
            int earlyLine = arrivalTime - 100 + random.nextInt(200);
            currentTime  = new TimeWindow(earlyLine, 0).entryTime(arrivalTime);
            int deadline = currentTime + random.nextInt(200);
            timeWindows[nextNode] = new TimeWindow(earlyLine, deadline);
            currentNode = nextNode;

            if (deliveryToAssociatedPickup.containsKey(currentNode)) {
                //it is a delivery that we defined
                currentContent -= 1;
            }else if (nodeToAssociatedNode.containsKey(currentNode)) {
                //we must define the pick-up and its associated delivery
                int d = nodeToAssociatedNode.get(currentNode);
                deliveryToAssociatedPickup.put(d,currentNode);
                pickupToAssociatedDelivery.put(currentNode,d);
                nodeToAssociatedNode.remove(currentNode);
                currentContent += 1;
                if(currentContent > maxCapa){
                    maxCapa = currentContent;
                }
            } else if(! unrelatedNodes.contains(currentNode)){
                throw new Error("error in generator example");
            }
        }
        totalDistance += distance[currentNode][0];
        int arrivalTime = currentTime + distance[currentNode][0];
        int deadline = arrivalTime + random.nextInt(100);
        timeWindows[0] = new TimeWindow(0, deadline);

        //now, we must calculate the maxCapa for the solution
        return new PDPTWInstance(distance, pickupToAssociatedDelivery, maxCapa, timeWindows, Integer.MAX_VALUE);
    }

    public static int biasedRandom(Random random, int[] valuesAndBias){
       // System.out.println("biasedRandom" + Arrays.toString(valuesAndBias));
        int summedBias = Arrays.stream(valuesAndBias).sum();
        int draw = random.nextInt(summedBias);
        //draw < summedBias
        for(int i = 0 ; i < valuesAndBias.length; i++){
            draw = draw - valuesAndBias[i];
            if(draw <= 0 && valuesAndBias[i] != 0){
                //System.out.println("return " + i);
                return i;
            }
        }
        //if we get there, there has been a problem
        throw new Error("error in random");
    }

    /**
     * Generates a PDPTW problem with a single vehicle:
     * a TSP problem such that
     * nodes are grouped by pair: (pickup node; delivery node)
     * in a pair, the pickup node must be reached before the delivery node
     * the problem can also have "unrelated nodes" that are not involved in such a pair
     *
     * this generator will generate an instance with a known solution although there might be a better one
     *
     * @param n         the number of nodes of the PDPTW problem
     * @param unrelated the number of nodes that are not involved in a pickup-delivery pair.
     *                  there might be one more unrelated node than specified here
     * @return a PDPTW problem
     */
    public static PDPTWInstance genInstance3(int n, int unrelated, int maxCapa, Random random) {

        int[] x = new int[n];
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            x[i] = random.nextInt(100);
            y[i] = random.nextInt(100);
        }

        int[][] distance = new int[n][];
        for (int i = 0; i < n; i++) {
            distance[i] = new int[n];
            for (int j = 0; j < n; j++) {
                distance[i][j] = dist(x[i] - x[j], y[i] - y[j]);
            }
        }

        //generate a solution; based on random sort

        List<Integer> solution = new ArrayList<>();
        for (int i = 1; i <= n-1; i++) {
            //solution does not include zero
            solution.add(i);
        }
        Collections.shuffle(solution, random);


        HashMap<Integer, Integer> pickupToAssociatedDelivery = new HashMap<>();
        HashMap<Integer, Integer> deliveryToAssociatedPickup = new HashMap<>();
        HashSet<Integer> unrelatedNodes = new HashSet<Integer>();
        unrelatedNodes.add(0);
        HashSet<Integer> openPickups = new HashSet<Integer>();

        int numberOfPairs = Math.floorDiv(n - max(1, unrelated), 2);
        int nbUnrelated = n - 2*numberOfPairs;

        TimeWindow[] timeWindows = new TimeWindow[n];
        int currentTime = 0;  //startTime is  0; also earlyLine for node0
        int currentNode = 0;
        int totalDistance = 0;
        int currentContent = 0;

        int numberOfNodesToAssign = n;
        for(int nextNode : solution){
            numberOfNodesToAssign -= 1;
            int arrivalTime = currentTime + distance[currentNode][nextNode];
            totalDistance += distance[currentNode][nextNode];
            int earlyLine = arrivalTime - 100 + random.nextInt(200);
            currentTime  = new TimeWindow(earlyLine, 0).entryTime(arrivalTime);
            int deadline = currentTime + random.nextInt(200);
            timeWindows[nextNode] = new TimeWindow(earlyLine, deadline);
            currentNode = nextNode;

            //what do we do with this node?
            //if capa is full, it is either a delivery or an unrelated node
            //otherwise, it is either a pickup, a delivery or an unrelated node
            int nbNodesForUnrelated = nbUnrelated - unrelatedNodes.size();

            //can it be a delivery? yes if there are openPickups
            int nbNodesForDelivery = openPickups.size();
            //can it be a pickup? yes if
            int nbNodesForPickup;
            if (currentContent == maxCapa){
                nbNodesForPickup = 0;
            }else {
                nbNodesForPickup = numberOfNodesToAssign - nbNodesForUnrelated - nbNodesForDelivery;
            }

            //random draw
            switch (biasedRandom(random,new int[]{nbNodesForUnrelated,nbNodesForPickup,nbNodesForDelivery})) {
                case 0: //unrelated
                    if(nbNodesForUnrelated ==0) throw new Error("A");
                    unrelatedNodes.add(currentNode);

                    break;
                case 1: //pickup
                    if(nbNodesForPickup ==0) throw new Error("B");
                    openPickups.add(currentNode);
                    currentContent += 1;
                    break;
                case 2: //delivery
                    if(nbNodesForDelivery == 0) throw new Error("C");
                    //get a pickup point
                    int pickup = (int) openPickups.toArray()[random.nextInt(openPickups.size())];

                    currentContent -= 1;
                    pickupToAssociatedDelivery.put(pickup,currentNode);
                    deliveryToAssociatedPickup.put(currentNode,pickup);
                    openPickups.remove(pickup);

                    //we delete one of the two timeWindows, to make the problem more challenging
                    if(random.nextBoolean()){
                        timeWindows[pickup] = new TimeWindow(0, Integer.MAX_VALUE);
                    }else{
                        timeWindows[currentNode] = new TimeWindow(0, Integer.MAX_VALUE);
                    }
            }
        }

        totalDistance += distance[currentNode][0];
        int arrivalTime = currentTime + distance[currentNode][0];
        int deadline = arrivalTime + random.nextInt(100);
        timeWindows[0] = new TimeWindow(0, deadline);

        PDPTWInstance instance = new PDPTWInstance(distance, pickupToAssociatedDelivery, maxCapa, timeWindows, arrivalTime);

        int[]fullSolutionArray = new int[n+1];
        for(int i = 0 ; i < n-1 ; i++){
            fullSolutionArray[i+1] = solution.get(i);
        }
        fullSolutionArray[0]= 0;
        fullSolutionArray[n] = 0;
        PDPTWSolution solution2 = new PDPTWSolution(new PDPTWProblem(instance), fullSolutionArray, arrivalTime);

        //System.out.println("Artificial Solution: " + solution2);

        //now, we must calculate the maxCapa for the solution
        return instance;
    }

    static int dist(int dx, int dy) {
        //we take floor to ensure that the matrix respects the triangular inequality
        return (int) Math.floor(Math.sqrt(dx * dx + dy * dy));
    }

    public static void main(final String[] args) throws IOException {

//        final PDPTWInstance instance = genRandomInstance(18, 2, 3, new Random(1));
        final PDPTWInstance instance = genInstance3(100, 5, 6, new Random(1));
        final PDPTWProblem problem = new PDPTWProblem(instance);

        System.out.println("problem:" + problem);
        System.out.println("initState:" + problem.initialState());

        Solver solver = solveDPD(problem, 1);
        PDPTWSolution solution = extractSolution(solver, problem);

        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.println("Eval from scratch: " + instance.eval(solution.solution));
        System.out.printf("Solution : %s%n", solution);
        System.out.println("Problem:" + problem);

        System.out.println("end");

        if(solution.value > problem.instance.knownSolutionValue){
            throw new Error("solution is worse than known one");
        }
    }

    /**
     * @param problem
     * @param solveurId
     *          0 for DDO
     *          1 for A*
     * @return
     */
    public static Solver solveDPD(PDPTWProblem problem, int solveurId) {

        SolverConfig<PDPTWState, PDPTWDominanceKey> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new PDPTWRelax(problem);
        config.ranking = new PDPTWRanking();
        config.flb = new PDPTWFastLowerBound(problem);
        config.width = new FixedWidth<>(5000);
        config.varh = new DefaultVariableHeuristic<>();
        config.cache = new SimpleCache<>(); //cache does not work on this problem dunno why
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.Frontier);
        config.dominance = new SimpleDominanceChecker<>(new PDPTWDominance(), problem.nbVars());

        config.verbosityLevel = 1;
        config.exportAsDot = false;

        //config.debugLevel = 1;

        switch(solveurId){
            case 0: {
                final Solver solver = new SequentialSolver<>(config);

                SearchStatistics statistics = solver.minimize();
                System.out.println(statistics);

                return solver;
            }
            case 1: {
                final Solver solver = new AStarSolver<>(config);

                SearchStatistics statistics = solver.minimize();
                System.out.println(statistics);

                return solver;
            }
            case 2:
                final Solver solver = new ACSSolver<>(config, 5);

                SearchStatistics statistics = solver.minimize();
                System.out.println(statistics);

                return solver;
        }
        return null;
    }

    public static PDPTWSolution extractSolution(Solver solver, PDPTWProblem problem) {
        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] route = new int[problem.nbVars() + 1];
                    route[0] = 0;
                    for (Decision d : decisions) {
                        route[d.var() + 1] = d.val();
                    }
                    return route;
                })
                .get();

        double value = -solver.bestValue().get();

        return new PDPTWSolution(problem, solution, value);
    }
}
