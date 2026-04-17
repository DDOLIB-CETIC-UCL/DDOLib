package org.ddolib.examples.pdptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.examples.pdp.PDPState;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a Pickup and Delivery Problem (PDP) instance with a single vehicle.
 * <p>
 * In this problem:
 * </p>
 * <ul>
 *     <li>Nodes may be grouped into pickup-delivery pairs, where a pickup node must be visited before its associated delivery node.</li>
 *     <li>There may also be unrelated nodes that are not part of any pair.</li>
 *     <li>The vehicle has a capacity limit that restricts how many pickups can be carried simultaneously.</li>
 *     <li>The problem is represented as a TSP-like graph with distances between nodes.</li>
 * </ul>
 *
 * <p>The class implements the {@link Problem} interface, providing methods for:</p>
 * <ul>
 *     <li>Number of variables {@link #nbVars()}</li>
 *     <li>Initial state {@link #initialState()}</li>
 *     <li>Transition function {@link #transition(PDPTWState, Decision)}</li>
 *     <li>Transition cost {@link #transitionCost(PDPTWState, Decision)}</li>
 *     <li>Domain of possible decisions {@link #domain(PDPTWState, int)}</li>
 * </ul>
 *
 *
 * <p>States are represented by {@link PDPTWState}, including:</p>
 * <ul>
 *     <li>The set of currently visited nodes</li>
 *     <li>The set of nodes still to visit</li>
 *     <li>The current vehicle load</li>
 * </ul>
 */
public class PDPTWProblem implements Problem<PDPTWState> {

    /**
     * Number of nodes in the problem.
     */
    public final int n;

    /**
     * Distance matrix between all nodes.
     */
    public final double[][] timeMatrix;

    /**
     * Maximum capacity of the vehicle.
     */
    public final int maxCapa;

    /**
     * Map of pickup nodes to their associated delivery nodes.
     */
    public final HashMap<Integer, Integer> pickupToAssociatedDelivery;

    /**
     * Map of delivery nodes to their associated pickup nodes.
     */
    public final HashMap<Integer, Integer> deliveryToAssociatedPickup;

    /**
     * Set of nodes that are not part of any pickup-delivery pair.
     */
    public final Set<Integer> unrelatedNodes;

    public final TimeWindow[] timeWindows;

    /**
     * Optional: a known solution of the problem.
     */
    private Optional<Double> aKnownSolutionValue;

    /**
     * Optional name of the instance to ease readability in tests.
     */
    private Optional<String> name = Optional.empty();


    PDPTWFastLowerBound myBoundCalculator = null;

    public void strengthenTimeWindows(){

        int deadlineStrengthen = 0;
        int earlyLineStrengthen = 0;
        String toReturn = "";
        for (int pickup : pickupToAssociatedDelivery.keySet()) {
            int delivery = pickupToAssociatedDelivery.get(pickup);

            //delivery.earlyLine = max(delivry.earlyLine,pickUp.ealyLine + travelTime(pickup,delivery)
            double newEarlyLine = timeWindows[pickup].start() + timeMatrix[pickup][delivery];
            if(newEarlyLine > timeWindows[delivery].start()){
                deadlineStrengthen ++;
                TimeWindow oldTW = timeWindows[delivery];
                TimeWindow newTW = new TimeWindow(newEarlyLine, timeWindows[delivery].end());
                //Strengthening the earlyLine is not about this one delivery node.
                //it is about exposing other earlylines in the FLB heuristics
                //and to do that we must shift as many early lines as possible to the latest time they are actually relevant
                toReturn += "\n\tearlyLineStrengthening " + pickup + "->*" + delivery + "*\n\t\tOLD:" + oldTW + "\n\t\tNEW:" + newTW;
                 timeWindows[delivery] = newTW;
            }

            // pickup.deadline = min(pickup.deadline,delivery.deadline - travelTime(pickup,delivery)
            double newDeadline = timeWindows[delivery].end() - timeMatrix[pickup][delivery];
            if(newDeadline < timeWindows[pickup].end()){
                earlyLineStrengthen ++;
                TimeWindow oldTW = timeWindows[pickup];
                TimeWindow newTW = new TimeWindow(timeWindows[pickup].start(), timeWindows[delivery].end() - timeMatrix[pickup][delivery]);
                //strengthening the deadline of pickup sill enable the solver to quicker identify
                // that a prefix does not lead to a feasible solution
                toReturn += "\n\tdeadlineStrengthen *" + pickup + "*->" + delivery + "\n\t\tOLD:" + oldTW + " \n\t\tNEW:" + newTW;
                timeWindows[pickup] = newTW;
            }
        }
        System.out.println("earlyLineStrengthen: " + earlyLineStrengthen + " deadlineStrengthen: " + deadlineStrengthen + toReturn);
    }
    /**
     * Constructs a PDPTWProblem from a distance matrix, a map of pickup-delivery pairs, and a maximum vehicle capacity.
     *
     * @param timeMatrix             distance matrix between all nodes
     * @param pickupToAssociatedDelivery mapping from pickup nodes to delivery nodes
     * @param maxCapa                    maximum capacity of the vehicle
     * @param timeWindows            the time window associated to each node
     */
    public PDPTWProblem(final double[][] timeMatrix,
                        HashMap<Integer, Integer> pickupToAssociatedDelivery,
                        int maxCapa,
                        TimeWindow[] timeWindows,
                        Optional<Double> aKnownSolutionValue,
                        Boolean strengthenTimeWindows) {

        this.timeMatrix = timeMatrix;
        this.n = timeMatrix.length;
        this.timeWindows = timeWindows;
        this.maxCapa = maxCapa;
        this.aKnownSolutionValue = aKnownSolutionValue;

        this.pickupToAssociatedDelivery = pickupToAssociatedDelivery;
        this.unrelatedNodes = new HashSet<Integer>(IntStream.range(0, n).boxed().toList());

        deliveryToAssociatedPickup = new HashMap<>();
        for (int p : pickupToAssociatedDelivery.keySet()) {
            int d = pickupToAssociatedDelivery.get(p);
            unrelatedNodes.remove(p);
            unrelatedNodes.remove(d);
            deliveryToAssociatedPickup.put(d, p);
        }
        if(strengthenTimeWindows){
            strengthenTimeWindows();
        }
        myBoundCalculator = new PDPTWFastLowerBound(this);
    }

    /**
     * Constructs a PDPProblem by reading an instance from a file.
     * <p>
     * The file format should include:
     * </p>
     * <ul>
     *     <li>The number of nodes and optionally the optimal value.</li>
     *     <li>The distance matrix between nodes.</li>
     *     <li>The pickup-delivery pairs.</li>
     * </ul>
     *
     * @param fname path to the instance file
     * @throws IOException if the file cannot be read
     */
    public PDPTWProblem(String fname) throws IOException {
        int numNodes = -1;
        double[][] matrix = null;
        Optional<Double> opti = Optional.empty();
        HashMap<Integer, Integer> pickupToAssociatedDelivery = new HashMap<>();
        TimeWindow[] tw = null;
        int maxCapa = -1;
        try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
            int linesCount = 0;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    linesCount--;
                } else if (linesCount == 0) { // read num nodes and optimal value
                    String[] tokens = line.split("\\s+");
                    numNodes = Integer.parseInt(tokens[0]);
                    matrix = new double[numNodes][numNodes];
                    tw = new TimeWindow[numNodes];
                    if (tokens.length >= 4) {
                        opti = Optional.of(Double.parseDouble(tokens[1]));
                    }
                } else if (linesCount == 1) { // read max capa
                    String[] tokens = line.split("\\s+");
                    maxCapa = Integer.parseInt(tokens[0]);

                } else if (linesCount <= numNodes+1) { // read distance matrix
                    int node = linesCount - 2;
                    String[] tokens = line.split("\\s+");
                    double[] row =
                            Arrays.stream(tokens).filter(s -> !s.isEmpty()).mapToDouble(Double::parseDouble).toArray();
                    matrix[node] = row;
                }else if (linesCount <= numNodes*2 +1) { //read timeWindow
                    int node = linesCount - numNodes - 2;
                    String[] tokens = line.split("\\s+");
                    tw[node] = new TimeWindow(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]));
                }else { // read pick-up and delivery pairs
                    String[] tokens = line.split(" -> ");
                    pickupToAssociatedDelivery.put(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
                }

                linesCount++;
            }
        }

        this.timeMatrix = matrix;
        this.pickupToAssociatedDelivery = pickupToAssociatedDelivery;
        this.timeWindows = tw;
        this.n = timeMatrix.length;
        this.aKnownSolutionValue = opti;
        this.maxCapa = maxCapa;

        this.unrelatedNodes = new HashSet<>(IntStream.range(0, n).boxed().toList());
        deliveryToAssociatedPickup = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : pickupToAssociatedDelivery.entrySet()) {
            int p = entry.getKey();
            int d = entry.getValue();
            unrelatedNodes.remove(p);
            unrelatedNodes.remove(d);
            deliveryToAssociatedPickup.put(d, p);
        }

        this.name = Optional.of(fname);

        strengthenTimeWindows();
        myBoundCalculator = new PDPTWFastLowerBound(this);
    }

    public void saveToFile(String fname) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {

            //the number of nodes
            bw.write("" + this.n);
            bw.write("\n\n");

            //the max capa
            bw.write("" + this.maxCapa);
            bw.write("\n\n");

            //the time matrix
            for(double[] line : timeMatrix) {
                for(double value : line) {
                    bw.write(value + " ");
                }
                bw.write("\n");
            }
            bw.write("\n");

            //time window
            for(TimeWindow tw : timeWindows) {
                bw.write(tw.start() + "  " + tw.end() + "\n");
            }
            bw.write("\n");

            // read pick-up and delivery pairs
            for(int pickup :pickupToAssociatedDelivery.keySet()) {
                bw.write(pickup + " -> " + pickupToAssociatedDelivery.get(pickup) + "\n");
            }
        }
    }

     /**
     * Returns the number of variables (decisions) in the problem.
     * <p>
     * Note: the last decision corresponds to returning to the depot (node 0).
     * </p>
     *
     * @return number of variables
     */
     @Override
     public int nbVars() {
        return n; //the last decision will be to come back to point zero
    }

    @Override
    public PDPTWState initialState() {
        BitSet openToVisit = new BitSet(n);
        openToVisit.set(1, n);

        for (int d : deliveryToAssociatedPickup.keySet()) {
            openToVisit.clear(d);
        }

        BitSet allToVisit = new BitSet(n);
        allToVisit.set(1, n);

        return new PDPTWState(singleton(0), openToVisit, allToVisit,0,0,
                timeWindows[0].start(),timeWindows[0].start());
    }

    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(n);
        toReturn.set(singletonValue);
        return toReturn;
    }

    @Override
    public double initialValue() {
        return 0;
    }



    @Override
    public Iterator<Integer> domain(PDPTWState state, int var) {
        if (var == n - 1) {
            //the final decision is to come back to node zero
            //it is only possible  if we are before the deadline of node0
            if(state.minCurrentTime
                    + state.current.stream().mapToDouble(from -> timeMatrix[from][0]).min().getAsDouble()
                    > timeWindows[0].end()) {
                return Collections.emptyIterator();
            }else{
                return singleton(0).stream().iterator();
            }
        } else {
            boolean canIncludePickups = state.minContent < maxCapa;
            boolean canIncludeDeliveries = state.maxContent !=0;

            //how many we need to visit from now on?
            int howManyToVisit = n - 1 - var;

            //check that all states that must be visited can still be visited given the currentTime,
            // otherwise, there is no successor at all.
            //this assumes that we have triangular inequality
            long nbStillReachablePoints = state.allToVisit.stream().filter(point ->
                    (state.minCurrentTime + (state.current.stream().mapToDouble(
                            from -> timeMatrix[from][point])).min().getAsDouble()) <= timeWindows[point].end()
            ).count();

            if(nbStillReachablePoints < howManyToVisit) return Collections.emptyIterator();

            IntStream choices = state.openToVisit.stream()
                    .filter(point ->
                            ((canIncludePickups | !pickupToAssociatedDelivery.containsKey(point))
                            && (canIncludeDeliveries | ! deliveryToAssociatedPickup.containsKey(point))));

            //TODO: can we re-use this instead of throwing it away?
            IntStream choices2 = choices.filter(choice ->{
                if(!state.openToVisit.get(choice)) throw new Error("error");
                if(var >= n-1) return true;
                PDPTWState potentialNext = transition(state,new Decision(var,choice));
                return (myBoundCalculator.fastLowerBound(potentialNext, n - var-2) < Double.MAX_VALUE);
            });

            //IntStream choices2 = choices;
            return choices2.boxed().iterator();
        }
    }

    @Override
    public PDPTWState transition(PDPTWState state, Decision decision) {
        int node = decision.value();
        BitSet newOpenToVisit = (BitSet) state.openToVisit.clone();
        newOpenToVisit.clear(node);

        BitSet newAllToVisit = (BitSet) state.allToVisit.clone();
        newAllToVisit.clear(node);

        int newMinContent = state.minContent;
        int newMaxContent = state.maxContent;
        if (pickupToAssociatedDelivery.containsKey(node)) {
            newOpenToVisit.set(pickupToAssociatedDelivery.get(node));
            newMinContent += 1;
            newMaxContent += 1;
        }

        if (deliveryToAssociatedPickup.containsKey(node)) {
            int p = deliveryToAssociatedPickup.get(node);
            if (newOpenToVisit.get(p)) {
                newOpenToVisit.clear(p);
            }
            newMinContent -= 1;
            newMaxContent -= 1;
        }

        if(newMinContent <0) newMinContent = 0;
        if(newMaxContent > maxCapa) newMaxContent = maxCapa;

        if(newMinContent > maxCapa) throw new Error("error");
        if(newMaxContent < 0) throw new Error("error");

        double minArrivalTime = state.minCurrentTime + state.current.stream()
                .mapToDouble(possibleCurrentNode -> timeMatrix[possibleCurrentNode][decision.value()])
                .min().getAsDouble();

    if(minArrivalTime < timeWindows[node].start()){
        minArrivalTime = timeWindows[node].start();
        }

        double maxArrivalTime = state.minCurrentTime + state.current.stream()
                .mapToDouble(possibleCurrentNode -> timeMatrix[possibleCurrentNode][decision.value()])
                .min().getAsDouble();

        if(maxArrivalTime < timeWindows[node].start()){
            maxArrivalTime = timeWindows[node].start();
        }

        return new PDPTWState(
                state.singleton(node),
                newOpenToVisit,
                newAllToVisit,
                newMinContent,
                newMaxContent,
                minArrivalTime,
                maxArrivalTime);
    }

    @Override
    public double transitionCost(PDPTWState state, Decision decision) {
        double travelTime= state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.value())
                .mapToDouble(
                        possibleCurrentNode -> timeMatrix[possibleCurrentNode][decision.value()])
                .min()
                .getAsDouble();

        //the final decision is to come back to node zero.
        // The earlyLine has a different semantics for that node.
        // However, since we started from it, we come back after its earlyLine anyway so we can use the same formula as the other nodes

        double waitTime = timeWindows[decision.value()].waitTime(state.maxCurrentTime + travelTime);
        return travelTime + waitTime;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        int vehicleContent = 0;
        double currentTime = timeWindows[0].start();
        int prevNode = 0;
        for (int i = 0; i < solution.length-1; i++) { //zero is in the solution as well
            if(pickupToAssociatedDelivery.containsKey(solution[i])) {
                vehicleContent += 1;
            }else if (deliveryToAssociatedPickup.containsKey(solution[i])){
                vehicleContent -= 1;
            }
            if(vehicleContent > maxCapa) {
                throw new InvalidSolutionException("vehicleContent > maxCapa");
            }
            TimeWindow window = timeWindows[solution[i]];
            currentTime += timeMatrix[prevNode][solution[i]];
            if(currentTime > window.end()){
                System.out.println("currentTime:" + currentTime + " node:" + solution[i] + " trw:" + window);
                throw new InvalidSolutionException("after deadline");
            }
            if(currentTime <= window.start()) {
                currentTime = window.start();
            }
            prevNode = solution[i];
        }
        currentTime += timeMatrix[solution[solution.length - 2]][0]; //final come back
        if(currentTime > timeWindows[0].end()) {
            System.out.println("currentTime:" + currentTime + " come back ToZero trw:" + timeWindows[0]);
            throw new InvalidSolutionException("comes back after deadline");
        }
        if(vehicleContent !=0){
            throw new InvalidSolutionException("non empty at the end");
        }
        return currentTime;
    }

    @Override
    public String toString() {
        return "PDPTWProblem(\n\tn:" + n + "\n" +
                "\taKnownSolutionValue:" + aKnownSolutionValue  + "\n" +
                "\tpdp:" + pickupToAssociatedDelivery.keySet().stream().map(p -> p + "->" + pickupToAssociatedDelivery.get(p)).toList() + "\n" +
                "\tmaxCapa:" + maxCapa + "\n" +
                "\tunrelated:" + unrelatedNodes.stream().toList() + "\n" +
                "\ttimeWindows" + Arrays.stream(timeWindows).map(l -> "\n\t " + l).toList() + "\n" +
                "\t" + Arrays.stream(timeMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList();
    }
}
