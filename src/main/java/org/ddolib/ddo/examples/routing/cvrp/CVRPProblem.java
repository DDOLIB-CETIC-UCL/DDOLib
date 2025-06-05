package org.ddolib.ddo.examples.routing.cvrp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;
import org.ddolib.ddo.examples.routing.util.RoutePosition;
import org.ddolib.ddo.examples.routing.util.RoutingNode;
import org.ddolib.ddo.examples.routing.util.VirtualRoutingNodes;

import java.util.*;

import static java.lang.Integer.min;

public class CVRPProblem implements Problem<CVRPState> {
    private final int v;
    private final int n;
    final int maxCapacity;
    final int[][] distancesMatrix;
    final int[] loading;

    private final ArrayList<VRPDecision> decisions;
    private final HashMap<VRPDecision, Integer> decisionIndexes;

    public CVRPProblem(int v, int n, int maxCapacity, int[][] distancesMatrix, int[] loading) {
        this.v = v;
        this.n = n;
        this.maxCapacity = maxCapacity;
        this.distancesMatrix = distancesMatrix;
        this.loading = loading;

        this.decisions = new ArrayList<>();
        this.decisionIndexes = new HashMap<>();
        // Last decision to go back to depot
        VRPDecision backToDepot = new VRPDecision(-1, 0);
        decisions.add(backToDepot);
        decisionIndexes.put(backToDepot, 0);
        int i = 1;
        for (int vehicle = 0; vehicle < v; vehicle++) {
            for (int node = 1; node < n; node++) {
                VRPDecision d = new VRPDecision(vehicle, node);
                decisions.add(d);
                decisionIndexes.put(d, i);
                i++;
            }
        }
    }


    @Override
    public int nbVars() {
        return n;
    }

    @Override
    public CVRPState initialState() {
        int[] initialCapacity = new int[v];
        Arrays.fill(initialCapacity, maxCapacity);
        RoutePosition[] initialPos = new RoutePosition[v];
        Arrays.fill(initialPos, new RoutingNode(0));
        BitSet initialMust = new BitSet(n);
        initialMust.set(1, n);
        BitSet initialMaybe = new BitSet(n);
        return new CVRPState(initialPos, initialCapacity, -1, initialMust, initialMaybe, 0);
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(CVRPState state, int var) {
        HashSet<Integer> toReturn = new HashSet<>();
        if (state.depth() == nbVars() - 1) {
            // //The only decision for the last variable is to return all the vehicles to the depot
            toReturn.add(0);
        } else {
            for (int vehicle = 0; vehicle <= min(state.lastUsedVehicle() + 1, v - 1); vehicle++) {
                for (
                        int node = state.mustVisit().nextSetBit(0);
                        node != -1;
                        node = state.mustVisit().nextSetBit(node + 1)
                ) {
                    if (state.capacity()[vehicle] >= loading[node]) {
                        VRPDecision d = new VRPDecision(vehicle, node);
                        toReturn.add(decisionIndexes.get(d));
                    }
                }
            }

        }
        return toReturn.iterator();
    }

    @Override
    public CVRPState transition(CVRPState state, Decision decision) {
        VRPDecision d = decisions.get(decision.var());
        int[] newCapacity = state.capacity().clone();
        newCapacity[d.vehicle()] -= loading[d.node()];
        RoutePosition[] newPos = state.pos().clone();
        newPos[d.vehicle()] = new RoutingNode(d.node());
        int newLast = d.vehicle() > state.lastUsedVehicle() ? state.lastUsedVehicle() + 1 : state.lastUsedVehicle();
        BitSet newMust = new BitSet(n);
        newMust.or(state.mustVisit());
        newMust.clear(d.node());
        return new CVRPState(newPos, newCapacity, newLast, newMust, new BitSet(), state.depth() + 1);
    }

    @Override
    public int transitionCost(CVRPState state, Decision decision) {
        VRPDecision d = decisions.get(decision.var());

        return -minDistance(state.pos()[d.vehicle()], d.node());
    }

    int minDistance(RoutePosition from, int to) {
        return switch (from) {
            case RoutingNode(int value) -> distancesMatrix[value][to];
            case VirtualRoutingNodes(Set<Integer> nodes) ->
                    nodes.stream().mapToInt(x -> distancesMatrix[x][to]).min().getAsInt();
        };
    }

}
